import groovy.transform.Field
import com.project.alm.*

import hudson.Functions

@Field Map pipelineParams

@Field String MICRO_SERVICE_project
@Field String DATA_SERVICE_project
@Field String LIBRARY_project
@Field String ARCH_MICRO_project
@Field String ARCH_PLUGIN_project
@Field String ARCH_LIBRARY_project
@Field String CONFIG_project
@Field String AGENT_project
@Field String loggerLevel = env.loggerLevel ? ((String)env.loggerLevel).toUpperCase() : 'ALL'

//Pipeline que permite el inicio del test del ciclo de vida
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    skippedProjects = []
    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo

    MICRO_SERVICE_project = env.MICRO_SERVICE_project
    DATA_SERVICE_project = env.DATA_SERVICE_project
    LIBRARY_project = env.LIBRARY_project
    ARCH_MICRO_project = env.ARCH_MICRO_project
    ARCH_PLUGIN_project = env.ARCH_PLUGIN_project
    ARCH_LIBRARY_project = env.ARCH_LIBRARY_project
	CONFIG_project = env.CONFIG_project
    AGENT_project = env.AGENT_project

    def loggerLevel = env.loggerLevel ? ((String)env.loggerLevel).toUpperCase() : 'ALL'
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
		libraries {
			lib("absis3-services@$params.BRANCH_NAME")
		}
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
            timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
        }
        stages {
            stage('get-git-repo') {
                steps {
                    script {

                        GlobalVars.CONSOLE_LOGGER_LEVEL = loggerLevel

                        def gitRepoUrl = null

                        env.getEnvironment().each { key, value ->
							printOpen("key ${key} - value ${value}",EchoLevel.INFO)
                            if (key.contains("project") && value && !key.contains("AGENT")) {
                                // Si el env es de string de proyecto y no está vacio

                                gitRepoUrl = GlobalVars.gitlabDomain + value.trim() + ".git"
                                try {
                                    pomXmlStructure = getPomFromGitRepo(gitRepoUrl, GlobalVars.MASTER_BRANCH, value.trim())
                                    // Esto también descargará el repo en la ruta especificada en el 3er parametro
                                    // TODO ésto puede fallar, porque, y si el proyecto nuevo descargado no tiene creado previamente un pom.xml? El script getPomFromGitRepo no lo maneja (aqui fallaria: analizePomXml.groovy:48). Habria que modificarlo
                                    def projectType = key.substring(0, key.indexOf("project") - 1)
                                    this.setProperty("${projectType}_pomVersion", pomXmlStructure.artifactVersion)
                                    this.setProperty("${projectType}_pomGroupId", pomXmlStructure.groupId)

                                } catch (Exception e) {
                                    printOpen("Falló la descarga del clonado de proyecto '${gitRepoUrl}' con error ${e.getMessage()} Este existe? ", EchoLevel.ERROR)
                                    printOpen("Procedemos a ignorar éste projecto en el Pipeline: ${value.trim()}", EchoLevel.ERROR)
                                    skippedProjects.add(value.trim()) // se añade aqui para ignorarlo más tarde
                                }
                            }
                        }
                    }
                }
            }
            stage('download-initializr-template-code') {
                steps {
                    script {
                        def paramsMap = [ // Common GET params
                                          "type"            : "maven-project",
                                          "language"        : "java",
                                          "bootVersion"     : GlobalVars.INITITALIZR_DEFAULT_BOOT_VERSION, // TODO Fijado, pero, quizás debiera tenerse dinamico, leyendo el valor default de la app de initializer, parseando el HTML del GET principal
                                          "absisCompany"    : "CBK",
                                          "packaging"       : "jar",
                                          "javaVersion"     : "1.8",
                                          "autocomplete"    : "",
                                          "generate-project": ""
                        ]
						
						if (CONFIG_project) {
							CONFIG_paramsMap = paramsMap.clone()
							CONFIG_paramsMap.baseDir = Utilities.getNameOfProjectFromProjectPath(CONFIG_project)
							CONFIG_paramsMap.name = Utilities.getNameOfProjectFromProjectPath(CONFIG_project)
							CONFIG_paramsMap.description = "Modulo+Configuracion"
							CONFIG_paramsMap.artifactId = Utilities.getNameOfProjectFromProjectPath(CONFIG_project)
							CONFIG_paramsMap.absisApp = "SRV_CFG"
							CONFIG_paramsMap.groupId = "com.caixabank.absis.apps.service.cbk.demo"
							CONFIG_paramsMap.groupIdNoCompany = "com.caixabank.absis.apps.service.demo"
							CONFIG_paramsMap.simpleProject = "true"
							CONFIG_paramsMap.packageName = "com.caixabank.absis.apps.service.cbk.demo"
						}

                        if (MICRO_SERVICE_project) {
                            MICRO_SERVICE_paramsMap = paramsMap.clone()
                            MICRO_SERVICE_paramsMap.baseDir = Utilities.getNameOfProjectFromProjectPath(MICRO_SERVICE_project)
                            MICRO_SERVICE_paramsMap.name = Utilities.getNameOfProjectFromProjectPath(MICRO_SERVICE_project)
                            MICRO_SERVICE_paramsMap.description = "Micro+Service+aplicativo"
                            MICRO_SERVICE_paramsMap.artifactId = Utilities.getNameOfProjectFromProjectPath(MICRO_SERVICE_project)
                            MICRO_SERVICE_paramsMap.absisApp = "MICRO_SERVICE"
                            MICRO_SERVICE_paramsMap.groupId = "com.caixabank.absis.apps.service.cbk.demo"
                            MICRO_SERVICE_paramsMap.groupIdNoCompany = "com.caixabank.absis.apps.service.demo"
                            MICRO_SERVICE_paramsMap.simpleProject = "true"
                            MICRO_SERVICE_paramsMap.packageName = "com.caixabank.absis.apps.service.cbk.demo"
                        }

                        if (DATA_SERVICE_project) {
                            DATA_SERVICE_paramsMap = paramsMap.clone()
                            DATA_SERVICE_paramsMap.baseDir = Utilities.getNameOfProjectFromProjectPath(DATA_SERVICE_project)
                            DATA_SERVICE_paramsMap.name = Utilities.getNameOfProjectFromProjectPath(DATA_SERVICE_project)
                            DATA_SERVICE_paramsMap.description = "Data+Service+aplicativo"
                            DATA_SERVICE_paramsMap.artifactId = Utilities.getNameOfProjectFromProjectPath(DATA_SERVICE_project)
                            //We have to provide a non-company groupId to match the same we have in master. versions:set uses master groupId when testing
                            DATA_SERVICE_paramsMap.groupId = "com.caixabank.absis.apps.dataservice.demo"
                            DATA_SERVICE_paramsMap.groupIdNoCompany = "com.caixabank.absis.apps.dataservice.demo"
                            DATA_SERVICE_paramsMap.absisApp = "DATA_SERVICE"
                            DATA_SERVICE_paramsMap.simpleProject = "true"
                            DATA_SERVICE_paramsMap.packageName = "com.caixabank.absis.apps.dataservice.demo"
                        }

                        if (LIBRARY_project) {
                            LIBRARY_paramsMap = paramsMap.clone()
                            LIBRARY_paramsMap.baseDir = Utilities.getNameOfProjectFromProjectPath(LIBRARY_project)
                            LIBRARY_paramsMap.name = Utilities.getNameOfProjectFromProjectPath(LIBRARY_project)
                            LIBRARY_paramsMap.description = "Librer%C3%ADa+com%C3%BAn+de+aplicaciones"
                            LIBRARY_paramsMap.artifactId = Utilities.getNameOfProjectFromProjectPath(LIBRARY_project)
                            LIBRARY_paramsMap.groupId = "com.caixabank.absis.apps.cbk.demo"
                            LIBRARY_paramsMap.groupIdNoCompany = "com.caixabank.absis.apps.demo"
                            LIBRARY_paramsMap.absisApp = "APP_COMMON_LIB"
                            LIBRARY_paramsMap.simpleProject = "true"
                            LIBRARY_paramsMap.packageName = "com.caixabank.absis.apps.common.demo"
                        }

                        if (ARCH_MICRO_project) {
                            ARCH_MICRO_paramsMap = paramsMap.clone()
                            ARCH_MICRO_paramsMap.baseDir = Utilities.getNameOfProjectFromProjectPath(ARCH_MICRO_project)
                            ARCH_MICRO_paramsMap.name = Utilities.getNameOfProjectFromProjectPath(ARCH_MICRO_project)
                            ARCH_MICRO_paramsMap.description = "Proyecto+agregador+de+un+microservicio+de+arquitectura"
                            ARCH_MICRO_paramsMap.artifactId = Utilities.getNameOfProjectFromProjectPath(ARCH_MICRO_project)
                            ARCH_MICRO_paramsMap.groupId = "com.caixabank.absis.arch.cbk.demo"
                            ARCH_MICRO_paramsMap.groupIdNoCompany = "com.caixabank.absis.arch.demo"
                            ARCH_MICRO_paramsMap.aggregator = "MICRO_ARCH"
                            ARCH_MICRO_paramsMap.simpleProject = "false"
                            ARCH_MICRO_paramsMap.packageName = "com.caixabank.absis.arch.cbk.demo"
                        }

                        if (ARCH_PLUGIN_project) {
                            ARCH_PLUGIN_paramsMap = paramsMap.clone()
                            ARCH_PLUGIN_paramsMap.baseDir = Utilities.getNameOfProjectFromProjectPath(ARCH_PLUGIN_project)
                            ARCH_PLUGIN_paramsMap.name = Utilities.getNameOfProjectFromProjectPath(ARCH_PLUGIN_project)
                            ARCH_PLUGIN_paramsMap.description = "Proyecto+agregador+de+un+plugin+absis"
                            ARCH_PLUGIN_paramsMap.artifactId = Utilities.getNameOfProjectFromProjectPath(ARCH_PLUGIN_project)
                            ARCH_PLUGIN_paramsMap.groupId = "com.caixabank.absis.arch.cbk.demo"
                            ARCH_PLUGIN_paramsMap.groupIdNoCompany = "com.caixabank.absis.arch.demo"
                            ARCH_PLUGIN_paramsMap.aggregator = "PLUGIN"
                            ARCH_PLUGIN_paramsMap.simpleProject = "false"
                            ARCH_PLUGIN_paramsMap.packageName = "com.caixabank.absis.arch.cbk.demo"
                        }

                        if (ARCH_LIBRARY_project) {
                            ARCH_LIBRARY_paramsMap = paramsMap.clone()
                            ARCH_LIBRARY_paramsMap.baseDir = Utilities.getNameOfProjectFromProjectPath(ARCH_LIBRARY_project)
                            ARCH_LIBRARY_paramsMap.name = Utilities.getNameOfProjectFromProjectPath(ARCH_LIBRARY_project)
                            ARCH_LIBRARY_paramsMap.description = "Proyecto+agregador+de+un+starter+absis"
                            ARCH_LIBRARY_paramsMap.artifactId = Utilities.getNameOfProjectFromProjectPath(ARCH_LIBRARY_project)
                            ARCH_LIBRARY_paramsMap.groupId = "com.caixabank.absis.arch.cbk.demo"
                            ARCH_LIBRARY_paramsMap.groupIdNoCompany = "com.caixabank.absis.arch.demo"
                            ARCH_LIBRARY_paramsMap.aggregator = "STARTER"
                            ARCH_LIBRARY_paramsMap.simpleProject = "false"
                            ARCH_LIBRARY_paramsMap.packageName = "com.caixabank.absis.arch.cbk.demo"
                        }

                        env.getEnvironment().each { key, value ->
                            if (key.contains("project") && !key.contains("AGENT") && value && !skippedProjects.contains(value.trim())) {
                                // Si el env es de string de proyecto y no está vacio, y no está en la lista de ignorados
                                try {
                                    def projectType = key.substring(0, key.indexOf("project") - 1)
                                    def params = Utilities.paramMapToHTTPGetStringParam(this.getProperty("${projectType}_paramsMap"))
                                    // Solo funciona el .getProperty() con variables no declaradas ni con tipo ni con def. Ojo con ésto

                                    response = httpRequest(consoleLogResponseBody: false,
                                        httpMode: "GET",
                                        url: GlobalVars.URL_ZIP_INITIALIZR_DEV + params,
                                        httpProxy: "http://proxyserv.svb.lacaixa.es:8080",
                                        outputFile: "${Utilities.getNameOfProjectFromProjectPath(value.trim())}.zip"
                                    )
                                    printOpen("Template de proyecto '${Utilities.getNameOfProjectFromProjectPath(value.trim())}' descargado desde el initializr", EchoLevel.INFO)
                                } catch (Exception e) {
                                    printOpen("Falló la descarga del codigo del initializr para el proyecto '${Utilities.getNameOfProjectFromProjectPath(value.trim())}'", EchoLevel.ERROR)
                                    printOpen(Functions.printThrowable(e), EchoLevel.ERROR)
                                    printOpen("Procedemos a ignorar este projecto en el Pipeline: ${value.trim()}", EchoLevel.ERROR)
                                    skippedProjects.add(value.trim()) // se añade aqui para ignorarlo más tarde
                                }
                            }
                        }
                    }
                }
            }
            stage('push-projects') {
                steps {
                    script {
                        def date = new Date()
                        def formattedDate = date.format("yyy-MM-dd")
                        env.getEnvironment().each { key, value ->
                            if (key.contains("project") && !key.contains("AGENT") && value && !skippedProjects.contains(value.trim())) {
                                // Si el env es de string de proyecto y no está vacio, y no está en la lista de ignorados
                                try {
                                    withCredentials([
                                        usernamePassword(credentialsId: GlobalVars.GIT_CREDENTIAL_PROFILE_ID, passwordVariable: GlobalVars.GIT_CREDENTIAL_PASSWORD_VAR, usernameVariable: GlobalVars.GIT_CREDENTIAL_USER_VAR)
                                    ]) {
                                        String branchName = "${GlobalVars.FEATURE_BRANCH}/USTest_${env.BUILD_ID}_${formattedDate}"
                                        sh "cd ${value.trim()} && git checkout -b ${branchName}"
                                        sh "cd ${value.trim()} && pwd && ls -la * &&  find . -not -path '*/\\.*' -delete"
                                        // Borramos tod lo que estaba en master, porque queremos probar la plantilla del micro vacia del initializr
                                        printOpen("Borrado contenido heredado de la rama master", EchoLevel.DEBUG)

                                        sh "unzip -o ${Utilities.getNameOfProjectFromProjectPath(value.trim())}.zip -d ${Utilities.getParentFolderPathFromProjectPath(value.trim())}"
                                        printOpen("Descomprimido nuevo codigo generado del initializr", EchoLevel.DEBUG)
                                        // Descomprimimos en la ruta del proyecto, sobrescribiendo. Como dentro del zip ya tendrá
                                        // un folder con el nombre del proyecto, por eso descomprimimos en el folder padre

										Utilities.updateJenkinsFileWith(this, ["parentPath": value.trim(), "almFolder": params.ALM_FOLDER, "almBranch": params.BRANCH_NAME, "executionProfile": "COMPLETE_TEST_AUTO", "loggerLevel": "${loggerLevel}", "agent": "$AGENT_project"])

										try {
                                            def projectType = key.substring(0, key.indexOf("project") - 1)
                                            def currentPomVersion = this.getProperty("${projectType}_pomVersion")
                                            def currentPomGroupId = this.getProperty("${projectType}_pomGroupId")
                                            printOpen("El nuevo valor para el proyecto ${projectType} es de version ${currentPomVersion} el grupo es de ${currentPomGroupId}", EchoLevel.DEBUG)
                                            if (currentPomVersion && currentPomGroupId) {
                                                configFileProvider([configFile(fileId: 'absis3-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
                                                    sh "cd ${value.trim()} && mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS ${GlobalVars.GLOBAL_MVN_PARAMS} versions:set -DnewVersion=${currentPomVersion} -DgroupId=${currentPomGroupId} -DgenerateBackupPoms=false"
                                                }
                                                // Actualizamos versión del pom y grupId, con la que teniamos antes en el repositorio, si la teniamos
                                            }
                                        } catch (Exception e) {
                                            printOpen("No se pudo actualizar pom.xml de proyecto '${value.trim()}'. El error fue: ${e.getMessage()}", EchoLevel.ERROR)
                                        }

                                        GitUtils git = new GitUtils(this, false)
                                        git.removeFromWorkspaceFilesNotAllowedInGitRepos("${value.trim()}")

                                        def gitVersion = sh(script: "git --version", returnStdout: true).replace("git version", "").trim()

                                        if (GitUtils.gitHandlerVersionEqualsOrExceeds(gitVersion, "2.0.0")) {

                                            sh "cd ${value.trim()} && git add ."

                                        } else {

                                            sh "cd ${value.trim()} && git add --all ."

                                        }

                                        sh "cd ${value.trim()} && git commit -m 'Update code and created branch to start testing with deploy' --allow-empty"
                                        sh "cd ${value.trim()} && git -c http.sslVerify=false push origin ${branchName}"
                                    }
                                    printOpen("Proyecto '${Utilities.getNameOfProjectFromProjectPath(value.trim())}' actualizado y pusheado con el codigo del initializr de TST", EchoLevel.INFO)
                                } catch (Exception e) {
                                    printOpen("Falló el push de proyecto '${Utilities.getNameOfProjectFromProjectPath(value.trim())}'", EchoLevel.ERROR)
                                    printOpen(Functions.printThrowable(e), EchoLevel.ERROR)
                                    printOpen("Procedemos a ignorar éste projecto en el Pipeline: ${value.trim()}", EchoLevel.ERROR)
                                    skippedProjects.add(value.trim()) // se añade aqui para ignorarlo más tarde
                                }
                            }
                        }
                    }
                }
            }
        }
        post {
            success {
                script {
                    if (skippedProjects) {
                        printOpen("Ejecución satisfactoria del pipeline, pero con algunos proyectos con errores:", EchoLevel.INFO)
                        skippedProjects.each { printOpen("${it}", EchoLevel.INFO) }
                    } else {
                        printOpen("Ejecución satisfactoria del pipeline", EchoLevel.INFO)
                    }
                }

            }
            failure {
                script {
                    printOpen("Falló la ejecución del pipeline", EchoLevel.ERROR)
                }
            }
            always {

                cleanWorkspace()

            }
        }
    }
}
