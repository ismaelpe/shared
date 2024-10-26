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

    resourcesReportArtifactoryUrl = "https://artifacts.cloud.project.com/artifactory/arq-openservices-maven-releases/com/project/alm/arch/management/resources-report/1.0.0/resources-report-1.0.0.jar"

    pipeline {		
        agent {	node (almJenkinsAgent('standard')) }
        
        //Environment sobre el qual se ejecuta este tipo de job
        options {
            buildDiscarder(logRotator(numToKeepStr: '7'))
            timestamps()
            timeout(time: 3, unit: 'HOURS')
        }
        environment {		
            proxyHost = "proxyserv.svb.digitalscale.es"
            proxyPort = "8080"
            CONFLUENCE = credentials('confluence_token')
            GRAFANA = credentials('grafana')
        }
        stages {	   
            stage('deploy-artifactory-resources-report') {
                when {
                    expression { compile }
                } 
                steps{			   
                    script {
                        configFileProvider([configFile(fileId: 'alm-maven-settings', variable: 'MAVEN_SETTINGS')]) {
                            sh ("mvn -Dhttp.nonProxyHosts=localhost -Dhttps.nonProxyHosts=localhost -Dhttp.proxyHost=$env.proxyHost -Dhttp.proxyPort=$env.proxyPort -Dhttps.proxyHost=$env.proxyHost -Dhttps.proxyPort=$env.proxyPort -s $MAVEN_SETTINGS clean deploy -Dmaven.test.skip=true")
                        }                    
                    }
                }
            }
            stage('downloading-resources-report') {
                when {
                    expression { !compile }
                }  
                steps{
                    sh ("curl -k --write-out '%{http_code}' --create-dirs -fsSLo ./target/resources-report-1.0.0.jar -s -X GET $resourcesReportArtifactoryUrl --connect-timeout 90")                  
                }
            }		
            stage('run-resources-report-pre') {
                when {
                    expression { execute }
                }  
                steps{                    
                    echo ("Generating Reports for Metrics")
                    sh ("java -jar -Dspring.profiles.active=pre -Dconfluence.token=$env.CONFLUENCE -Dgrafana.user=$env.GRAFANA_USR -Dgrafana.password=$env.GRAFANA_PSW -Dresources-report.network-settings.use-proxy=true -Dresources-report.network-settings.proxy.host=$env.proxyHost -Dresources-report.network-settings.proxy.port=$env.proxyPort ./target/resources-report-1.0.0.jar cpu-mem-report")
                    echo ("Done!")                    
                }
            }
            stage('run-resources-report-pro') {
                when {
                    expression { execute }
                }
                steps{
                    echo ("Generating Reports for Metrics")
                    sh ("java -jar -Dspring.profiles.active=pro -Dconfluence.token=$env.CONFLUENCE -Dgrafana.user=$env.GRAFANA_USR -Dgrafana.password=$env.GRAFANA_PSW -Dresources-report.network-settings.use-proxy=true -Dresources-report.network-settings.proxy.host=$env.proxyHost -Dresources-report.network-settings.proxy.port=$env.proxyPort ./target/resources-report-1.0.0.jar cpu-mem-report")
                    echo ("Done!")                
                }
            }
        }
    }
}
