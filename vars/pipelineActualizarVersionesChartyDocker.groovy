import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.CloudApiResponse

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
		agent {	node (almJenkinsAgent(pipelineParams)) }
        options {
            buildDiscarder(logRotator(numToKeepStr: '50'))
            timestamps()
			timeout(time: 2, unit: 'HOURS')
        }
        environment {
            AppPortal = credentials('IDECUA-JENKINS-USER-TOKEN')
			JNKMSV = credentials('JNKMSV-USER-TOKEN')
            Cloud_CERT = credentials('cloud-alm-pro-cert')
            Cloud_PASS = credentials('cloud-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyDigitalscale}"
			https_proxy = "${GlobalVars.proxyDigitalscale}"
            proxyHost = "${GlobalVars.proxyDigitalscaleHost}"
            proxyPort = "${GlobalVars.proxyDigitalscalePort}"
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

    CloudApiResponse appApps = cloudApplicationApi.getCloudAppInfo("AB3APP")
    CloudApiResponse archApps = cloudApplicationApi.getCloudAppInfo("AB3COR")

    boolean appCallWasOk = appApps.statusCode == 200
    boolean archCallWasOk = archApps.statusCode == 200

    def filteredAppApps = cloudApplicationApi.filterApps(appApps.body, appsToBeUpdatedArray, dockerVersionsBlacklist)


    if (appCallWasOk && showAllApps) {

        cloudApplicationApi.dumpAppsMetadataToLog(filteredAppApps, "AB3APP apps")

    }

    def filteredArchApps = cloudApplicationApi.filterApps(archApps.body, appsToBeUpdatedArray, dockerVersionsBlacklist)

    if (archCallWasOk && showAllApps) {

        cloudApplicationApi.dumpAppsMetadataToLog(filteredArchApps, "AB3COR apps")

    }
    
    printOpen("The filtered apps are ${filteredAppApps} ${appCallWasOk}", EchoLevel.INFO)
    
    printOpen("The filtered arch apps are ${filteredArchApps} ${archCallWasOk}", EchoLevel.INFO)
    

    if (appCallWasOk) {

        cloudApplicationApi.updateChartAndDockerVersionsOrShowEligibleForUpdate(filteredAppApps, "AB3APP", chartVer, dockerVersion, updateVersion)

    }

    if (archCallWasOk) {

        cloudApplicationApi.updateChartAndDockerVersionsOrShowEligibleForUpdate(filteredArchApps, "AB3COR", chartVer, dockerVersion, updateVersion)

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
