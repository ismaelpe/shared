import com.project.alm.*

def call(PomXmlStructure pomXmlStructure,PipelineData pipelineData,Map pipelineParams,BranchStructure branchStructure ) {
	
	sendStageStartToGPL(pomXmlStructure, pipelineData, "100")
	
    //currentBuild.displayName = "Build_${env.BUILD_ID}_" + pipelineData.getPipelineBuildName()
    debugInfo(pipelineParams, pomXmlStructure, pipelineData)

    try {
        //pomXmlStructure.validateArtifact()
        printOpen("The environment is ${pipelineData.bmxStructure.environment}", EchoLevel.DEBUG)
        if (pipelineData.branchStructure.branchType == BranchType.HOTFIX) {
            printOpen("Hotfix branch. Architecture version not validated.", EchoLevel.INFO)
        } else {
            printOpen("Validating architecture version ${pomXmlStructure.archVersion}...", EchoLevel.INFO)
            pomXmlStructure.validateArtifact(pipelineData.bmxStructure.environment)
            printOpen("Architecture version is OK", EchoLevel.INFO)
        }
        printOpen("Check vars:", EchoLevel.ALL)
        printOpen("${CicsVars.AGILEWORKS_VALIDATION_ENABLED} | ${pipelineData.branchStructure.branchType} | ${pipelineData.isBpiRepo()} | ${pipelineData.isBpiArchRepo()}", EchoLevel.ALL)
        if (CicsVars.AGILEWORKS_VALIDATION_ENABLED && pipelineData.branchStructure.branchType == BranchType.FEATURE && (pipelineData.isBpiRepo() || pipelineData.isBpiArchRepo())) {
            if (!pipelineData.getExecutionMode().skipAgileworksValidation()) {
                sendAgileWorkAuthFeatureToGPL(pomXmlStructure, pipelineData, GlobalVars.DEV_ENVIRONMENT, null, "${branchStructure.featureNumber}", pipelineData.getPushUserEmail())
            } else {
                printOpen("AgileWork skipped by Execution Profile: ${pipelineData.executionProfileName}", EchoLevel.ALL)
            }
        } else {
            printOpen("Not a BPI artifact. AgileWork will not be checked.", EchoLevel.ALL)
        }
    } catch (Exception e) {
        printOpen("${e.getMessage()}", EchoLevel.ERROR)
        sendStageEndToGPL(pomXmlStructure, pipelineData, "100", null, null, "error")
        throw e
    }

    if (pomXmlStructure.isRelease() && (pipelineData.branchStructure.branchType == BranchType.RELEASE || pipelineData.branchStructure.branchType == BranchType.HOTFIX)) {
        //Es una MR de un cierre de release. No puede ser otra cosa
        //No se debe hacer deploy de ningun modo
        pipelineData.deployFlag = false
    }

    if (pomXmlStructure.isSNAPSHOT() && (pipelineData.branchStructure.branchType == BranchType.RELEASE || pipelineData.branchStructure.branchType == BranchType.HOTFIX)) {

        pipelineData.deployFlag = false
        pipelineData.isRebaseOfARelease = true
        pipelineData.pipelineStructure.resultPipelineData = new FeatureResultPipelineData(GlobalVars.TST_ENVIRONMENT, pipelineData.gitUrl, pipelineData.gitProject, false)
        pipelineData.pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(pipelineParams)

    } else {
        try {
            validateThatUserHasNotChangedArtifactVersions(pipelineData, pomXmlStructure)
            validateBranch(pomXmlStructure.getArtifactVersionWithoutQualifier(), pipelineData.branchStructure)
            if (CicsVars.AGILEWORKS_VALIDATION_ENABLED &&
                pipelineData.branchStructure.branchType == BranchType.FEATURE &&
                (pipelineData.isBpiRepo() || pipelineData.isBpiArchRepo())) {
                if (!pipelineData.getExecutionMode().skipAgileworksValidation()) {
                    sendAgileWorkAuthFeatureToGPL(pomXmlStructure, pipelineData, GlobalVars.DEV_ENVIRONMENT, null, "${branchStructure.featureNumber}", pipelineData.getPushUserEmail())
                } else {
                    printOpen("AgileWork skipped by Execution Profile: ${pipelineData.executionProfileName}", EchoLevel.ALL)
                }
            } else {
                printOpen("Not a BPI artifact. AgileWork will not be checked.", EchoLevel.ALL)
            }

            new GitUtils(this).checkNoFilesWithChangesPendingToBeMerged()

            //INIT AND DEPLOY
            initCloudDeploy(pomXmlStructure, pipelineData)
        } catch (Exception e) {
            printOpen("${e.getMessage()}", EchoLevel.ERROR)
            sendStageEndToGPL(pomXmlStructure, pipelineData, "100", null, null, "error")
            throw e
        }
    }

    /* MGV: Pendiente de comentar si esto es necesario o no
    if (!pomXmlStructure.isSNAPSHOT() && pipelineData.branchStructure.branchType==BranchType.MASTER) {
        throw new RuntimeException("En rama master solo se permiten versiones SNAPSHOT");
    }
    */

    if (pomXmlStructure.isMicro()) {

        validateThatSecretsBeingUsedBelongJustToThisApp(pomXmlStructure, pipelineData)

    }

    sendStageEndToGPL(pomXmlStructure, pipelineData, "100")
	
}

def validateThatSecretsBeingUsedBelongJustToThisApp(PomXmlStructure pomXmlStructure, PipelineData pipelineData) {

    //Comprobar que el secreto que usa es solo de esa aplicación

    if("true".equals(GlobalVars.ALM_SERVICES_SKIP_VALIDATION_SECRETS_ALL) || isInSecretsExclusionList(pomXmlStructure.artifactName)) {
        printOpen("Skipping secrets validation for ${pomXmlStructure.artifactName}", EchoLevel.INFO)
    } else {
        printOpen("Validating secrets... artifactId: ${pomXmlStructure.artifactName} version: ${pomXmlStructure.artifactVersion}", EchoLevel.INFO)

        String pathToOriginalManifest = pomXmlStructure.getRouteToManifest();

        def manifestOfApp

        try {

            manifestOfApp = sh(returnStdout: true, script: "cat ${pathToOriginalManifest}")

        } catch (Exception e) {

            String message = "Parece que hubo un error leyendo manifest.yml. ¿Es posible que no esté en el root del proyecto?"
            printOpen(message, EchoLevel.ERROR)
            sendStageEndToGPL(pomXmlStructure, pipelineData, "100", null, null, "error")
            error "${message}"

        }

        boolean servicesFound = false

        String aplicacion = MavenUtils.sanitizeArtifactName(pomXmlStructure.artifactName, pipelineData.garArtifactType).toLowerCase()
        String domain = pipelineData.domain.toLowerCase()

        manifestOfApp.tokenize('\n').each {
            if (it.contains('services:')) {

                servicesFound = true

            } else if (servicesFound && it.trim().startsWith("- cbk-apps")) {

                String[] secretFragments = it.trim().split("-");

                if (secretFragments.length > 4){

                    String domainOnSecret = secretFragments[3].toLowerCase()
                    String appOnSecret = secretFragments[4].toLowerCase()

                    if (domain != domainOnSecret || aplicacion != appOnSecret) {

                        String message = "Error validacion secretos sobre "+ domain + " - " + aplicacion + " pero usa: " + domainOnSecret +" - "+ appOnSecret
                        printOpen(message, EchoLevel.ERROR)
                        sendStageEndToGPL(pomXmlStructure, pipelineData, "100", null, null, "error")
                        error "${message}"

                    }

                }

            } else if (servicesFound && it.trim().startsWith("- client-cert")) {
                String[] secretFragments = it.trim().split("-");

                if (secretFragments.length > 3){
                    String appOnSecret = secretFragments[3].toLowerCase()

                    if (aplicacion != appOnSecret) {

                        String message = "Error validacion certificado sobre "+ aplicacion + " pero usa: " + appOnSecret
                        printOpen(message, EchoLevel.ERROR)
                        sendStageEndToGPL(pomXmlStructure, pipelineData, "100", null, null, "error")
                        error "${message}"

                    }
                }
            }
        }
        
        printOpen("The secrets have been validated.", EchoLevel.INFO)

    }

}

def isInSecretsExclusionList(String component) {
	printOpen("List of skipped components", EchoLevel.ALL)
	def exclusionList = GlobalVars.ALM_SERVICES_SKIP_VALIDATION_SECRETS_LIST.split(";")
	return Arrays.asList(exclusionList).contains(component)
}
