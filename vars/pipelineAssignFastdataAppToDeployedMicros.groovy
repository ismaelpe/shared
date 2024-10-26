import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars

@Field Map pipelineParams

@Field boolean successPipeline

@Field String configEnvironment
@Field boolean updateCatalog
@Field String catalogEnvironment

@Field String catalogUrl

@Field appsFromCatalog

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo

    successPipeline = true
	appsFromCatalog = [:]
	
	configEnvironment = params.configEnvironment
	updateCatalog = params.updateCatalog
	catalogEnvironment = params.catalogEnvironment
    
    pipeline {		
		agent {	node (almJenkinsAgent(pipelineParams)) }
        options {
			buildDiscarder(logRotator(numToKeepStr: '0'))
			timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
        }
		stages {
			stage("get-config-git-repo") {
				steps {					
                     getConfigGitRepoStep()
				}
			}
			stage("process-apps") {
				steps {
					 processAppsStep()
				}
			}
		}
        post {
            success {
                endPipelineSuccessStep()
            }
            failure {
                endPipelineFailureStep()
            }
            always {
                endPipelineAlwaysStep()
            }        
        }
	}
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Stage 'getConfigGitRepoStep'
 */
def getConfigGitRepoStep() {			
    printOpen("Downloading application configurations from ${configEnvironment} environment", EchoLevel.INFO)
	
	def gitRepoUrl = null
	try {
		if (GlobalVars.TST_ENVIRONMENT == configEnvironment) {
	        gitRepoUrl = GlobalVars.GIT_CONFIG_REPO_URL_TST
	    } else if (GlobalVars.PRE_ENVIRONMENT == configEnvironment) {
			gitRepoUrl = GlobalVars.GIT_CONFIG_REPO_URL_PRE
	    } else if (GlobalVars.PRO_ENVIRONMENT == configEnvironment) {
			throw new Exception("PRO environment not yet supported")
	        //gitRepoUrl = GlobalVars.GIT_CONFIG_REPO_URL_PRO
	    } else {
			throw new Exception("${configEnvironment} is not a valid Configuration Environment param.")
		}
		
		if(gitRepoUrl == null) {
			throw new Exception("${configEnvironment} is not a supported environment")
		}
		
		printOpen("The ${configEnvironment} config git repo URL is ${gitRepoUrl}", EchoLevel.DEBUG)
		printOpen("We have to clone from branch master ", EchoLevel.DEBUG)
		
		GitRepositoryHandler git = new GitRepositoryHandler(this, gitRepoUrl, [gitProjectRelativePath: '.', checkoutBranch: 'master'])
		git.pullOrClone()
		
	} catch (Exception e) {
		printOpen("Error getting config server repository", EchoLevel.ERROR)
		printOpen(e.getMessage(), EchoLevel.ERROR)
		
		def sw = new StringWriter()
		def pw = new PrintWriter(sw)
		e.printStackTrace(pw)
		printOpen(sw.toString(), EchoLevel.ERROR)
		
		throw e
	}
	
	printOpen("Downloaded application configurations from ${configEnvironment} environment", EchoLevel.INFO)
}

/**
 * Stage 'processAppsStep'
 */
def processAppsStep() {
	printOpen("Processing applications from ${configEnvironment} environment. Register into ${catalogEnvironment} Catalog: ${updateCatalog}", EchoLevel.INFO)
	
	try {
		if (GlobalVars.TST_ENVIRONMENT == catalogEnvironment) {
			catalogUrl = "${GlobalVars.URL_CATALOGO_ALM_TST}"
		} else if (GlobalVars.PRE_ENVIRONMENT == catalogEnvironment) {
			catalogUrl = "${GlobalVars.URL_CATALOGO_ALM_PRE}"
		} else if (GlobalVars.PRO_ENVIRONMENT == catalogEnvironment) {
			throw new Exception("PRO environment not yet supported")
			//catalogUrl = "${GlobalVars.URL_CATALOGO_ALM_PRO}"
		} else {
			throw new Exception("${catalogEnvironment} is not a valid Catalog Environment param")
		}
		
		getListAllAppsFromCatalog()
		
		def appList = sh(returnStdout: true, script: "ls -d ${workspace}/services/apps/*-micro*")
		
		printOpen("Application list:\n ${appList}", EchoLevel.DEBUG)
		
		String reportRelationshipsFound = ""
		String reportAppsNotFound = ""
		String reportInvalidDirectories = ""
		
		def lines = appList.split( '\n' )
		for(line in lines) {
			printOpen("Processing application: ${line}", EchoLevel.DEBUG)
			
			def appType = null
			def garApp = null
			def kafkaConnect = false
			def fastdataApp = null
			
			def appRegexStr = /^.*\/([a-zA-Z0-9\-_]{1,32}-micro)\-(server-)?[0-9]+$/
			
			def matches = (line =~ appRegexStr)
			if(matches) {
				def appName = matches[0][1]
				
				def appFound = getAppFromCatalog(appName)
				if(appFound != null) {
					appType = appFound.appType
					garApp = appFound.garApp
					
					printOpen("Application ${appType}.${garApp} found. Searching related FastData app...", EchoLevel.DEBUG)
						
					fastdataApp = extractFastdataRelatedApp(line, configEnvironment)
						
					if(fastdataApp != null) {
						printOpen("Relationship found: ${appType}.${garApp} --> ${fastdataApp}", EchoLevel.DEBUG)
						reportRelationshipsFound = reportRelationshipsFound + "\t- ${appType}.${garApp} --> ${fastdataApp}\n"
							
						if(updateCatalog) {
							updateFastdataAppInCatalog(appType, garApp, fastdataApp)
						}
					} else {
						printOpen("Microservice ${appType}.${garApp} has no relationship with any FastData Application", EchoLevel.DEBUG)
					}
				} else {
					printOpen("Application ${appName} is not a valid microservice registered in Catalog", EchoLevel.DEBUG)
					reportAppsNotFound = reportAppsNotFound + "\t- ${appName}\n"
				}
			} else {
				printOpen("Directory ${line} is not a valid microservice configuration directory", EchoLevel.DEBUG)
				reportInvalidDirectories = reportInvalidDirectories + "\t- ${line}\n"
			}
		}
		
		printOpen("Relationships found:\n${reportRelationshipsFound}", EchoLevel.INFO)
		if(!reportAppsNotFound.isEmpty()) {
			printOpen("Apps not found in Catalog:\n${reportAppsNotFound}", EchoLevel.ERROR)
		}
		if(!reportInvalidDirectories.isEmpty()) {
			printOpen("Config directories invalid:\n${reportInvalidDirectories}", EchoLevel.ERROR)
		}
		
	} catch (Exception e) {
		printOpen("Error processing apps", EchoLevel.ERROR)
		printOpen(e.getMessage(), EchoLevel.ERROR)
		
		def sw = new StringWriter()
		def pw = new PrintWriter(sw)
		e.printStackTrace(pw)
		printOpen(sw.toString(), EchoLevel.ERROR)
		
		throw e
	}
	
	printOpen("Applications from ${configEnvironment} environment processed. Registered into ${catalogEnvironment} Catalog: ${updateCatalog}", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("Is pipeline successful? ${successPipeline}", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("Is pipeline unsuccessful? ${successPipeline}", EchoLevel.ERROR) 
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}

String getAppFromCatalog(String appName) {
	
	def appFound = null
	
	def garApp = appsFromCatalog["SRV.DS.${appName}"]
	if(garApp != null) {
		appFound = [appType: "SRV.DS", garApp: garApp]
	} else {
		garApp = appsFromCatalog["SRV.MS.${appName}"]
		if(garApp != null) {
			appFound = [appType: "SRV.MS", garApp: garApp]
		} else {
			garApp = appsFromCatalog["ARQ.MIA.${appName}"]
			if(garApp != null) {
				appFound = [appType: "ARQ.MIA", garApp: garApp]
			}
		}
	}
	
	return appFound
}

def getListAllAppsFromCatalog() {
	def env = catalogEnvironment.toUpperCase()
	def response = sendRequestToAlm3MS(
		'GET',
		"${catalogUrl}/app/${env}",
		null,
		env,
		[:])
	
	if (response.status == 200) {
		for(app in response.content) {
			appsFromCatalog["${app.appType}.${app.appName}"] = "${app.garApp}"
		}
	} else {
		throw new Exception("Error getting applications from Catalog. Status code: ${response.content}", EchoLevel.INFO)
	}
	
	printOpen("Apps in Catalog: ${appsFromCatalog}", EchoLevel.DEBUG)
}

Map extractFastdataRelatedApp(String dir, String configEnvironment) {
	def fastdataApp = null
	
	fastdataApp = extractFastdatadAppFromConfigFile("${dir}/application.yml")
	if(fastdataApp == null) {
		fastdataApp = extractFastdatadAppFromConfigFile("${dir}/application-${configEnvironment}.yml")
		if(fastdataApp == null) {
			fastdataApp = extractFastdatadAppFromConfigFile("${dir}/application-cloud.yml")
		}
	}
	
	if(fastdataApp != null) {
		fastdataApp = fastdataApp.toLowerCase()
	}
	
	return fastdataApp
}

String extractFastdatadAppFromConfigFile(String filePath) {
	def fastdataApp = null
	
	if(fileExists(filePath)) {
		printOpen("Extracting FastData App from config file ${filePath}", EchoLevel.DEBUG)
		
		def fileContent = readFile(filePath)
		
		fastdataApp = extractFastdatadAppForConsumer(fileContent)
		if(fastdataApp != null) {
			printOpen("Consumer configuration found in ${filePath}. FastData app is ${fastdataApp}", EchoLevel.DEBUG)
		} else {
			fastdataApp = extractFastdatadAppForProducer(fileContent)
			
			if(fastdataApp != null) {
				printOpen("Producer configuration found in ${filePath}. FastData app is ${fastdataApp}", EchoLevel.DEBUG)
			} else {
				printOpen("No producer or consumer configuration was found in ${filePath}", EchoLevel.DEBUG)
			}
		}
	}
	
	return fastdataApp
}

String extractFastdatadAppForConsumer(String fileContent) {
	def fastdataApp = null
	
	if(fileContent != null) {
		def consumerRegexp = /(group|applicationId):\s*directo-cai-([a-zA-Z0-9]+)[-|\s]+/
		def matches = (fileContent =~ consumerRegexp)
		
		if(matches) {
			for(match in matches) {
				def appFound = match[2]
				if(fastdataApp == null) {
					fastdataApp = appFound
				} else if(fastdataApp != appFound) {
					printOpen("Two diferent FastData apps was found! ${appFound} != ${fastdataApp}", EchoLevel.ERROR)
					fastdataApp = null
					break
				} 
			}
		} else {
			printOpen("No consumer configuration was found", EchoLevel.DEBUG)
		}
	}
	
	return fastdataApp
}

String extractFastdatadAppForProducer(String fileContent) {
	def fastdataApp = null
	
	def producerRegexp = /output(.|:\s*)destination:\s*cai-([a-zA-Z0-9]+)[-|\s]+/
	def matches = (fileContent =~ producerRegexp)
	
	if(matches) {
		for(match in matches) {
			def appFound = match[2]
			if(fastdataApp == null) {
				fastdataApp = appFound
			} else if(fastdataApp != appFound) {
				printOpen("Two diferent FastData apps was found! ${appFound} != ${fastdataApp}", EchoLevel.ERROR)
				fastdataApp = null
				break
			} 
		}
	} else {
		printOpen("No producer configuration was found", EchoLevel.DEBUG)
	}
	
	return fastdataApp
}

def updateFastdataAppInCatalog(String appType, String appName, String fastdataApp) {
	def body = [id: fastdataApp]
	def response = sendRequestToAlm3MS(
		'PUT',
		"${catalogUrl}/app/${appType}/${appName}/fastdata",
		body,
		catalogEnvironment.toUpperCase(),
		[:])
	
	if (response.status == 200) {
		printOpen("Catalog in ${catalogEnvironment} environment successfully updated for application ${appType}.${appName}", EchoLevel.DEBUG)
	} else {
		throw new Exception("Error updating Catalog for application ${appType}.${appName}: ${response.content}", EchoLevel.ERROR)
	}
}
