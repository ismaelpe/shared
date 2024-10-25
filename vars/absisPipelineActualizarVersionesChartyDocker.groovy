import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.ICPApiResponse

@Field Map pipelineParams
@Field String[] technologyVersions
@Field String[] appsToBeUpdatedArray
@Field String[] dockerVersionsBlacklistStr
@Field boolean updateVersion
@Field boolean showAllApps

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters

    technologyVersions = params.technologyVersion.split("\t")
    appsToBeUpdatedArray = params.appsToBeUpdated.split(";")
    dockerVersionsBlacklistStr = params.dockerVersionBlacklist.split(";")
    updateVersion = params.updateVersions.toString().toBoolean()
    showAllApps = params.showAllAppsOnLog.toString().toBoolean()
    
    pipeline {		
		agent {	node (absisJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '50'))
            timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
			JNKMSV = credentials('JNKMSV-USER-TOKEN')
            ICP_CERT = credentials('icp-absis3-pro-cert')
            ICP_PASS = credentials('icp-absis3-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
			https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
        }
        stages {
            stage('retrieve-versions-and-update') {
                steps {
                    retrieveVersionsAndUpdateStep()
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
 * Stage 'retrieveVersionsAndUpdate'
 */
def retrieveVersionsAndUpdateStep() {

    Integer chartVer = "${technologyVersions[0]}" as Integer
    Integer dockerVersion = "${technologyVersions[2]}" as Integer

    def dockerVersionsBlacklist = []
    dockerVersionsBlacklistStr.each {
        if (it == '') return true
        dockerVersionsBlacklist.add(Integer.valueOf(it))
    }

    ICPApiResponse appApps = icpApplicationApi.getICPAppInfo("AB3APP")
    ICPApiResponse archApps = icpApplicationApi.getICPAppInfo("AB3COR")

    boolean appCallWasOk = appApps.statusCode == 200
    boolean archCallWasOk = archApps.statusCode == 200

    def filteredAppApps = icpApplicationApi.filterApps(appApps.body, appsToBeUpdatedArray, dockerVersionsBlacklist)


    if (appCallWasOk && showAllApps) {

        icpApplicationApi.dumpAppsMetadataToLog(filteredAppApps, "AB3APP apps")

    }

    def filteredArchApps = icpApplicationApi.filterApps(archApps.body, appsToBeUpdatedArray, dockerVersionsBlacklist)

    if (archCallWasOk && showAllApps) {

        icpApplicationApi.dumpAppsMetadataToLog(filteredArchApps, "AB3COR apps")

    }
    
    printOpen("The filtered apps are ${filteredAppApps} ${appCallWasOk}", EchoLevel.INFO)
    
    printOpen("The filtered arch apps are ${filteredArchApps} ${archCallWasOk}", EchoLevel.INFO)
    

    if (appCallWasOk) {

        icpApplicationApi.updateChartAndDockerVersionsOrShowEligibleForUpdate(filteredAppApps, "AB3APP", chartVer, dockerVersion, updateVersion)

    }

    if (archCallWasOk) {

        icpApplicationApi.updateChartAndDockerVersionsOrShowEligibleForUpdate(filteredArchApps, "AB3COR", chartVer, dockerVersion, updateVersion)

    }
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen("CHART_DOCKER_VERSIONS_UPDATE_STARTED realizado con exito", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen("CHART_DOCKER_VERSIONS_UPDATE_STARTED fail", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}
