import com.project.alm.GlobalVars

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call() {
    pipeline {		
        agent {	node (almJenkinsAgent('standard')) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '7'))
            timestamps()
            timeout(time: 3, unit: 'HOURS')
        }
        environment {	
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
        }
        stages {	   
            stage('create-artifact') {
                steps{
                    createArtifact()
                }
            }            
            stage('generate-token-file') {
                steps{
                    generateTokenFile()
                }
            }
            stage('build-artifact') {
                steps{
                    buildArtifact()
                }
            }
            stage('deploy-to-artifactory') {
                steps{
                    deployToArtifactory()
                }
            }            
        }
        post {
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
 * Stage 'createArtifact'
 */
def createArtifact() {
    configFileProvider([configFile(fileId: 'alm-maven-settings', variable: 'MAVEN_SETTINGS')]) {
        sh ("mvn --no-transfer-progress -Dhttp.proxyHost=$env.proxyHost -Dhttp.proxyPort=$env.proxyPort -Dhttps.proxyHost=$env.proxyHost -Dhttps.proxyPort=$env.proxyPort -s $MAVEN_SETTINGS archetype:generate -DgroupId=$params.groupId -DartifactId=$params.artifactId -Dversion=$params.version -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false")
    }   
}

/**
 * Stage 'generateTokenFile'
 */
def generateTokenFile() {
    def resourcesPath = "$params.artifactId/src/main/resources"
    sh ("rm -rf $params.artifactId/src/main/java")
    sh ("mkdir -p $resourcesPath")
    writeFile file: "$resourcesPath/token.jwt", text: params.token   
}

/**
 * Stage buildArtifact
 */
def buildArtifact() {
    configFileProvider([configFile(fileId: 'alm-maven-settings', variable: 'MAVEN_SETTINGS')]) {
        sh ("cd $params.artifactId && mvn --no-transfer-progress -Dhttp.proxyHost=$env.proxyHost -Dhttp.proxyPort=$env.proxyPort -Dhttps.proxyHost=$env.proxyHost -Dhttps.proxyPort=$env.proxyPort -s $MAVEN_SETTINGS clean install -Dmaven.test.skip=true")                     
    }   
}

/**
 * Stage deployToArtifactory
 */
def deployToArtifactory() {
    configFileProvider([configFile(fileId: 'alm-maven-settings', variable: 'MAVEN_SETTINGS')]) {
        sh ("mvn --no-transfer-progress -Dhttp.proxyHost=$env.proxyHost -Dhttp.proxyPort=$env.proxyPort -Dhttps.proxyHost=$env.proxyHost -Dhttps.proxyPort=$env.proxyPort -s $MAVEN_SETTINGS deploy:deploy-file -Durl=$GlobalVars.MVN_RELEASE_DEPLOYMENT_REPO_URL -DrepositoryId=$GlobalVars.MVN_RELEASE_DEPLOYMENT_REPO_NAME -Dfile=$params.artifactId/target/$params.artifactId-${params.version}.jar -DpomFile=$params.artifactId/pom.xml -DgeneratePom=false")                     
    }   
}

/**
 * Stage endPipelineAlwaysStep
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}
