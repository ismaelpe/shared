import groovy.transform.Field

@Field Map pipelineParams

@Field boolean compile
@Field boolean execute

@Field String resourcesReportArtifactoryUrl

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    compile = params.compile.toString().toBoolean()
    execute = params.execute.toString().toBoolean()

    morningCheckArtifactoryUrl = "https://artifacts.cloud.project.com/artifactory/arq-openservices-maven-releases/com/project/alm/arch/management/morning-check/1.0.0/morning-check-1.0.0.jar"

    pipeline {		
        agent {	node (almJenkinsAgent('standard')) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '7'))
            timestamps()
            timeout(time: 3, unit: 'HOURS')
        }
        environment {	
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
        }
        stages {	   
            stage('deploy-artifactory-morning-check') {
                when {
                    expression { compile }
                } 
                steps{                    
                    configFileProvider([configFile(fileId: 'alm-maven-settings', variable: 'MAVEN_SETTINGS')]) {
                        sh ("mvn -Dhttp.proxyHost=$env.proxyHost -Dhttp.proxyPort=$env.proxyPort -Dhttps.proxyHost=$env.proxyHost -Dhttps.proxyPort=$env.proxyPort -s $MAVEN_SETTINGS clean deploy -Dmaven.test.skip=true")                     
                    }                
                }
            }
            stage('downloading-morning-check') {
                when {
                    expression { !compile }
                }  
                steps{
                    sh ("curl -k --write-out '%{http_code}' --create-dirs -fsSLo ./target/morning-check-1.0.0.jar -s -X GET ${morningCheckArtifactoryUrl} --connect-timeout 90")
                }
            }		
            stage('run-morning-check-metrics') {
                when {
                    expression { execute }
                }  
                steps{                   
                    withCredentials([usernamePassword(credentialsId: 'user-morning-check', passwordVariable: 'mc_pass', usernameVariable: 'mc_user'), string(credentialsId: 'user-morning-token', variable: 'mc_token')]) {
                        echo ("Generating Reports for Metrics")
                        sh ("java -jar -Dmorning-check.credentials.username=$mc_user -Dmorning-check.credentials.password=$mc_pass -Dmorning-check.credentials.confluence_token=$mc_token -Dmorning-check.network-settings.use-proxy=true -Dmorning-check.network-settings.proxy.host=$env.proxyHost -Dmorning-check.network-settings.proxy.port=$env.proxyPort ./target/morning-check-1.0.0.jar c08openservicesmetric")
                        echo ("Done!")
                        
                        echo ("Generating Reports for Status")
                        sh ("java -jar -Dmorning-check.credentials.username=$mc_user -Dmorning-check.credentials.password=$mc_pass -Dmorning-check.credentials.confluence_token=$mc_token -Dmorning-check.network-settings.use-proxy=true -Dmorning-check.network-settings.proxy.host=$env.proxyHost -Dmorning-check.network-settings.proxy.port=$env.proxyPort ./target/morning-check-1.0.0.jar c08openservicesstatus")
                        echo ("Done!")					
                    }                   
                }
            }
        }
    }
}
