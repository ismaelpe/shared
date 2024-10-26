package com.project.alm

class PipelineData {

    boolean pipelineEndsWithWarning = false

    def ancientMapInfo = [:]
    def resultDeployInfo = [:]
    def routesToNexus = null
	def clientRoutesToNexus = null

    PipelineBehavior pipelineBehavior = PipelineBehavior.LIKE_ALWAYS
    PipelineExecutionMode pipelineDataExecutionMode = new DefaultExecutionMode()
    CloudVarPipelineCopyType manualCopyExecutionMode = CloudVarPipelineCopyType.DOES_NOT_APPLY
    CloudVarPipelineCopyType manualCopyElectionOriginArtifact = CloudVarPipelineCopyType.DOES_NOT_APPLY

    String cloudPath
    String componentId
	boolean isNowDeployed = false
    boolean isRebaseOfARelease = false
    String pipelineId
    String gitAction
    String targetBranch
    String commitId = ''
    String gitUrl
    String commitLog = ""
    String originPushBranchToMaster = ""
    boolean isMultiBranchPipeline
    BranchType originPushToMaster
    boolean deployFlag
    boolean literalsFlag
    boolean undeploySampleApp = true
    boolean isPushFromMerge = false
    BranchStructure branchStructure
    PipelineStructure pipelineStructure
    GarAppType garArtifactType
    PipelineStructureType pipelineStructureType
    String gitProject
    BmxStructure bmxStructure
    DistributionModePRO distributionModePRO
	CloudDeployStructure deployStructure

    String pushUserEmail = ""
    String pushUser = ""

    String eventToPush = ""
    String buildCode = "0"

    RevapiStructure revapiStructure = new RevapiStructure()

    boolean isArchetype
    String archetypeModel

    String testData = ""

    //domain, subdomain and company will be set from git url by the initFroGirUrlGarApp method
    String domain = ""
    String subDomain = ""
    String company = ""
    String executionProfileName

    boolean deployOnCloud = false
    boolean isDataserviceWithH2InMemoryDatabase = false

    //vcapsservices ids
    Set<String> vcapsServiceIds = []

    def jenkinsFileParams = [:]

    def mvnAdditionalParameters = []

    def mvnMuleParameters = []

    String jobName = ""
    Map jobParameters = new HashMap()

    boolean onlyProductionTests = false

    String agent = ""

    PipelineData() {
        pipelineStructure = new CIPipelineStructure()
        distributionModePRO = DistributionModePRO.CANARY_ON_ALL_CENTERS
    }

    PipelineData(String agent) {
        pipelineStructure = new CIPipelineStructure()
        distributionModePRO = DistributionModePRO.CANARY_ON_ALL_CENTERS
        this.agent = agent
    }

    PipelineData(PipelineStructureType type, String pipelineInput, String jobName, Map jobParameters) {
        this(type, pipelineInput)
        this.jobName = jobName
        this.jobParameters = jobParameters
    }

    PipelineData(PipelineStructureType type, String pipelineInput, Map jobParameters) {
        this(type, pipelineInput)
        this.jobParameters = jobParameters
    }

    PipelineData(PipelineStructureType type, String pipelineInput) {

        distributionModePRO = DistributionModePRO.CANARY_ON_ALL_CENTERS

        pipelineStructureType = type
        if (pipelineInput != null) {
            pipelineId = pipelineInput.replace("/", "")
            pipelineId = pipelineId.replace("#", "")
            pipelineId = pipelineId.replace("%", "")
            pipelineId = pipelineId.replace(".", "-")
        }

		if (type == PipelineStructureType.DEPLOY_PROTOTYPE) pipelineStructure = new DeployPrototypePipelineStructure(pipelineId)
        else if (type == PipelineStructureType.CI) pipelineStructure = new CIPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.NO_CI) pipelineStructure = new CIPipelineStructure(pipelineId, "NO_CI_Pipeline")
        else if (type == PipelineStructureType.CI_CONFIGS) pipelineStructure = new CIConfigsPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.CICS_WSDL) pipelineStructure = new CicsWsdlPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.CONFIG_RELEASE) pipelineStructure = new CreateConfigReleasePipelineStructure(pipelineId)
        else if (type == PipelineStructureType.RELEASE) pipelineStructure = new CreateReleasePipelineStructure(pipelineId)
        else if (type == PipelineStructureType.RELEASE_CANDIDATE) pipelineStructure = new CreateReleaseCandidatePipelineStructure(pipelineId)
        else if (type == PipelineStructureType.RELEASE_CANDIDATE_CONFIGLIB) pipelineStructure = new CreateReleaseCandidateConfigLibPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.RELEASE_CONFIGLIB) pipelineStructure = new CreateReleaseConfigLibPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.IOP_PRO_CONFIGFIX) pipelineStructure = new IopProConfigFixPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.IOP_PRO_CONFIGLIB) pipelineStructure = new IopProConfigLibPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.IOP_PRO) pipelineStructure = new IopProPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.ROLLBACK) pipelineStructure = new RollbackPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.ROLLBACK_FINISH) pipelineStructure = new RollbackFinishPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.CLOSE) pipelineStructure = new ClosePipelineStructure(pipelineId)
        else if (type == PipelineStructureType.INC_CANNARY) pipelineStructure = new IncCannaryPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.INC_CAMPAIGN_CANNARY) pipelineStructure = new IncCampaignCannaryPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.IOP_DATASOURCES) pipelineStructure = new IncCannaryPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.HOTFIX) pipelineStructure = new HotFixPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.CONFIGFIX) pipelineStructure = new ConfigFixPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.DELETE) pipelineStructure = new DeleteComponentVersionPipelineStructure(pipelineId)
		else if (type == PipelineStructureType.BBDD_LIQUIBASE_STATUS) pipelineStructure = new BBDDLiquibaseStatusPipelineStructure(pipelineId)		
		else if (type == PipelineStructureType.BBDD_VALIDATION) pipelineStructure = new BBDDValidationPipelineStructure(pipelineId)
		else if (type == PipelineStructureType.CONFIGLIBFIX) pipelineStructure = new ConfigLibFixPipelineStructure(pipelineId)
		else if (type == PipelineStructureType.CLOSE_CAMPAIGN) pipelineStructure = new CloseCampaignPipelineStructure(pipelineId)
		else if (type == PipelineStructureType.NOTIFY_END_CAMPAIGN || type == PipelineStructureType.JOB_CLEAN_DEV_DUPLICATED_PODS) pipelineStructure = new NotifyCloseCampaignPipelineStructure(pipelineId)
		else if (type == PipelineStructureType.RELEASE_CANDIDATE_BBDD) pipelineStructure = new CreateReleaseCandidateBBDDPipelineStructure(pipelineId)
		else if (type == PipelineStructureType.DEPLOY_BBDD_SCRIPT) pipelineStructure = new DeployScriptBBDDPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.REFRESH_LITERALS) pipelineStructure = new RefreshLiteralsPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.RESTART_APP) pipelineStructure = new RestartAppPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.START_STOP) pipelineStructure = new StartAndStopPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.STRESS_TESTS_PREPARATION) pipelineStructure = new StressTestsPreparationPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.SONAR_SCAN_AND_SEND_REPORT) pipelineStructure = new SonarScanAndSendReportPipelineStructure(pipelineId)
        else if (type == PipelineStructureType.CALL_ACTUATOR_ENV) pipelineStructure = new CallActuatorEnvPipelineStructure(pipelineId)
    }

    def prepareResultData(String version, String artifact, String component, ArtifactType artifactType = null, ArtifactSubType artifactSubType = null, String branchName = null) {
        if (pipelineStructureType == PipelineStructureType.IOP_PRO || pipelineStructureType == PipelineStructureType.IOP_PRO_CONFIGFIX) {
            if (ancientMapInfo.containsKey(GlobalVars.BMX_CD1)) {
                pipelineStructure.resultPipelineData.newVersion = !ancientMapInfo.get(GlobalVars.BMX_CD1).hasAncient
            } else if (ancientMapInfo.containsKey(GlobalVars.BMX_CD2)) {
                pipelineStructure.resultPipelineData.newVersion = !ancientMapInfo.get(GlobalVars.BMX_CD2).hasAncient
            }
            pipelineStructure.resultPipelineData.nextDistributionMode =
                DistributionPROResolver.determineNextDistributionModeOnPRO(
                    distributionModePRO
                )
        } else if (pipelineStructureType == PipelineStructureType.RELEASE) {
            pipelineStructure.resultPipelineData.branchName = branchStructure.branchName
        } else if (pipelineStructureType == PipelineStructureType.INC_CANNARY) {
            pipelineStructure.resultPipelineData.branchName = branchStructure.branchName
        }
        pipelineStructure.resultPipelineData.artifact = artifact
        pipelineStructure.resultPipelineData.version = version
        pipelineStructure.resultPipelineData.component = component
        if (artifactType) pipelineStructure.resultPipelineData.artifactType = artifactType
        if (artifactSubType) pipelineStructure.resultPipelineData.artifactSubType = artifactSubType
        if (branchName) pipelineStructure.resultPipelineData.branchName = branchName
    }

    String getRouteToDeployedJar() {
        def routeToNexus = ''
        def routeToNexusPom = ''
        if (routesToNexus != null) {
            routesToNexus.each {
                if (it.contains('.jar') && !it.contains('sources.jar') && !it.contains('tests.jar') && !it.contains('jar-with-dependencies.jar')) routeToNexus = it
                else if (it.contains('.pom')) routeToNexusPom = it
            }

            if (routeToNexus == '') routeToNexus = routeToNexusPom
        }
        return routeToNexus
    }
	
	String getRouteToClientDeployedJar() {
		def routeToNexus = ''
		def routeToNexusPom = ''
		if (clientRoutesToNexus != null) {
			clientRoutesToNexus.each {
				if (it.contains('.jar') && !it.contains('sources.jar') && !it.contains('tests.jar') && !it.contains('jar-with-dependencies.jar')) routeToNexus = it
				else if (it.contains('.pom')) routeToNexusPom = it
			}

			if (routeToNexus == '') routeToNexus = routeToNexusPom
		}
		return routeToNexus
	}

    boolean isMultiBranchPipeline() {
        if (gitAction == '') return true
        else return false
    }

    boolean isPushCI() {
        if (commitLog == null) return false

        return commitLog.contains(GlobalVars.GIT_TAG_CI_PUSH) || PipelineBehavior.COMMITLOG_REQUESTED_NO_CI == pipelineBehavior
    }
	
	boolean isPushConfigFile() {
		if (commitLog == null) return false

		return commitLog.contains(GlobalVars.GIT_TAG_CONGIG_PUSH)
	}
	
	boolean isPushCreateRC() {    
        if (commitLog == null) return false

        return commitLog.contains(GlobalVars.GIT_TAG_CR_PUSH)
    }

    String initCommitAuxInfo() {

    }


    boolean isBpiRepo() {
        GitUtils.isBpiRepo(gitUrl)
    }

    boolean isBpiArchRepo() {
        GitUtils.isBpiArchRepo(gitUrl)
    }

    def getFeature(String version) {
        if (branchStructure != null && branchStructure.branchType == BranchType.MASTER && eventToPush != null && eventToPush != "" && originPushToMaster != null && originPushToMaster != BranchType.RELEASE) {
            return [
                name: eventToPush,
                type: "FEATURE"
            ]
        } else
            return null

    }

    def init(BranchStructure branch) {
        init(branch, ArtifactSubType.MICRO_APP, ArtifactType.SIMPLE)
    }

    def init(BranchStructure branch, ArtifactSubType artifactSubtype, ArtifactType artifactType) {
        init(branch, artifactSubtype, artifactType, false)
    }

    def init(BranchStructure branch, String artifactSubType, String artifactType) {
        init(branch, ArtifactSubType.valueOfSubType(artifactSubType), ArtifactType.valueOfType(artifactType), false)
    }
    
    def init(BranchStructure branch, ArtifactSubType artifactSubType, ArtifactType artifactType, boolean isArchetype) {
        init(branch, artifactSubType, artifactType, isArchetype, null)
    }


    def init(BranchStructure branch, String artifactSubType, String artifactType, boolean isArchetype, String archetypeModel) {
        init(branch, ArtifactSubType.valueOfSubType(artifactSubType), ArtifactType.valueOfType(artifactType), isArchetype, archetypeModel)
    }

    def init(BranchStructure branch, ArtifactSubType artifactSubtype, ArtifactType artifactType, boolean isArchetype, String archetypeModel) {

        branchStructure = branch

        this.isArchetype = isArchetype
        this.archetypeModel = archetypeModel

        if (isArchetype) {
            this.deployFlag = false
        } else if (branch.branchType == BranchType.PROTOTYPE && PipelineUtils.commitLogHasDeployFlag(commitLog))  deployFlag = true
		else if (branch.branchType == BranchType.FEATURE && PipelineUtils.commitLogHasDeployFlag(commitLog)) deployFlag = true
        else if (branch.branchType == BranchType.MASTER) deployFlag = true
        else if (branch.branchType == BranchType.HOTFIX) deployFlag = true
		else if (branch.branchType == BranchType.CONFIGFIX) deployFlag = false
		//else if (branch.branchType == BranchType.CONFIGLIBFIX) deployFlag = false
        else if (branch.branchType == BranchType.RELEASE) {
            deployFlag = true
        } else deployFlag = false

        branch.initFeatureFromBranchName()

        if (gitAction == 'MERGE') {
            deployFlag = false
        }

        if ((gitAction == null || gitAction == 'PUSH') && branch.branchType == BranchType.MASTER) {
            //Estamos delante un PUSH DE MASTER
            //Tenemos que validar si el origen del MR es una FEATURE o RELEASE
            //En el caso que sea el origen una FEATURE tenemos que recoger el FEATURE origen
            //en el caso que sea una RELEASE ¿? hacer deploy contra BMX. Esto no lo poderemos saber
            //en otro caso, esta gente esta haciendo PUSH contra MASTER directament

            //Revisamos el config log a ver que contiene
            //Si no han modificado el mensaje deberia salir Merge branch '<nombre_rama>' into 'master'
            //Nos basamos en esto y crucemos los dedos que no decidan
            commitLog.tokenize(' ').each {
                if (it.toLowerCase()==GlobalVars.MERGE)
                    isPushFromMerge = true
                else {
                    if (it.toLowerCase().indexOf(GlobalVars.FEATURE_BRANCH) != -1 || it.toLowerCase().indexOf(GlobalVars.RELEASE_BRANCH) != -1) {
                        BranchStructure tmpBranch = new BranchStructure()
                        tmpBranch.branchName = it
						if (tmpBranch.branchName != null) {
							tmpBranch.branchName = tmpBranch.branchName.replaceAll('\'', '')
						}
                        tmpBranch.init()
                        originPushBranchToMaster = tmpBranch.branchName

                        originPushToMaster = tmpBranch.branchType
                        eventToPush = tmpBranch.getFeatureRelease()
                    }
                }
            }
        }
        garArtifactType = initFromGitUrlGarApp(gitUrl, artifactSubtype)
        initDomainProperties(gitUrl,garArtifactType)

        /**
         * Instanciamos el tipo de structura para saber que desplegamos
         */

		if (branch.branchType == BranchType.PROTOTYPE ) {
			bmxStructure = new EdenBmxStructure()
			pipelineStructure.resultPipelineData = new PrototypeResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag)
		}else if (branch.branchType == BranchType.FEATURE) {
            bmxStructure = new EdenBmxStructure()
			if (branch.featureNumber.startsWith('BBDD')) {
				//Es una feature de un script de BBDD
				pipelineStructure.resultPipelineData = new FeatureBBDDResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag,branch.branchName,artifactType,artifactSubtype,commitId,pipelineId)
				pipelineStructure.nombre="CI_BBDD_Pipeline"				
			}else {
                if (pipelineStructureType == PipelineStructureType.SONAR_SCAN_AND_SEND_REPORT) {
                    pipelineStructure.resultPipelineData = new SonarScanResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag, branch.branchType, isArchetype, archetypeModel)
                } else {    
				    pipelineStructure.resultPipelineData = new FeatureResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag)
                }
			}           
			
        } else if (branch.branchType == BranchType.MASTER && originPushToMaster == BranchType.FEATURE) {
            bmxStructure = new DevBmxStructure()
            if (pipelineStructureType == PipelineStructureType.SONAR_SCAN_AND_SEND_REPORT) {
                pipelineStructure.resultPipelineData = new SonarScanResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag, branch.branchType, isArchetype, archetypeModel)
            } else {
                pipelineStructure.resultPipelineData = new MasterResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag)
			    if (eventToPush.startsWith('BBDD'))	pipelineStructure.nombre="CI_BBDD_Pipeline"
            }			
        } else if (branch.branchType == BranchType.MASTER && originPushToMaster == BranchType.RELEASE) {
            bmxStructure = new DevBmxStructure()
            if (pipelineStructureType == PipelineStructureType.SONAR_SCAN_AND_SEND_REPORT) {
                pipelineStructure.resultPipelineData = new SonarScanResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag, branch.branchType, isArchetype, archetypeModel)
            } else {
                pipelineStructure.resultPipelineData = new MasterResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag)
            }
        } else if (branch.branchType == BranchType.MASTER) {
            bmxStructure = new DevBmxStructure()
            if (pipelineStructureType == PipelineStructureType.SONAR_SCAN_AND_SEND_REPORT) {
                pipelineStructure.resultPipelineData = new SonarScanResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag, branch.branchType, isArchetype, archetypeModel)
            } else {
                pipelineStructure.resultPipelineData = new MasterResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag)
            }
        } else if (branch.branchType == BranchType.RELEASE && !branch.branchName.startsWith('release/BBDD')) {
            bmxStructure = new TstBmxStructure()
            if (pipelineStructureType == PipelineStructureType.SONAR_SCAN_AND_SEND_REPORT) {
                pipelineStructure.resultPipelineData = new SonarScanResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag, branch.branchType, isArchetype, archetypeModel)
            } else {
                pipelineStructure.resultPipelineData = new ReleaseResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag)
            }
		} else if (branch.branchType == BranchType.RELEASE && branch.branchName.startsWith('release/BBDD')) {
			bmxStructure = new TstBmxStructure()
			pipelineStructure.resultPipelineData = new ReleaseBBDDResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag, isArchetype, archetypeModel)
		} else if (branch.branchType == BranchType.HOTFIX) {
            bmxStructure = new PreBmxStructure()
            pipelineStructure.resultPipelineData = new HotFixResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag, isArchetype, archetypeModel)
        } else if (branch.branchType == BranchType.CONFIGFIX) {
            bmxStructure = new PreBmxStructure()
            pipelineStructure.resultPipelineData = new ConfigFixResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag, isArchetype, archetypeModel)
        }  /*else if (branch.branchType == BranchType.CONFIGLIBFIX) {
            bmxStructure = new PreBmxStructure()
            pipelineStructure.resultPipelineData = new ConfigLibFixResultPipelineData(bmxStructure.environment, gitUrl, gitProject, deployFlag, isArchetype, archetypeModel)
        }*/


        if (pipelineStructure.resultPipelineData != null) {
            pipelineStructure.resultPipelineData.repoName = getGitRepoName()
            pipelineStructure.resultPipelineData.pipelineOrigId = pipelineId
            pipelineStructure.resultPipelineData.branchName = branchStructure.branchName
            pipelineStructure.resultPipelineData.artifactType = artifactType
            pipelineStructure.resultPipelineData.artifactSubType = artifactSubtype
            pipelineStructure.resultPipelineData.commitId = commitId

            def additionalParameters = ''
            this.mvnAdditionalParameters.each { parameter ->
                if (additionalParameters) {
                    additionalParameters += ",${parameter}"
                } else {
                    additionalParameters += "${parameter}"
                }
            }
            pipelineStructure.resultPipelineData.mvnAdditionalParameters = additionalParameters

            pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)
        }
    }


    def initFromGitWithNoProject(BranchStructure branch, String gitUrl) {

        branchStructure = branch

        this.isArchetype = false
        this.archetypeModel = false
        this.deployFlag = false
        this.gitUrl = gitUrl

        branch.initFeatureFromBranchName()

        garArtifactType = initFromGitUrlGarApp(gitUrl, null)
        initDomainProperties(gitUrl,garArtifactType)
    }

    def initFromIOPPro(String gitUrlProject, String branch, ArtifactType artifactType, ArtifactSubType artifactSubtype, String gitProject) {
        branchStructure = new BranchStructure()
        branchStructure.branchName = branch
        branchStructure.init()
        gitUrl = gitUrlProject

        garArtifactType = initFromGitUrlGarApp(gitUrl, artifactSubtype)
        initDomainProperties(gitUrl,garArtifactType)

        bmxStructure = new ProBmxStructure()
        deployFlag = true
		if (pipelineStructureType == PipelineStructureType.IOP_PRO_CONFIGFIX) {
			pipelineStructure.resultPipelineData = new IopProConfigFixResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, deployFlag, jobName, jobParameters)
		} else {
			pipelineStructure.resultPipelineData = new IopProResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, deployFlag, jobName, jobParameters)
		}

        pipelineStructure.resultPipelineData.pipelineOrigId = pipelineId
        pipelineStructure.resultPipelineData.artifactSubType = artifactSubtype
        pipelineStructure.resultPipelineData.artifactType = artifactType
        pipelineStructure.resultPipelineData.branchName = branch

        pipelineStructure.resultPipelineData.commitId = commitId

        pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)
    }

    def initFromRelease(String gitUrlProject, String branch, ArtifactType artifactType, ArtifactSubType artifactSubtype, String gitProject) {
        initFromRelease(gitUrlProject, branch, artifactType, artifactSubtype, gitProject, false)
    }

    def initFromComponentVersion(String gitUrlProject, ArtifactType artifactType, ArtifactSubType artifactSubtype, String gitProject, String environment, String version, String artifact) {
        branchStructure = new BranchStructure()
        branchStructure.branchName = ""
        branchStructure.init()
        gitUrl = gitUrlProject

        garArtifactType = initFromGitUrlGarApp(gitUrl, artifactSubtype)
        initDomainProperties(gitUrl,garArtifactType)

        this.deployFlag = isArchetype ? false : true
        //Siempre se hace deploy excepto de si estamos generando un proyecto para generar un arquetipo

        if (environment == GlobalVars.DEV_ENVIRONMENT) bmxStructure = new DevBmxStructure()
        else if (environment == GlobalVars.TST_ENVIRONMENT) bmxStructure = new TstBmxStructure()
        else if (environment == GlobalVars.PRE_ENVIRONMENT) bmxStructure = new PreBmxStructure()
        else if (environment == GlobalVars.PRO_ENVIRONMENT) bmxStructure = new ProBmxStructure()

        pipelineStructure.resultPipelineData = new DeleteComponentVersionResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, deployFlag)

        pipelineStructure.resultPipelineData.pipelineOrigId = pipelineId
        pipelineStructure.resultPipelineData.artifactSubType = artifactSubtype
        pipelineStructure.resultPipelineData.artifactType = artifactType
		pipelineStructure.resultPipelineData.artifact = artifact
		pipelineStructure.resultPipelineData.version = version

        pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)
    }

	def initFromRelease(String gitUrlProject, String branch, ArtifactType artifactType, ArtifactSubType artifactSubtype, String gitProject, boolean isArchetype) {
		initFromRelease(gitUrlProject, branch, artifactType, artifactSubtype, gitProject, isArchetype, false)
	}
	
    def initFromRelease(String gitUrlProject, String branch, ArtifactType artifactType, ArtifactSubType artifactSubtype, String gitProject, boolean isArchetype, boolean isConfigurationFix) {
        branchStructure = new BranchStructure()
        branchStructure.branchName = branch
        branchStructure.init()
        gitUrl = gitUrlProject

        garArtifactType = initFromGitUrlGarApp(gitUrl, artifactSubtype)
        initDomainProperties(gitUrl,garArtifactType)

        this.deployFlag = isArchetype ? false : true
        //Siempre se hace deploy excepto de si estamos generando un proyecto para generar un arquetipo
        bmxStructure = new PreBmxStructure()
		if (isConfigurationFix) {
			pipelineStructure.resultPipelineData = new ConfigReleaseResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, deployFlag, jobName, jobParameters)
		} else {
			if (branch.startsWith('release/BBDD')) {
				pipelineStructure.resultPipelineData = new DeployReleaseResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, deployFlag, jobName, jobParameters)
			}else {
				pipelineStructure.resultPipelineData = new ReleaseResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, deployFlag, jobName, jobParameters)
			}
			
		}

        pipelineStructure.resultPipelineData.pipelineOrigId = pipelineId
        pipelineStructure.resultPipelineData.artifactSubType = artifactSubtype
        pipelineStructure.resultPipelineData.artifactType = artifactType

        pipelineStructure.resultPipelineData.commitId = commitId

        pipelineStructure.resultPipelineData.branchName = branch

        pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)
    }

    def initFromRollback(String environment, String gitUrlProject, String branch, ArtifactType artifactType, ArtifactSubType artifactSubtype, String gitProject) {
        initFromNonGit(environment, gitUrlProject, branch, artifactType, artifactSubtype, gitProject)
    }
	
	def initGlobalPipeline(String environment) {
//		initFromRelease(gitUrlProject, branch, artifactType, artifactSubtype, gitProject)
		bmxStructure = BmxUtilities.buildStructure(environment)
		garArtifactType = GarAppType.GLOBAL_PIPELINE

		if (pipelineStructureType == PipelineStructureType.INC_CAMPAIGN_CANNARY) {
			pipelineStructure.resultPipelineData = new IncCampaignCannaryResultPipelineJobData(bmxStructure.environment, pushUser, pipelineId, jobName, jobParameters)
		}else if (pipelineStructureType == PipelineStructureType.NOTIFY_END_CAMPAIGN || pipelineStructureType == PipelineStructureType.JOB_CLEAN_DEV_DUPLICATED_PODS) {
			pipelineStructure.resultPipelineData = new NotifyCloseCampaignResultPipelineJobData(bmxStructure.environment, pushUser, pipelineId, jobName, jobParameters)
		}else if (pipelineStructureType == PipelineStructureType.CLOSE_CAMPAIGN) {
			pipelineStructure.resultPipelineData = new CloseCampaignResultPipelineJobData(bmxStructure.environment, pushUser, pipelineId, jobName, jobParameters)
		}
		if (branchStructure==null) {
			branchStructure = new BranchStructure()
			branchStructure.branchName = 'master'
			branchStructure.init()
		}

	}

    def initFromNonGit(String environment, String gitUrlProject, String branch, ArtifactType artifactType, ArtifactSubType artifactSubtype, String gitProject) {
        initFromRelease(gitUrlProject, branch, artifactType, artifactSubtype, gitProject)
        bmxStructure = BmxUtilities.buildStructure(environment)

        if (pipelineStructureType == PipelineStructureType.ROLLBACK) {
            pipelineStructure.resultPipelineData = new RollbackResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, deployFlag, branch, jobName, jobParameters)
        } else if (pipelineStructureType == PipelineStructureType.ROLLBACK_FINISH) {
            pipelineStructure.resultPipelineData = new RollbackFinishResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, deployFlag, jobName, jobParameters)
        } else if (pipelineStructureType == PipelineStructureType.CLOSE) {
            pipelineStructure.resultPipelineData = new CloseResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, deployFlag, jobName, jobParameters)
        } else if (pipelineStructureType == PipelineStructureType.INC_CANNARY) {
            pipelineStructure.resultPipelineData = new IncCannaryResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, deployFlag, jobName, jobParameters)
        } else if (pipelineStructureType == PipelineStructureType.DELETE) {
            pipelineStructure.resultPipelineData = new DeleteComponentVersionResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, deployFlag)
        }

        pipelineStructure.resultPipelineData.pipelineOrigId = pipelineId
        pipelineStructure.resultPipelineData.artifactSubType = artifactSubtype
        pipelineStructure.resultPipelineData.artifactType = artifactType

        pipelineStructure.resultPipelineData.commitId = commitId

        pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)
    }

    def initFromClose(String environment, String gitUrlProject, String branch, ArtifactType artifactType, ArtifactSubType artifactSubtype, String gitProject) {
        initFromNonGit(environment, gitUrlProject, branch, artifactType, artifactSubtype, gitProject)
    }
	
	def initVoidActions(String gitUrlProject, String branch, ArtifactSubType artifactSubtype, String gitProject) {
		initVoidActions(gitUrlProject,branch,artifactSubtype,gitProject,null)
	}
	
	def initVoidActions(String gitUrlProject, String branch, ArtifactSubType artifactSubtype, String gitProject, String environment) {
		branchStructure = new BranchStructure()
		branchStructure.branchName = branch
		branchStructure.init()

		garArtifactType = initFromGitUrlGarApp(gitUrlProject, artifactSubtype)
		initDomainProperties(gitUrlProject, garArtifactType)

		gitUrl = gitUrlProject
		//Por poner algo no se usa para nada
		if (environment==null) {
			bmxStructure = new TstBmxStructure()
		}else {
			if (environment.equals(GlobalVars.EDEN_ENVIRONMENT)) {
				bmxStructure = new EdenBmxStructure()
            }else if (environment.equals(GlobalVars.DEV_ENVIRONMENT)) {
				bmxStructure = new DevBmxStructure()
            }else if (environment.equals(GlobalVars.TST_ENVIRONMENT)) {
				bmxStructure = new TstBmxStructure()
            }else if (environment.equals(GlobalVars.PRE_ENVIRONMENT)) {
				bmxStructure = new PreBmxStructure()
            }else if (environment.equals(GlobalVars.PRO_ENVIRONMENT)) {
				bmxStructure = new ProBmxStructure()
            }			
		}
		
		if (pipelineStructureType == PipelineStructureType.BBDD_LIQUIBASE_STATUS) {
            pipelineStructure.resultPipelineData = new BBDDReportActionsResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, false, jobName, jobParameters)
		}else {
			pipelineStructure.resultPipelineData = new VoidActionsResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, false, jobName, jobParameters)
		}
		
		pipelineStructure.resultPipelineData.pipelineOrigId = pipelineId
		pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)
	}
	
    def initFromReleaseCandidate(String gitUrlProject, String branch, ArtifactSubType artifactSubtype, String gitProject) {
        branchStructure = new BranchStructure()
        branchStructure.branchName = branch
        branchStructure.init()

        garArtifactType = initFromGitUrlGarApp(gitUrlProject, artifactSubtype)
        initDomainProperties(gitUrl,garArtifactType)

        gitUrl = gitUrlProject
        bmxStructure = new TstBmxStructure()
		if (branch.startsWith('release/BBDD')) {
			pipelineStructure.resultPipelineData = new ReleaseCandidateBBDDResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, false, jobName, jobParameters)
		}else {
			pipelineStructure.resultPipelineData = new ReleaseCandidateResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, false, jobName, jobParameters)
		}
        pipelineStructure.resultPipelineData.pipelineOrigId = pipelineId
        pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)
    }

    def initFromFix(String gitUrlProject, String branch, ArtifactSubType artifactSubtype, String gitProject) {
        branchStructure = new BranchStructure()
        branchStructure.branchName = branch
        branchStructure.init()

        garArtifactType = initFromGitUrlGarApp(gitUrlProject, artifactSubtype)
        initDomainProperties(gitUrl)

        gitUrl = gitUrlProject
        bmxStructure = new PreBmxStructure()
        pipelineStructure.resultPipelineData = new HotFixResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, false, jobName, jobParameters)
        pipelineStructure.resultPipelineData.pipelineOrigId = pipelineId
        pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)
    }
	
	def initFromConfigFix(String gitUrlProject, String branch, ArtifactSubType artifactSubtype, String gitProject) {
		branchStructure = new BranchStructure()
		branchStructure.branchName = branch
		branchStructure.init()

		garArtifactType = initFromGitUrlGarApp(gitUrlProject, artifactSubtype)
		initDomainProperties(gitUrl)

		gitUrl = gitUrlProject
		bmxStructure = new PreBmxStructure()
		pipelineStructure.resultPipelineData = new ConfigFixResultPipelineJobData(bmxStructure.environment, gitUrl, gitProject, false, jobName, jobParameters)
		pipelineStructure.resultPipelineData.pipelineOrigId = pipelineId
        pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)
	}

    static public GarAppType initFromGitUrlGarApp(String gitUrl, ArtifactSubType artifactSubtype) {

        GarAppType result = null
        if (gitUrl != null && gitUrl != '') {
            /**
             * Como podemos saber el tipo de artefacto?
             * Si es aplicativo.... la carpeta del repo nos indicar donde tiene que ir
             * Si es de arquitectura... el SubType deberia ser suficiente
             */
            if (gitUrl.contains(GlobalVars.GIT_REPO_ARCH)) {
                //No tenemos el tipo
                switch (artifactSubtype) {
                    case ArtifactSubType.MICRO_ARCH:
                        result = GarAppType.ARCH_MICRO
                        break
                    case ArtifactSubType.PLUGIN_STARTER:
					case ArtifactSubType.PLUGIN_STARTER_SAMPLE_APP:
                    case ArtifactSubType.PLUGIN:
                    case ArtifactSubType.STARTER:
                        result = GarAppType.ARCH_PLUGIN
                        break
                    case ArtifactSubType.ARCH_LIB:
					case ArtifactSubType.ARCH_LIB_WITH_SAMPLEAPP:
                        result = GarAppType.ARCH_LIBRARY
                        break
                    case ArtifactSubType.ARCH_CFG:
                        result = GarAppType.ARCH_CONFIG
                        break
                    case ArtifactSubType.SRV_CFG:
                        result = GarAppType.SRV_CONFIG
                        break
                    default:
                        result = GarAppType.ARCH_LIBRARY
                        break
                }
            } else if (gitUrl.contains(GlobalVars.GIT_REPO_APP_DATA_SERVICE)) {
                result = GarAppType.DATA_SERVICE
            } else if (gitUrl.contains(GlobalVars.GIT_REPO_APP_BFF_SERVICE)) {
                result = GarAppType.BFF_SERVICE
            } else if (gitUrl.contains(GlobalVars.GIT_REPO_APP_SERVICE)) {
                result = GarAppType.MICRO_SERVICE
            } else if (gitUrl.contains(GlobalVars.GIT_REPO_APP_LIBRARY) ) {
				switch (artifactSubtype) {
					case ArtifactSubType.SRV_CFG:
						result = GarAppType.SRV_CONFIG
					break
					default:
						result = GarAppType.LIBRARY
					break
				}
            } else if (gitUrl.contains(GlobalVars.GIT_REPO_APP_CONF_LIBRARY) && artifactSubtype == ArtifactSubType.SRV_CFG) {
                result = GarAppType.SRV_CONFIG
            } else if (gitUrl.contains(GlobalVars.GIT_REPO_DEFINITIONS)) {
                result = GarAppType.ARCH_LIBRARY
            }
        }

        return result
    }

	def initDomainProperties(String gitUrl) {
		initDomainProperties(gitUrl,null)
	}
		
    def initDomainProperties(String gitUrl,GarAppType garArtifactType) {
        if (garArtifactType!=null &&  GarAppType.ARCH_MICRO == garArtifactType ) {
			//for arch-micro type the domain is architecture
			domain = "architecture"
		} else {
			domain = GitUtils.getDomainFromUrl(gitUrl)
		}
		subDomain = "NO_SUBDOMAIN"
        company = GitUtils.getCompanyFromUrl(gitUrl)

    }

	def initFromCopy(BranchStructure branch, ArtifactSubType artifactSubtype, ArtifactType artifactType,String env) {
		init(branch,artifactSubtype,artifactType,false,'./')
		ManualDeployPipelineStructure manualDeploy = new ManualDeployPipelineStructure(pipelineStructure.pipelineId)
	
		if (pipelineStructure.resultPipelineData != null) {
			manualDeploy.resultPipelineData=new ManualCopyResultPipelineJobData(pipelineStructure.resultPipelineData)
			manualDeploy.resultPipelineData.environment=env.toLowerCase()
            pipelineStructure.resultPipelineData.storeRetryAuthorizationParameters(jobParameters)
		}
		pipelineStructure= manualDeploy
	}

    def setDefaultAgent(agent) {
        pipelineStructure.resultPipelineData.agent = agent;
    }

    def getGitRepoName() {
        if (gitUrl != null && gitUrl != '') {
            def lastIndex = gitUrl.lastIndexOf('/')
            if (lastIndex != -1) {
                return gitUrl.substring(gitUrl.lastIndexOf('/') + 1) - '.git'
            } else return ''
        } else return ''
    }

    def getGitUrlProjectRelative() {
        if (gitUrl != null) return gitUrl - GlobalVars.gitlabDomain - '.git'
        else return ''
    }

    def getPipelineBuildName() {

		if (branchStructure.branchType == BranchType.PROTOTYPE) return "" + branchStructure.branchType.toString() + " "
        if (branchStructure.branchType == BranchType.FEATURE) return "" + branchStructure.branchType.toString() + "_" + branchStructure.featureNumber
        if (branchStructure.branchType == BranchType.MASTER) return "" + branchStructure.branchType.toString() + " "
        if (branchStructure.branchType == BranchType.RELEASE) return "" + branchStructure.branchType.toString() + "_" + branchStructure.releaseNumber
        if (branchStructure.branchType == BranchType.HOTFIX) return "" + branchStructure.branchType.toString() + "_" + branchStructure.releaseNumber
		if (branchStructure.branchType == BranchType.CONFIGFIX) return "" + branchStructure.branchType.toString() + "_" + branchStructure.releaseNumber
    }

    void prepareExecutionMode(String executionMode, String almSubFolder, skipQuality = false) {
        pipelineDataExecutionMode = new DefaultExecutionMode(almSubFolder, skipQuality);
        pipelineStructure.resultPipelineData.executionProfile = PipelineExecutionMode.DEFAULT_MODE
        pipelineStructure.resultPipelineData.almSubFolder = almSubFolder != null ? almSubFolder : ""

        //El perfil puede venir a traves del commitLog, si es asi se parsea
        if (commitLog && commitLog.indexOf(GlobalVars.EXECUTION_PROFILE_COMMIT_LOG_TAG) != -1) {
            String textToLocate = GlobalVars.EXECUTION_PROFILE_COMMIT_LOG_TAG + "[";
            executionMode = commitLog.substring(commitLog.indexOf(textToLocate) + textToLocate.length(), commitLog.lastIndexOf("]"));
        }

        if (executionMode) {
            executionProfileName = executionMode
            pipelineStructure.resultPipelineData.executionProfile = executionMode
            if (PipelineExecutionMode.DEFAULT_MODE == executionMode) {
                pipelineDataExecutionMode = new DefaultExecutionMode(almSubFolder, skipQuality);
            } else if (PipelineExecutionMode.COMPLETE_TEST_AUTO == executionMode) {
                pipelineDataExecutionMode = new PipelineCompleteTestExecutionMode(PipelineCompleteTestExecutionMode.DEFAULT_FLAG, almSubFolder, skipQuality);
                if (branchStructure.branchType == BranchType.HOTFIX) {
                    //Si entramos en hotfix, hay que cambiar el perfil para cambiar el camino de la siguiente invocacion
                    pipelineStructure.resultPipelineData.executionProfile = PipelineExecutionMode.COMPLETE_TEST_AUTO_HOTFIX
                } else if (branchStructure.branchType == BranchType.CONFIGFIX) {
                    //Si entramos en configuration fix, hay que cambiar el perfil para cambiar el camino de la siguiente invocacion
                    pipelineStructure.resultPipelineData.executionProfile = PipelineExecutionMode.COMPLETE_TEST_AUTO_CONFIGURATIONFIX
                    pipelineDataExecutionMode = new PipelineCompleteTestExecutionMode(PipelineCompleteTestExecutionMode.FLAG_WHEN_CONFIGURATION_FIX, almSubFolder, skipQuality)
                }
            } else if (PipelineExecutionMode.COMPLETE_TEST_AUTO_HOTFIX == executionMode) {
                pipelineDataExecutionMode = new PipelineCompleteTestExecutionMode(PipelineCompleteTestExecutionMode.FLAG_WHEN_HOTFIX, almSubFolder, skipQuality)
            } else if (PipelineExecutionMode.COMPLETE_TEST_AUTO_CONFIGURATIONFIX == executionMode) {
                pipelineDataExecutionMode = new PipelineCompleteTestExecutionMode(PipelineCompleteTestExecutionMode.FLAG_WHEN_CONFIGURATION_FIX, almSubFolder, skipQuality)
            } else if (PipelineExecutionMode.COMPONENT_NEW_VERSION == executionMode) {
                pipelineDataExecutionMode = new CustomFlagExecutionMode("triggerIfComponentIsNewVersion", almSubFolder)
            } else if (PipelineExecutionMode.FEATURE_AUTO_MERGE_IF_OK == executionMode) {
                //Solo completa la siguiente accion a nivel de feature que es un MR Automatico
                pipelineDataExecutionMode = new CustomFlagExecutionMode("actionInFeatureAutoMerge", almSubFolder)
            } else if (PipelineExecutionMode.UPGRADE_CORE_AND_CREATE_RC == executionMode) {
                //Completa la siguiente accion a nivel de feature que es un MR Automatico y lanza la creación de RC
                pipelineDataExecutionMode = new CustomFlagExecutionMode("upgradeCoreAndCreateRC", almSubFolder)
            } else if (PipelineExecutionMode.SCHEDULED_CORE_UPDATE == executionMode) {
                pipelineDataExecutionMode = new CoreUpdateExecutionMode("actionInFeatureAutoMerge", almSubFolder)
            }

        }
    }

    public boolean isSonarScanneable() {
        boolean scanneable = true
        if (branchStructure.branchType == BranchType.FEATURE) {
            if (pipelineBehavior == PipelineBehavior.PUSH_OPENED_MR || pipelineBehavior == PipelineBehavior.PUSH_NO_MR) {
                scanneable = false
            }
        } else if (branchStructure.branchType == BranchType.CONFIGFIX) {
			scanneable = false
		}
        return scanneable
    }
	
	public boolean isCheckmarxScanneable() {
		//reuse sonar method it would change in future
		return isSonarScanneable()
	}

    PipelineExecutionMode getExecutionMode() {
        return pipelineDataExecutionMode
    }

    public boolean isDelete() {
        return this.pipelineStructureType == PipelineStructureType.DELETE
    }

    public boolean isRollbackStart() {
        return this.pipelineStructureType == PipelineStructureType.ROLLBACK
    }

    public boolean isRollbackFinish() {
        return this.pipelineStructureType == PipelineStructureType.ROLLBACK_FINISH
    }

    public boolean isCreateRC() {
        return this.pipelineStructureType == PipelineStructureType.RELEASE_CANDIDATE
    }

    public boolean isHotfixRelease() {
        return this.pipelineStructureType == PipelineStructureType.RELEASE && this.branchStructure.branchType == BranchType.HOTFIX
    }

    public boolean isRegularRelease() {
        return this.pipelineStructureType == PipelineStructureType.RELEASE && this.branchStructure.branchType == BranchType.RELEASE
    }

    public boolean isCIConfigFixBranch() {
        return this.pipelineStructureType == PipelineStructureType.CI && this.branchStructure.branchType == BranchType.CONFIGFIX
    }

    public boolean isCIHotfixBranch() {
        return this.pipelineStructureType == PipelineStructureType.CI && this.branchStructure.branchType == BranchType.HOTFIX
    }

    public boolean isCIReleaseBranch() {
        return this.pipelineStructureType == PipelineStructureType.CI && this.branchStructure.branchType == BranchType.RELEASE
    }

    public boolean isCIFeatureOrMaster() {
        return this.pipelineStructureType == PipelineStructureType.CI && (this.branchStructure.branchType == BranchType.MASTER || this.branchStructure.branchType == BranchType.FEATURE)
    }

    public boolean isCIMasterBranch() {
        return this.pipelineStructureType == PipelineStructureType.CI && this.branchStructure.branchType == BranchType.MASTER
    }

    public boolean isCIFeatureBranch() {
        return this.pipelineStructureType == PipelineStructureType.CI && this.branchStructure.branchType == BranchType.FEATURE
    }

    public boolean isCreateRelease() {
        return this.pipelineStructureType == PipelineStructureType.RELEASE
    }

    public boolean isCloseRelease() {
        return this.pipelineStructureType == PipelineStructureType.CLOSE
    }

    public boolean requiresChangelogPush() {
        if (commitLog?.contains("deploy with executionProfile[${PipelineExecutionMode.COMPLETE_TEST_AUTO}]")) {
            return false
        }
        if (commitLog?.contains("deploy with executionProfile[${PipelineExecutionMode.COMPLETE_TEST_AUTO_HOTFIX}]")) {
            return false
        }
        if (commitLog?.contains("deploy with executionProfile[${PipelineExecutionMode.SCHEDULED_CORE_UPDATE}]")) {
            return false
        }
        if (commitLog?.contains("deploy with executionProfile[${PipelineExecutionMode.UPGRADE_CORE_AND_CREATE_RC}]")) {
            return false
        }
        return true
    }

    String toString() {
        return "PipelineData:\n" +
            "\tancientMapInfo: ${ancientMapInfo}\n" +
            "\tresultDeployInfo: ${resultDeployInfo}\n" +
            "\troutesToNexus: ${routesToNexus}\n" +
			"\tclientRoutesToNexus: ${clientRoutesToNexus}\n" +
            "\tpipelineId: ${pipelineId}\n" +
            "\tgitAction: ${gitAction}\n" +
            "\tgitUrl: ${gitUrl}\n" +
            "\tcommitLog: ${commitLog}\n" +
            "\tpipelineBehavior: ${pipelineBehavior}\n" +
            "\tisMultiBranchPipeline: ${isMultiBranchPipeline}\n" +
            "\toriginPushToMaster: ${originPushToMaster}\n" +
            "\tdeployFlag: ${deployFlag}\n" +
            "\tisPushFromMerge: ${isPushFromMerge}\n" +
            "\tbranchStructure: ${branchStructure ? branchStructure.toString() : ''}\n" +
            "\tpipelineStructure: ${pipelineStructure ? pipelineStructure.toString() : ''}\n\n" +
            "\tgarArtifactType: ${garArtifactType ?: ''}\n" +
            "\tpipelineStructureType: ${pipelineStructureType ?: ''}\n" +
            "\tgitProject: ${gitProject}\n" +
            "\tbmxStructure: ${bmxStructure ? bmxStructure.toString() : ''}\n" +
            "\tdistributionModePRO: ${distributionModePRO ? distributionModePRO.toString() : ''}\n" +
            "\teventToPush: ${eventToPush}\n" +
            "\tbuildCode: ${buildCode}\n" +
            "\tisArchetype: ${isArchetype}\n" +
            "\tarchetypeModel: ${archetypeModel}\n" +
            "\tpushUser: ${pushUser}\n" +
            "\tpushUserEmail: ${pushUserEmail}\n" +
			"\tcommitLog: ${commitLog}\n"+
            "\ttestData: ${testData}\n"
    }

}
