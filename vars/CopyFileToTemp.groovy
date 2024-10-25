import com.caixabank.absis3.EchoLevel

def call(String srcPath, String destPath) {
    String destDir = createTempDir()
    
    if (fileExists(srcPath)) {
        destPath = buildDestPath(destDir, destPath)
        writeFile file: destPath, text: readFile(srcPath)
        printOpen("CopyFileToTemp: copied ${srcPath} to ${destPath}", EchoLevel.ALL)
    } else {
        printOpen("CopyFileToTemp file not exists ${srcPath}", EchoLevel.ERROR)
    }
    
    return destDir
}

String createTempDir() {
    String tmpDir = pwd tmp: true
    String sanitizedTmpDir = tmpDir.replace(' ', '\\ ')

    cleanTmp(sanitizedTmpDir)

    return tmpDir
}

static String buildDestPath(String dir, String path) {
    return dir + File.separator + new File(path).getName()
}
