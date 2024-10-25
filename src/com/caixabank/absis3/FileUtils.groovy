package com.caixabank.absis3

class FileUtils implements Serializable {

    private final def scriptContext

    FileUtils(scriptContext) {
        this.scriptContext = scriptContext
    }

    List<String> findRecursivelyReversed(String baseDirectory, String findType, String fileName) {

        String cmd = "path=\"${baseDirectory}\"; \\\n" +
                "shift 1; \\\n" +
                "while [[ \$path != / ]]; do \\\n" +
                "    find \"\$path\" -maxdepth 1 -mindepth 1 ${findType} \"${fileName}\" -execdir pwd \\;; \\\n" +
                "    path=\"\$(readlink -f \"\$path\"/..)\"; \\\n" +
                "done"

        String[] paths = scriptContext.sh(
                script: "${cmd}",
                returnStdout: true
        ).split("\n")

        List<String> sanitizedPaths = new ArrayList<>()

        for (String path in paths) {
            if (path.trim()) {
                sanitizedPaths.add(path)
                scriptContext.printOpen("FileUtils.findRecursivelyReversed(): Found ${fileName} on ${path}", EchoLevel.DEBUG)
            }
        }

        return sanitizedPaths
    }

    List<String> findIPath(String findExecDirectory, String parameters, String pathToFind) {

        List<String> paths = new ArrayList<>()

        findExecDirectory = findExecDirectory?.trim() ? findExecDirectory : '.'
        parameters = parameters?.trim() ? parameters : ""

        String[] pathsFound = scriptContext.sh(
                script: "find ${findExecDirectory} ${parameters} -ipath '${pathToFind}'",
                returnStdout: true
        ).split("\n")

        for (String originPath in pathsFound) {
            if (originPath?.trim()) {
                paths.add(originPath)
                scriptContext.printOpen("FileUtils.findIPath(): Found path from ${originPath}", EchoLevel.DEBUG)
            }
        }

        return paths
    }

    List<ArtifactFilesPath> calculateSourceAndDestinationDirectoriesWithSubartifacts(String findExecDirectory, String pathToFind, String destinationBasePath, String destinationPath) {

        List<ArtifactFilesPath> documentationFilesPaths = new ArrayList<>()

        findExecDirectory = findExecDirectory ? findExecDirectory : '.'
        pathToFind = "/" == pathToFind?.charAt(0) ? pathToFind : "/".concat(pathToFind)
        destinationBasePath =
                "/" == destinationBasePath?.charAt(destinationBasePath.length() - 1)
                        ? destinationBasePath.substring(0, destinationBasePath.length())
                        : destinationBasePath
        destinationPath = "/" == destinationPath?.charAt(0) ? destinationPath : "/".concat(destinationPath)

        String[] pathsFound = scriptContext.sh(
                script: "find ${findExecDirectory} -type d -wholename '*${pathToFind}'",
                returnStdout: true
        ).split("\n")

        for (String originPath in pathsFound) {
            if (originPath?.trim()) {
                String artifactPath = originPath.replace(pathToFind, "")
                int subArtifactNameIndex = artifactPath.lastIndexOf("/")
                String subArtifactName = subArtifactNameIndex == -1 ? "" : artifactPath.substring(subArtifactNameIndex + 1).trim()
                String destinationFullPath =
                        subArtifactName?.trim()
                                ? destinationBasePath + "/" + subArtifactName + destinationPath
                                : destinationBasePath + destinationPath
                ArtifactFilesPath documentationPath = new ArtifactFilesPath(subArtifactName, originPath, destinationFullPath)
                documentationFilesPaths.add(documentationPath)
                scriptContext.printOpen("FileUtils.calculateSourceAndDestinationFolders(): Found location from ${documentationPath.originPath} to ${documentationPath.destinationFullPath}", EchoLevel.DEBUG)
            }
        }

        return documentationFilesPaths
    }

    void copyFilesFromDirectory2Directory(List<ArtifactFilesPath> artifactFilesPaths, boolean wipeDestinationFolderBeforeCopy) {
        artifactFilesPaths.each { artifactFilesPath ->
            copyFilesFromDirectoryToDirectory(artifactFilesPath.originPath, artifactFilesPath.destinationFullPath, wipeDestinationFolderBeforeCopy)
        }
    }

    void copyFilesFromDirectoryToDirectory(String sourcePath, String targetPath, wipeDestinationFolderBeforeCopy) {
        if (scriptContext.fileExists(sourcePath) && directoryHasFiles(sourcePath)) {
            this.createPathIfNotExists(targetPath)
            if (wipeDestinationFolderBeforeCopy) {
                this.cleanDirectoryContent(targetPath)
            }
            this.copyFiles(sourcePath + "/*", targetPath, true)
        } else {
            scriptContext.printOpen("FileUtils.copyFilesFromDirectory2Directory(): ${sourcePath} does not exist or has no files! No copy will be done", EchoLevel.DEBUG)
        }
    }

    void removeDirectory(String directoryPath) {

        scriptContext.printOpen("FileUtils.removeDirectory(): Removing directory ${directoryPath}", EchoLevel.DEBUG)

        String sanitizedDirectoryPath = FileUtils.sanitizePath(directoryPath)

        if (scriptContext.fileExists(sanitizedDirectoryPath)) {
            scriptContext.sh("rm -rf ${sanitizedDirectoryPath}")
        } else {
            scriptContext.printOpen("The directory ${sanitizedDirectoryPath} doesn\'t exist", EchoLevel.DEBUG)
        }

    }

    void cleanDirectoryContent(String destPath) {
        scriptContext.printOpen("FileUtils.cleanFolderContent(): Cleaning directory ${destPath} content", EchoLevel.DEBUG)
        String sanitizedDestPath = destPath.replace(' ', '\\ ')
        scriptContext.sh "rm -rf ${sanitizedDestPath}/*"
    }

    void copyFiles(String origin, String destination, boolean recursively) {

        scriptContext.printOpen("Copying files from ${origin} to ${destination}", EchoLevel.DEBUG)
        def copyParams = recursively ? "-R" : ""

        String sanitizedOrigin = origin.replace(' ', '\\ ')
        String sanitizedDestination = destination.replace(' ', '\\ ')

        if (directoryHasFiles(sanitizedOrigin.replace("/*", ""))) {
            scriptContext.sh "cp ${copyParams} ${sanitizedOrigin} ${sanitizedDestination}"
            String dirContent = scriptContext.sh(script: "ls -la ${sanitizedDestination}", returnStdout: true)
            scriptContext.printOpen("Directory content:\n${dirContent}", EchoLevel.DEBUG)
        } else {
            scriptContext.printOpen("FileUtils.copyFiles(): ${sanitizedOrigin} has no files! Nothing will be copied...", EchoLevel.DEBUG)
        }
    }

    String createPathIfNotExists(String path) {

        String sanitizedPath = path.replace(' ', '\\ ')

        scriptContext.printOpen("Creating directory ${path}", EchoLevel.DEBUG)
        scriptContext.sh "mkdir -p ${sanitizedPath}"

        return sanitizedPath
    }

    boolean directoryHasFiles(String path) {
        if (scriptContext.fileExists(path)) {

            String dirContent = scriptContext.sh(script: "ls -la ${path}", returnStdout: true)
            scriptContext.printOpen("Directory content:\n${dirContent}", EchoLevel.DEBUG)
            List<String> files = new ArrayList<>()
            final String[] sourcePathFileList = scriptContext.sh(script: "ls -1 ${path}", returnStdout: true).split()

            for (String sourcePathFile in sourcePathFileList) {
                if (sourcePathFile?.trim()) {
                    files.add(sourcePathFile)
                }
            }

            boolean hasFiles = !files.isEmpty()
            scriptContext.printOpen("FileUtils.directoryHasFiles(): ${path} ${hasFiles ? 'has' : 'has no'} files", EchoLevel.DEBUG)

            return hasFiles

        } else {

            return false

        }
    }

    void wipeFileContent(String file) {
        def indexExists = scriptContext.fileExists "${file}"
        if (indexExists) {
            scriptContext.printOpen("FileUtils.wipeFileContent(): Wiping content of ${file}", EchoLevel.DEBUG)
            scriptContext.writeFile file: "${file}", text: ""
            scriptContext.printFile("${file}", EchoLevel.DEBUG)
        }
    }

    void copySimpleFile(String pathFileOrigin, String pathFileDestination, String artifactName, String artifactVersion) {

        scriptContext.printOpen("Copy Simple file", EchoLevel.DEBUG)

        scriptContext.printOpen("path origin--- ${pathFileOrigin}", EchoLevel.DEBUG)

        scriptContext.printOpen("path destination--- ${pathFileDestination}", EchoLevel.DEBUG)

        scriptContext.sh "cp -f ${pathFileOrigin} ${pathFileDestination}"

        String firstLine = "# " + artifactName + "v" + artifactVersion

        String firstLineOri = "# " + artifactName

        scriptContext.printOpen("contenido para la primera linea del fichero readme renombrado --- ${firstLine}", EchoLevel.DEBUG)


        //	scriptContext.sh "sed -i 's/${artifactName}/# ${artifactName}v${artifactVersion}/g' ${pathFileDestination}"
        //scriptContext.sh "sed -i 's/${firstLineOri}/${firstLine}/g' ${pathFileDestination}"
        scriptContext.sh "sed -i '1i ${firstLine}' ${pathFileDestination}"

        return
    }

    void moveSimpleFile(String pathFileOrigin, String pathFileDestination) {

        scriptContext.printOpen("Move Simple file", EchoLevel.DEBUG)

        scriptContext.printOpen("path origin--- ${pathFileOrigin}", EchoLevel.DEBUG)

        scriptContext.printOpen("path destination--- ${pathFileDestination}", EchoLevel.DEBUG)

        if (scriptContext.fileExists(pathFileOrigin)) {
            scriptContext.sh "mv ${pathFileOrigin} ${pathFileDestination}"
        } else {
            scriptContext.printOpen("The source file ${pathFileOrigin} doesn\'t exists", EchoLevel.DEBUG)
        }

        return
    }

    void moveFiles(String origin, String destination) {

        scriptContext.printOpen("Moving files from ${origin} to ${destination}", EchoLevel.DEBUG)

        String sanitizedOrigin = origin.replace(' ', '\\ ')
        String sanitizedDestination = destination.replace(' ', '\\ ')

        scriptContext.sh "mv ${sanitizedOrigin} ${sanitizedDestination}"
        String dirContent = scriptContext.sh(script: "ls -la ${sanitizedDestination}", returnStdout: true)
        scriptContext.printOpen("Directory content:\n${dirContent}", EchoLevel.DEBUG)

    }

    void addTextToFile(String pathFileDestination, String textToEnd) {
        scriptContext.printOpen("add text to end of file", EchoLevel.DEBUG)

        scriptContext.printOpen("path destination " + pathFileDestination, EchoLevel.DEBUG)

        scriptContext.printOpen("textToEnd " + textToEnd, EchoLevel.DEBUG)

        scriptContext.sh "echo '${textToEnd}' >> ${pathFileDestination}"

        scriptContext.printOpen("add text to end of file success", EchoLevel.DEBUG)
    }

    /**
     * STATIC METHODS
     */

    static String sanitizePath(String path) {

        return path.replace(' ', '\\ ')

    }
}
