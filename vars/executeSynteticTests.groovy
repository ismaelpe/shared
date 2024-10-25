import com.project.alm.*
import com.project.alm.SyntheticTestStructure
import java.util.List
import java.util.ArrayList

def call(PomXmlStructure pomXml, PipelineData pipeline, def result) {
	def appsList=new ArrayList<SyntheticTestStructure>()
    printOpen("Empezamos los test sinteticos", EchoLevel.INFO)
	SyntheticTestStructure syntheticTestStructure = new SyntheticTestStructure()
	appsList.add(syntheticTestStructure)
	
	syntheticTestStructure.appName = pomXml.artifactName
	
	if (pomXml.isArchProject()) {
		syntheticTestStructure.urlIcp = "https://k8sgateway.${pipeline.deployStructure.env.toLowerCase()}.icp-1.absis.cloud.lacaixa.es/arch-service/${pomXml.getBmxAppId()}"
	}else {
		syntheticTestStructure.urlIcp = "https://k8sgateway.${pipeline.deployStructure.env.toLowerCase()}.icp-1.absis.cloud.lacaixa.es/${pomXml.getBmxAppId()}"
	}
    if (pomXml.artifactSubType == ArtifactSubType.MICRO_APP){
        syntheticTestStructure.isArchMicro = true
    }
	syntheticTestStructure.resultOK = true
	
	executeSynteticTests(appsList)
	
	result.doesntExists=false
	result.resultOK=true
	
	for (SyntheticTestStructure item : appsList) {
		if (item.doesnExist) {
			 result.doesntExists=true
		}
		if (!item.resultOK) {
			result.resultOK=false
		}			
	}
	
}

def call(def appsList) {
	//the goal in this 'for' is discard all endpoints that we cannot test : item.resultOK = false
	for (SyntheticTestStructure item : appsList) {
        printOpen("HTTP URL ${item.urlIcp}", EchoLevel.DEBUG)
		def url = item.urlIcp + "/actuator/info"
		def response = null

		try {
			
			def urlParameters=[:]
			urlParameters.needsProxy=true
			urlParameters.url="${url}"
			urlParameters.parseResponse=true
			urlParameters.inputData=item
			
			response = httpRequestUtils.send(urlParameters)
			//response = httpRequest url: "${url}", httpProxy: "${env.https_proxy}"
		} catch (Exception e) {
            printOpen("Error ${e}", EchoLevel.ERROR)
			item.errorMessage = 'error maybe is not alive'
			item.resultOK = false
		}
		if (!item.resultOK) {
			continue
		}
		try {
			def json = response.content
			item.pomVersion = "${json.build.version}"
			item.pomArtifactId = "${json.build.artifact}"
			item.pomGroup = "${json.build.group}"
			
		} catch (Exception e) {
			item.errorMessage = 'error parsing /actuator/info maybe not enough info'
			item.resultOK = false
		}


	}

    def nexus = new NexusUtils(this)

	boolean exists = true
    boolean testsJarExists = true

	for (SyntheticTestStructure item : appsList) {
		if (item.resultOK) {
			
			StringBuilder loggerTest = new StringBuilder()
			loggerTest.append("**** EXECUTE INTEGRATION TEST FOR:\n")
			loggerTest.append("- artifactId:${item.pomArtifactId}\n")
			loggerTest.append("- version:${item.pomVersion}\n")
			loggerTest.append("- enpoint:${item.urlIcp}\n")
            printOpen(loggerTest.toString(), EchoLevel.DEBUG)


			testsJarExists = nexus.exists("${item.pomGroup}:${item.pomArtifactId}:jar:tests:${item.pomVersion}")
				
			if (testsJarExists) {
				//se vuelve a generar el pom en el disco porque fue machacado por el for del stage anterior
				generateSyntheticTestPom(item)
	
				String pathToSyntheticTestPom = item.pathToSyntheticTestPom
				String url = item.urlIcp
				String goal = "verify"

				try {
					configFileProvider([configFile(fileId: 'alm-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
						//ICP
                        def cmd = ""
                        if (item.isArchMicro) {
                            cmd = "mvn  -s $MAVEN_SETTINGS ${GlobalVars.GLOBAL_MVN_PARAMS} -f ${pathToSyntheticTestPom}  ${goal} -Dmicro-url=${item.urlIcp} -Dskip-it=false -Dskip-ut=true"
                        } else {
                            cmd = "mvn  -s $MAVEN_SETTINGS ${GlobalVars.GLOBAL_MVN_PARAMS} -f ${pathToSyntheticTestPom}  ${goal} -Dmicro-url=${item.urlIcp} -Dskip-it=false -Dskip-ut=true -P micro-app"
                        }

                        runMavenCommand(cmd)
                    }
				} catch (Exception e) {
					item.errorMessage = 'mvn verify finish with errors'
					item.resultOK = false
                    printOpen("Error en el verify ${e}", EchoLevel.DEBUG)
				}
	
			} else {
				item.doesnExist=true
                printOpen("El artifact de tests no se encontró en artifactory! La ejecución de test sintéticos no se realizará.", EchoLevel.ERROR)
			}
		}

	}

}
