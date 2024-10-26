import com.project.alm.*
import groovy.json.JsonSlurperClassic

def call(PipelineData pipelineData, PomXmlStructure pomXml) {
    def artifactName = pomXml.artifactName
    def contractVersion = pomXml.contractVersion
    def type = pipelineData.garArtifactType.getGarName()
    def environment = getEnvironment(pipelineData.bmxStructure.environment)
    def microVersion = getVersionWithoutSuffix(pomXml.artifactVersion)

    if (environment != "dev" && environment != "eden") {
        printOpen("ArchApiManagementLifecycle promote contract for '$artifactName':\nasset_version:\t$contractVersion\nenvironment:\t$environment\ntype:\t$type\nmicro_version:\t$microVersion", EchoLevel.INFO)
        
        def muleParams = new JsonSlurperClassic().parseText(env.ALM_MULE_PROPERTIES)
        
        def securityArtifact = getSecurityArtifact(muleParams)

        printOpen ("Mule URL: $securityArtifact.url", EchoLevel.DEBUG)
        
        def urlContractPromote = "$securityArtifact.url/promote/$artifactName/$contractVersion/${environment.toUpperCase()}?type=$type&microVersion=$microVersion"
  
        try {
            def response = sendRequestToService('POST', urlContractPromote, null, null, [
                parseResponse: true,
                hideCommand: true,
                retryLoopTimeout: muleParams.muleConnectRetries,
                customHeaders: [
                    [name: 'Authorization', value: "Bearer $securityArtifact.token"],
                    [name: 'HTTP_HEADER_ACTOR', value: securityArtifact.actor]
                ],
                consoleLogResponseBody: true,
                validResponseCodes: "100:299",
                timeout: muleParams.muleConnectTimeOutMs
            ])

            printOpen("ArchApiManagementLifecycle response: $response.content with $response.statusCode", EchoLevel.INFO)
            
            return null
        } catch (Exception e) {      
            def error = "Ha ocurrido un error al hacer el promote a ArchApiManagementLifecycle:\n\n$e"

            printOpen(error, EchoLevel.ERROR)

            if (shouldWeAbortThePipe()) {
                printOpen("Abortaremos la pipeline por error al hacer el 'promote' del contrato en ArchApiManagementLifecycle", EchoLevel.ERROR)
                throw e
            }

            def message = "Ignoramos el error y seguimos adelante.\nInforme al API Team para que promocione su contrato, indicando entorno '$environment' y version del contrato '$contractVersion'"             
            
            printOpen(message, EchoLevel.INFO)

            return "$error\n$message"
        }
    } else {
        printOpen("ArchApiManagementLifecycle dont call promote contracts for dev or eden. The promote is applied after tst (inclusive)", EchoLevel.INFO)
        
        return null
    }
}

def getSecurityArtifact(muleParams) {
    printOpen("Downloading 'security artifact'...", EchoLevel.INFO)

    def securityArtifactUrl = env.ALM_MULE_SECURITY_ARTIFACT_URL
    
    if (securityArtifactUrl) {
        withCredentials([usernamePassword(credentialsId: 'cloudcoucxba3msje01', passwordVariable: 'artifactPass', usernameVariable: 'artifactUser')]) {
            def securityArtifactZipDir = "securityArtifact"
            def securityArtifactZipFileName = "securityArtifact.zip"
            securityArtifactUrl = securityArtifactUrl.replaceAll("\\{ENV\\}", muleParams.muleSecurityVersion.toUpperCase())
            printOpen("Security Artifact: $securityArtifactUrl")
            def command = buildCurlCommand("GET", securityArtifactUrl, artifactUser, artifactPass, muleParams.muleConnectTimeOutMs, muleParams.muleReadTimeOutMs, securityArtifactZipFileName)

            def response = checkEndpoint.executeCurlWithRetries(command, [
                maxRetries: muleParams.muleConnectRetries,
                kpiAlmEvent: new KpiAlmEvent(
                    null, null,
                    KpiAlmEventStage.GENERAL,
                    KpiAlmEventOperation.MULEAPI_HTTP_CURL, null, null, null)
            ])

            if (response.status == '200') {	
                try {
                    sh(returnStdout: false, script: "unzip -o $securityArtifactZipFileName -d $securityArtifactZipDir")

                    def contentArtifactSecurity = sh(returnStdout: true, script: "cat $securityArtifactZipDir/token.jwt").split("\n")
                    
                    printOpen("Downloaded 'security artifact' OK!", EchoLevel.INFO)

                    return [
                        url: contentArtifactSecurity[0],
                        actor: contentArtifactSecurity[1],
                        token: contentArtifactSecurity[2]
                    ]
                } catch (Exception exception) {
                    printOpen("The file $zipFileName is empty, nothing to do!", EchoLevel.INFO)
                } finally {
                    sh "rm -rf $securityArtifactZipDir"
                    sh "rm -f $securityArtifactZipFileName"	

                    printOpen("$securityArtifactZipDir, $securityArtifactZipFileName deleted!!", EchoLevel.DEBUG)
                }		
            } else {
                throw new Exception("Fail download security artifact")
            }
        }
    } else {
        throw new Exception("Check jenkins environment, 'ALM_MULE_SECURITY_ARTIFACT_URL' is not defined!")
    }
}

def buildCurlCommand(method, url, user, pass, connectTimeOut, readTimeOut, filename) {
    return "curl -x http://$env.proxyHost:$env.proxyPort -k --write-out '%{http_code}' -o $filename -s -X $method $url --connect-timeout $connectTimeOut --user $user:$pass"
}

def shouldWeAbortThePipe() {
	boolean abortThePipe = Utilities.getBooleanPropertyOrDefault(env.ALM_MULE_ARCHAPIMANAGEMENTLIFECYCLE_IF_ERROR_THEN_PIPELINE_FAILS, true)
    printOpen("Looking if we have to abort the pipe: ${abortThePipe}", EchoLevel.INFO)
	return abortThePipe
}

def getVersionWithoutSuffix(String version) {
    printOpen("Deleting version suffix to call promote endpoint", EchoLevel.INFO)
    return version.split("-")[0]   
}

def getEnvironment(String environment) {
    if (environment.equalsIgnoreCase("eden")) {
        return "dev"
    } else {
        return environment.toLowerCase()
    }
}