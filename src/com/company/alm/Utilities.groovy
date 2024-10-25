package com.caixabank.absis3

import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern
import com.cloudbees.plugins.credentials.SystemCredentialsProvider

import org.jenkinsci.plugins.workflow.cps.*

class Utilities {

    private static final Logger = CpsThreadGroup.current().execution.owner.listener.getLogger() as PrintStream

    static String getActualDate(String pattern, Date date = new Date()) {
        SimpleDateFormat SDF = new SimpleDateFormat(pattern)
        return SDF.format(date)
    }

    static Date parseDate(String pattern, String dateText) {
        SimpleDateFormat SDF = new SimpleDateFormat(pattern)
        return SDF.parse(dateText)
    }

    static boolean getBooleanPropertyOrDefault(def property, boolean defaultValue) {
        if (property==null) {
            return defaultValue
        } else if (property=="false") {
            return false
        } else if (property=="true") {
            return true
        }
        return defaultValue
    }

    static String prettyException(def err, boolean dumpStacktrace = true) {

											  
        String message = "Error message: ${err.getMessage()}\n\n"

        try {

            def prettyError = err.prettyPrint()
            message += "prettyPrint: ${prettyError}\n\n"

        } catch (err2) {
            //If no prettyPrint is present we do nothing. This is ok.
        }

        if (dumpStacktrace) {

            message += "Stacktrace:\n\n"

            def sw = new StringWriter()
            def pw = new PrintWriter(sw)
            err.printStackTrace(pw)
            message += sw.toString()

        }

        return message
    }

    static def isBranch(String flag, String branchName) {
        def index = branchName.indexOf(flag, 0)

        if (index == -1) return false
        else return true
    }

    static def getFeatureFromBranchName(String branchName, String flag, String separator) {
        def index = branchName.indexOf(flag, 0)

        if (index == -1) return ""
        else {
            def indexFeature = branchName.indexOf(separator, index + 1)
            if (index == -1) return ""
            else return branchName.substring(index)
        }
    }

    static List splitStringToList(String data, String ignoreValue) {
        List lines = data.split('\n').findAll { !it.startsWith(ignoreValue) }
        return lines
    }

    static List splitStringToListWithSplitter(String data, String splitter) {
        return data.split(splitter)
    }
	
	static String concatListWithSeparator(List list, String separator) {
		if(list!= null) {
			return list.join(separator)
		}else {
			return ""
		}
	}

    static def getMajorMinorFix(String version) {
        int[] versionMajor = [0, 0, 0]
        int i = 0
		
        version.tokenize('.').each { x ->
			//This if allows use spring parent poms (ej: 1.1.1.RELEASE)
			if(i < 3) {				
            	versionMajor[i] = x as Integer
            	i++
			}

        }


        return versionMajor
    }

    static boolean isLowerThan(String value, String compareTo) {

        if (value == null || value == "") return true;
        else {
            //Deberiamos comparar major + minor + fix
            def valueVersion = getMajorMinorFix(value)
            def compareToVersion = getMajorMinorFix(compareTo)

            if (valueVersion[0] > compareToVersion[0]) return false
            else {
                if (valueVersion[0] == compareToVersion[0]) {
                    if (valueVersion[1] > compareToVersion[1]) return false
                    else {
                        if (valueVersion[1] == compareToVersion[1]) {
                            if (valueVersion[2] < compareToVersion[2]) return true
                            else return false
                        } else return true
                    }
                } else return true
            }
        }
    }

    static String paramMapToHTTPGetStringParam(Map params) {
        def str = ""
        params.each {
            str += str ? "&" : "?"
            str += "${it.key}=${it.value ? java.net.URLEncoder.encode(it.value,"UTF-8") : ""}"
        }
		return str
    }
    /**
     * Devuelve el nombre del proyecto, o lo que es lo mismo, el nombre del ultimo folder del path recibido
     * @param projectPath String
     * @return
     */
    static String getNameOfProjectFromProjectPath(String projectPath) {
        return projectPath.substring(projectPath.lastIndexOf("/") + 1)
    }

    /**
     * Devuelve el nombre del proyecto, desde la url de git recibida
     * @param projectUrl String
     * @return
     */
    static String getNameOfProjectFromUrl(String projectUrl) {
        String lastPart = projectUrl.substring(projectUrl.lastIndexOf("/") + 1)
        return lastPart.substring(0, lastPart.lastIndexOf(".git"))
    }


    /**
     * Devuelve justo el path padre anterior al path recibido
     * @param projectPath String
     * @return String
     */
    static String getParentFolderPathFromProjectPath(String projectPath) {
        return projectPath.substring(0, projectPath.lastIndexOf("/"))
    }

	/**
	 * Actualiza el fichero JenkinsFile del path especificado con los valores establecidos en parametros
	 * @param scriptContext this. Pasar SIEMPRE el contexto del script
	 * @param params Map Mapa donde se especificarán los parametros. Los validos actualmente son:
	 * parentPath: String. Ruta del padre donde estará ubicado el JenkinsFile.
	 * executionProfile: String. Nombre del perfil de ejecución si se quiere especificar
	 * almFolder: String. Nombre del folder (y rama de la JenkinsSharedLibrary) de Jenkins donde contiene los jobs a probar. Si no se especificase, se usará master
	 * @return
	 */
	static void updateJenkinsFileWith(scriptContext, Map params, String almFolderJenkins) {
		String filenameToUpdate = params.get("filenameToUpdate", "Jenkinsfile")
		String executionProfile = params.get("executionProfile")
		String almFolder = params.get("almFolder")
        String almBranch = params.get("almBranch")
        String loggerLevel = params.get("loggerLevel")
        String agentBuild = params.get("agent")
		String parentPath = params.get("parentPath", "./")
		parentPath = parentPath[-1] == "/" ? parentPath : parentPath + "/"
		if (!scriptContext.sh(returnStdout: true, script: "test -f ${parentPath}${filenameToUpdate} && echo exist")) {
			scriptContext.printOpen("ERROR: ${filenameToUpdate} no encontrado. se aborta su update", EchoLevel.ERROR)
		} else {
			// Reading file
			String currentFileContent = scriptContext.sh(returnStdout: true, script: "cat ${parentPath}${filenameToUpdate}")
			if (almFolder) {
				String needle1 = "@Library(\"absis3-services"
				String needle2 = ")"
				String insert1 = "@${almBranch.trim()}"
				String insert2 = ", almFolder: '${almFolderJenkins}'"
				currentFileContent = currentFileContent.substring(0, currentFileContent.indexOf(needle1) + needle1.length()) + insert1 + currentFileContent.substring(currentFileContent.indexOf(needle1) + needle1.length())
				currentFileContent = currentFileContent.substring(0, currentFileContent.lastIndexOf(needle2)) + insert2 + currentFileContent.substring(currentFileContent.lastIndexOf(needle2))
			}
			if (executionProfile) {
				String needle3 = ")"
				String insert3 = ", executionProfile: '${executionProfile.trim().toUpperCase()}'"
				currentFileContent = currentFileContent.substring(0, currentFileContent.lastIndexOf(needle3)) + insert3 + currentFileContent.substring(currentFileContent.lastIndexOf(needle3))
			}
            if (loggerLevel) {
                String needle4 = ")"
                String insert4 = ", loggerLevel: '${loggerLevel.trim().toUpperCase()}'"
                currentFileContent = currentFileContent.substring(0, currentFileContent.lastIndexOf(needle4)) + insert4 + currentFileContent.substring(currentFileContent.lastIndexOf(needle4))
            }
            if (agentBuild) {
                String needle5 = ")"
                String insert5 = ", agent: '${agentBuild.trim()}'"
                currentFileContent = currentFileContent.substring(0, currentFileContent.lastIndexOf(needle5)) + insert5 + currentFileContent.substring(currentFileContent.lastIndexOf(needle5))
            }

			scriptContext.printOpen("${filenameToUpdate} actual previo a modificación:", EchoLevel.ALL)
			scriptContext.printOpen(scriptContext.sh(returnStdout: true, script: "cat ${parentPath}${filenameToUpdate}"), EchoLevel.ALL)

			// Writing file overriding
//            def errorCode = scriptContext.sh(returnStatus: true, script: "echo '${currentFileContent}'> ${parentPath}${filenameToUpdate}") // Esto no funciona como se espera. Se come todas las comillas simples en el resultado dentro del fichero
			def errorCode = scriptContext.writeFile(file: parentPath + filenameToUpdate, text: currentFileContent)

			scriptContext.printOpen("${filenameToUpdate} actual tras modificación:", EchoLevel.ALL)
			scriptContext.printOpen(scriptContext.sh(returnStdout: true, script: "cat ${parentPath}${filenameToUpdate}"), EchoLevel.ALL)

			if (!errorCode) {
				scriptContext.printOpen("${filenameToUpdate} actualizado", EchoLevel.INFO)
			} else {
				scriptContext.printOpen("ERROR: No se ha podido sobreescribir el fichero ${filenameToUpdate}. La salida al comando o metodo de escritura devolvió error: ${errorCode}", EchoLevel.ERROR)
			}
		}
	}
	
    /**
     * Actualiza el fichero JenkinsFile del path especificado con los valores establecidos en parametros
     * @param scriptContext this. Pasar SIEMPRE el contexto del script
     * @param params Map Mapa donde se especificarán los parametros. Los validos actualmente son:
     * parentPath: String. Ruta del padre donde estará ubicado el JenkinsFile.
     * executionProfile: String. Nombre del perfil de ejecución si se quiere especificar
     * almFolder: String. Nombre del folder (y rama de la JenkinsSharedLibrary) de Jenkins donde contiene los jobs a probar. Si no se especificase, se usará master
     * @return
     */
    static void updateJenkinsFileWith(scriptContext, Map params) {
        String filenameToUpdate = params.get("filenameToUpdate", "Jenkinsfile")
        String executionProfile = params.get("executionProfile")
        String almBranch = params.get("almBranch")
        String almFolder = params.get("almFolder")
        String loggerLevel = params.get("loggerLevel")
        String parentPath = params.get("parentPath", "./")
        String agentBuild = params.get("agent")
        parentPath = parentPath[-1] == "/" ? parentPath : parentPath + "/"
        if (!scriptContext.sh(returnStdout: true, script: "test -f ${parentPath}${filenameToUpdate} && echo exist")) {
            scriptContext.printOpen("ERROR: ${filenameToUpdate} no encontrado. se aborta su update", EchoLevel.ERROR)
        } else {
            // Reading file
            String currentFileContent = scriptContext.sh(returnStdout: true, script: "cat ${parentPath}${filenameToUpdate}")
            if (almFolder) {
                String needle1 = "@Library(\"absis3-services"
                String needle2 = ")"
                String insert1 = "@${almBranch.trim()}"
                String insert2 = ", almFolder: '${almFolder}'"
                currentFileContent = currentFileContent.substring(0, currentFileContent.indexOf(needle1) + needle1.length()) + insert1 + currentFileContent.substring(currentFileContent.indexOf(needle1) + needle1.length())
                currentFileContent = currentFileContent.substring(0, currentFileContent.lastIndexOf(needle2)) + insert2 + currentFileContent.substring(currentFileContent.lastIndexOf(needle2))
            }
            if (executionProfile) {
                String needle3 = ")"
                String insert3 = ", executionProfile: '${executionProfile.trim().toUpperCase()}'"
                currentFileContent = currentFileContent.substring(0, currentFileContent.lastIndexOf(needle3)) + insert3 + currentFileContent.substring(currentFileContent.lastIndexOf(needle3))
            }
            if (loggerLevel) {
                String needle4 = ")"
                String insert4 = ", loggerLevel: '${loggerLevel.trim().toUpperCase()}'"
                currentFileContent = currentFileContent.substring(0, currentFileContent.lastIndexOf(needle4)) + insert4 + currentFileContent.substring(currentFileContent.lastIndexOf(needle4))
            }

            if (agentBuild) {
                String needle5 = ")"
                String insert5 = ", agent: '${agentBuild.trim()}'"
                currentFileContent = currentFileContent.substring(0, currentFileContent.lastIndexOf(needle5)) + insert5 + currentFileContent.substring(currentFileContent.lastIndexOf(needle5))
            }

            scriptContext.printOpen("${filenameToUpdate} actual previo a modificación:", EchoLevel.ALL)
            scriptContext.printOpen(scriptContext.sh(returnStdout: true, script: "cat ${parentPath}${filenameToUpdate}"), EchoLevel.ALL)

            // Writing file overriding
//            def errorCode = scriptContext.sh(returnStatus: true, script: "echo '${currentFileContent}'> ${parentPath}${filenameToUpdate}") // Esto no funciona como se espera. Se come todas las comillas simples en el resultado dentro del fichero
            def errorCode = scriptContext.writeFile(file: parentPath + filenameToUpdate, text: currentFileContent)

            scriptContext.printOpen("${filenameToUpdate} actual tras modificación:", EchoLevel.ALL)
            scriptContext.printOpen(scriptContext.sh(returnStdout: true, script: "cat ${parentPath}${filenameToUpdate}"), EchoLevel.ALL)

            if (!errorCode) {
                scriptContext.printOpen("${filenameToUpdate} actualizado", EchoLevel.INFO)
            } else {
                scriptContext.printOpen("ERROR: No se ha podido sobreescribir el fichero ${filenameToUpdate}. La salida al comando o metodo de escritura devolvió error: ${errorCode}", EchoLevel.ERROR)
            }
        }
    }

    

    /**
     * Get Jenkinsfile from repo and branch
     * @param pathToRepoParam git repo
     * @param originBranchParam git branch
     * @param secret jenkins git secret
     */
    static void parseJenkinsFilePipelineParams(scriptContext, String jenkinsFile, map = [:]) {
        def regex = ~/(absisPipelineBuild|customPipelineConfigsBuild)\((?<params>.*)\)/
        def matcher = regex.matcher(jenkinsFile)        
        if (matcher.find()) {
            matcher.group("params").split(",").each { it ->
                def keyValue = it.split(":")
                def key = keyValue[0].trim().replaceAll("'", "").replaceAll("\"", "")
                def value = keyValue[1].trim().replaceAll("'", "").replaceAll("\"", "")
                map.put(key, value)     
            }
            if (scriptContext) {
                scriptContext.printOpen("Parametros del Jenkinsfile: $map" , EchoLevel.DEBUG)
            }
        } else {
            if (scriptContext) {
                scriptContext.printOpen("No se reconoce la JenkinsFile a parsear", EchoLevel.ERROR)
            }
        }

        return map
    }

    /**
     * Get Jenkinsfile from repo and branch
     * @param pathToRepoParam git repo
     * @param originBranchParam git branch
     * @param secret jenkins git secret
     */
    static void getRemoteJenkinsFileParams(pathToRepoParam, originBranchParam, secret, useproxy = true, connecttimeout = 90, maxtime = 45, retries = 5, retry_delay = 2) {
        def file = "Jenkinsfile"
        def pipelineParamsMaps        
        def exception = null

        def secretPlain = SystemCredentialsProvider.getInstance().getCredentials().find{it.id.equals(secret)}             
        def pathToRepoParamEncoded = URLEncoder.encode(pathToRepoParam.substring(GlobalVars.gitlabDomain.length(), pathToRepoParam.length() - 4), "UTF-8")
        def originBranchParamEncoded = URLEncoder.encode(originBranchParam, "UTF-8")

        def jenkinsFileUrlPath = "$GlobalVars.gitlabApiDomain$pathToRepoParamEncoded/repository/files/$file/raw?ref=$originBranchParamEncoded"
        
        def proxyOption = ""
        if (useproxy) {
            proxyOption = "--proxy $GlobalVars.proxyCaixa"
        }

        def startTime = System.currentTimeMillis()
	
        def command = "curl --connect-timeout $connecttimeout --max-time $maxtime --retry $retries --retry-delay $retry_delay --output ./$file --write-out %{http_code} $proxyOption -k -s -X GET $jenkinsFileUrlPath -H Private-Token:$secretPlain.secret"
		
        for (int retry = 0; retries; retry++) {
             def curlResult = Utilities.executeCommand(command)

             if (curlResult.exitCode == 0) {
                if (curlResult.err) {
                    exception = new IOException("Error $curlResult.err")
                } else if (curlResult.out) {
                    if (curlResult.out.trim() == "200") {
                        def catResult = Utilities.executeCommand("cat ./$file")
                        if (catResult.exitCode == 0 && catResult.out) {
                            def rmResult = Utilities.executeCommand("rm -f ./$file", connecttimeout)
                            pipelineParamsMaps = parseJenkinsFilePipelineParams(null, catResult.out)                            
                            exception = null
                            def ellapsedTime = System.currentTimeMillis() - startTime
                            Logger.println("Success getting Jenkinsfile from '$pathToRepoParam' branch '$originBranchParam in $ellapsedTime ms")
                            break;
                        }
                    } else {
                        exception =  Exception("Error $curlResult.out obtaining Remote Jenkinsfile from '$pathToRepoParam', branch $originBranchParam")
                    }
                } else {
                    exception = new Exception("Something was wrong!!")
                }
             } else {
                exception = new Exception("curl return $curlResult.exitCode")
             }
        }

        if (exception) {
            Logger.out.println("Jenking can't get jenkins file from '$originBranchParam' branch '$originBranchParam:\n $exception")
            throw exception
        }

        if (pipelineParamsMaps) {
            pipelineParamsMaps.put("projectinfo", [pathToRepoParamEncoded: pathToRepoParamEncoded, originBranchParamEncoded: originBranchParamEncoded])
        }

        return pipelineParamsMaps
    }

    /**
     * Execute command in shell
     * @param commamnd commando
     */
    static Map executeCommand(command, timeout = 4000) {
        def sout = new StringBuilder()
        def serr = new StringBuilder()
        def process = command.execute()
        process.waitForOrKill(timeout)
        process.consumeProcessOutput(sout, serr)
        
        return [
            out: sout.toString(),
            err: serr.toString(),
            exitCode: process.exitValue()
        ]
    }

    /**
     * Metodo para devolver diferentes plantillas de email.
     *
     * @param scriptContext this. Pasar SIEMPRE el contexto del script
     * @param params Map Mapa donde se especificarán los parametros. Los validos actualmente son:
     * type: String. Especifica el nombre del tipo de plantilla a devolver
     * @return Map. Devuelve un Map, con las keys subject y body, para usarse para el metodo de envio de email preferido
     */
    static Map getEmailTemplate(scriptContext, Map params) {
        String templateType = params.get("type", "default")
        String gitUrl = params.get("gitUrl", "<La URL de git no se pudo obtener de la pipeline>")
		String branchName = params.get("branchName", "<La rama de git no se pudo obtener de la pipeline>")
        Map arrayEmailFields = [:]


        switch (templateType) {


            case "updateCoreNocturneJobInBuildFailure":
                arrayEmailFields.subject = "[Absis3] Resultado Jenkins Nocturne job: ${scriptContext.env.JOB_BASE_NAME} - ${scriptContext.currentBuild.result}"
                arrayEmailFields.body = """
				Componente: ${scriptContext.env.JOB_BASE_NAME}
				Build: ${scriptContext.currentBuild.fullDisplayName}
				Resultado: ${scriptContext.currentBuild.result}
				
				URL de build: ${scriptContext.env.BUILD_URL}
				URL de Git: ${gitUrl}
				Rama Git que desencadenó el Build: ${branchName}
				
				Puedes ver la salida a consola completo aqui: ${scriptContext.env.BUILD_URL}/consoleFull
				"""
                break


            case "updateCoreNocturneJobFailure":
                arrayEmailFields.subject = "[Absis3] Resultado Jenkins Nocturne job: ${scriptContext.env.JOB_BASE_NAME} - PARTIAL FAILURE"
                arrayEmailFields.body = """
Componente: ${scriptContext.env.JOB_BASE_NAME}
Build: ${scriptContext.currentBuild.fullDisplayName}

URL de build: ${scriptContext.env.BUILD_URL}
Proyectos donde falló la actualización de la versión del Core:
${scriptContext.projectsFailed.join("\n")}

Puedes ver la salida a consola completo aqui: ${scriptContext.env.BUILD_URL}/consoleFull
"""
                break


            case "provisioningFailure":
				String appType = params.get("appType", "<UNKNOWN>")
                arrayEmailFields.subject = "[Absis3] Resultado de Provisioning Failed: ${scriptContext.env.JOB_BASE_NAME}"
                arrayEmailFields.body = """
Componente: ${scriptContext.env.JOB_BASE_NAME}
Tipo de aplicación: ${appType}
Build: ${scriptContext.currentBuild.fullDisplayName}
Resultado: ${scriptContext.currentBuild.result}

URL de build: ${scriptContext.env.BUILD_URL}
URL de Git: ${gitUrl}

Puedes ver la salida a consola completo aqui: ${scriptContext.env.BUILD_URL}consoleFull
"""
                break
				
				case "deProvisioningFailure":
				arrayEmailFields.subject = "[Absis3] Resultado del DeProvisioning Failed: ${scriptContext.env.JOB_BASE_NAME}"
				arrayEmailFields.body = """
Componente: ${scriptContext.env.JOB_BASE_NAME}
Tipo de aplicación: ${scriptContext.typeApp}
Build: ${scriptContext.currentBuild.fullDisplayName}
Resultado: ${scriptContext.currentBuild.result}

Puedes ver la salida a consola completo aqui: ${scriptContext.env.BUILD_URL}/consoleFull
"""
				break
        }


        return arrayEmailFields

    }
    /**
     * Metodo que ejecuta un comando sh bajo Jenkins, lanzando excepcion con su salida si falla
     * @param scriptContext this. Pasar SIEMPRE el contexto del script
     * @param params Map Mapa donde se especificarán los parametros. Los validos actualmente son:
     * commandString: String completo del comando de sh que se quiere ejecutar con sus parametros
     * allowOutputIfOk: Boolean. Indica si se quiere la salida del comando cuando la ejecución es satisfactoria. Como default no la devuelve
     * showOnlyLastNLines: int. Devuelve solo las N ultimas lineas pasadas en este parametro, del comando, si es fallido
     * showOnlyMvnErrors: Boolean. Devuelve solo las lineas que comiencen con [ERROR] del comando, capturando hasta el siguiente [, si es fallida la ejecución del comando mvn
     * @return String/Exception. Devolverá una excepción con el texto de la salida del comando en la excepcion, si el comando falla
     */
    static String runShCapturingError(scriptContext, Map params) {
        Boolean returnStatus = params.get("returnStatus", true)
        Boolean allowOutputIfOk = params.get("allowOutputIfOk", false)

        String commandString = params.get("commandString", "")

        int showOnlyLastNLines = params.get("showOnlyLastNLines", 0)
        Boolean showOnlyMvnErrors = params.get("showOnlyMvnErrors", false)
//        Boolean showOnlyErrorBrief = params.get("showOnlyErrorBrief", false)

        String stdOutFilename = params.get("stdOutFilename", "stdout.txt")
        String status = scriptContext.sh(returnStatus: returnStatus, script: commandString + ">" + stdOutFilename)
        String stdout = scriptContext.readFile(stdOutFilename).trim()

        scriptContext.sh 'rm ' + stdOutFilename

        if (showOnlyLastNLines) {
            List<String> stdOutList = stdout.split("\n")
            int listSize = stdOutList.size()
            stdout = stdOutList.subList(listSize - showOnlyLastNLines, listSize).join("\n")
        }
        if (showOnlyMvnErrors) {
            String regex = "(\\[ERROR].*(?:\\n*[^\\[]*\\n*)*)"
            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE)
            Matcher matcher = pattern.matcher(stdout)
            List<String> matchListResult = []
            while (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    matchListResult.add(matcher.group(i).trim())
                }
            }
            stdout = matchListResult.join("\n")

        }
        if (Integer.parseInt(status)) {

            scriptContext.printOpen("Shell command has failed\n\nLog:\n\n${stdout}", EchoLevel.ERROR)
            throw new Exception(stdout)

        } else if (allowOutputIfOk) {

            return stdout

        }
    }

    /**
     * Metodo que comprueba si un fichero existe o no
     * @param scriptContext this. Pasar SIEMPRE el contexto del script
     * @param params Map Mapa donde se especificarán los parametros. Los validos actualmente son:
     * path: String de la ruta a la que se quiere comprobar si el fichero existe.
     * returnExceptionIfNoExist: Boolean. Default true. Si el fichero no existe, lanza excepcion. Si es false este parametro, el metodo devolverá false sin lanzar excepcion.
     * @return Boolean
     */
    static boolean fileExist(scriptContext, Map params) {
        String path = params.get("path", "")
        Boolean returnExceptionIfNoExist = params.get("returnExceptionIfNoExist", true)
        String exist = scriptContext.sh(script: "test -e ${path} && echo exist || echo 'no exist'", returnStdout: true).trim()
        if (exist == "no exist") {
            if (returnExceptionIfNoExist) {
                throw new Exception("Fichero ${path} no encontrado")
            } else {
                return false
            }
        }
        return true
    }

    

}
