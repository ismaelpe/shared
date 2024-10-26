package com.project.alm


import com.project.alm.BmxUtilities
import com.project.alm.BmxStructure
import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure
import com.project.alm.CloudWorkflowStates
import com.project.alm.CloudAppResources
import com.project.alm.PipelineData
import com.project.alm.CloudDeployStructure
import com.project.alm.BranchType
import com.project.alm.BranchStructure
import com.project.alm.ArtifactSubType
import com.project.alm.MavenVersionUtilities
import com.project.alm.Utilities
import java.util.Random
import org.apache.commons.lang.RandomStringUtils

class CloudStateUtility {

    /**
     * Generate
     alm:
     app:
     instance: demoConnectA22
     name: demoConnectA2
     resources:
     requests:
     memory: 512Mi
     cpu: 500m
     limits:
     memory: 1024Mi
     cpu: 1000m


     services:
     envQualifier:
     stable:
     id: demmoConnectA22
     targetColour: G
     new:
     id: new-demoConnectA22
     targetColour: B


     apps:
     envQualifier:
     stable:
     id: micro2-G
     colour: G
     image: image-1.2.1
     version: 1.2.1
     stable: false
     new : false
     envVars:
     SPRING_PROFILES_ACTIVE: "tst"
     new:
     id: micro1-G
     colour: B
     image: image-1.2.2
     version: 1.2.2
     stable: false
     new: false
     envVars:
     SPRING_PROFILES_ACTIVE: "tst,apps"

     */
    String pathFeature=""
    String versionImage=""
    CloudAppResources cloudResources
    PipelineData pipelineData
    PomXmlStructure pomXml
    String buildId
    CloudDeployStructure cloudDeployStructure

    String suffixedComponentName

    //Indica la nueva imagen
    String newImage
    String currentImage
    //Indica el valor actual del micro desplegado en el entorno
    String currentColour
    String currentVersion

    String currentReadinessProbePath
    String currentLivenessProbePath

    String currentReplicas

    Map currentEnvVars

    String environment=""

    String nameComponentInCloud
    String aplicacion
    String deployId
    String additionalRoute=null
    boolean sampleAppFlag=false
    String microType=null

    CloudWorkflowStates cloudAppState

    def scriptContext

    CloudStateUtility(PipelineData pipelineData,PomXmlStructure pomXml, String aplicacion, String majorMicro) {
        this.pipelineData=pipelineData
        this.pomXml=pomXml
        this.aplicacion = aplicacion
        this.nameComponentInCloud = aplicacion+majorMicro

        if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
            this.nameComponentInCloud=this.nameComponentInCloud + pipelineData.branchStructure.featureNumber
            this.pathFeature=""
        }
        if (pomXml.artifactSubType!=ArtifactSubType.MICRO_APP && pomXml.artifactSubType!=ArtifactSubType.MICRO_ARCH) {
            //Es una sample app
            this.nameComponentInCloud=this.nameComponentInCloud+"S"
            this.sampleAppFlag = true
        }

        this.environment=pipelineData.bmxStructure.environment.toUpperCase()
    }

    CloudStateUtility(PipelineData pipelineData,PomXmlStructure pomXml,String newImage, Cloudk8sActualStatusInfo actualStatusInfo, CloudWorkflowStates cloudAppState, String aplicacion, String majorMicro) {
        this.pipelineData=pipelineData
        this.pomXml=pomXml
        this.newImage=newImage
        this.currentColour=actualStatusInfo.currentColour
        this.currentVersion=actualStatusInfo.currentVersion
        this.currentImage=actualStatusInfo.currentImage
        this.currentReadinessProbePath = actualStatusInfo.readinessProbePath
        this.currentLivenessProbePath = actualStatusInfo.livenessProbePath
        this.currentEnvVars = actualStatusInfo.envVars
        this.currentReplicas = actualStatusInfo.replicas

        this.aplicacion = aplicacion
        this.nameComponentInCloud = aplicacion+majorMicro

        if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
            this.nameComponentInCloud=this.nameComponentInCloud + pipelineData.branchStructure.featureNumber
            this.pathFeature=""
        }
        if (pomXml.artifactSubType != ArtifactSubType.MICRO_APP && pomXml.artifactSubType != ArtifactSubType.MICRO_ARCH) {
            //Es una sample app
            this.nameComponentInCloud = this.nameComponentInCloud+"S"
        }

        this.cloudAppState=cloudAppState
        this.environment=pipelineData.bmxStructure.environment.toUpperCase()
    }

    CloudStateUtility(PipelineData pipelineData,PomXmlStructure pomXml,String newImage, String currentImage, String currentColour, String currentVersion, String currentReadinessProbePath, String currentLivenessProbePath, CloudWorkflowStates cloudAppState, String aplicacion, String majorMicro) {
        this.pipelineData=pipelineData
        this.pomXml=pomXml
        this.newImage=newImage
        this.currentColour=currentColour
        this.currentVersion=currentVersion
        this.currentImage=currentImage
        this.currentReadinessProbePath = currentReadinessProbePath
        this.currentLivenessProbePath = currentLivenessProbePath

        this.aplicacion = aplicacion
        this.nameComponentInCloud = aplicacion+majorMicro

        if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
            this.nameComponentInCloud=this.nameComponentInCloud + pipelineData.branchStructure.featureNumber
            this.pathFeature=""
        }
        if (pomXml.artifactSubType!=ArtifactSubType.MICRO_APP && pomXml.artifactSubType!=ArtifactSubType.MICRO_ARCH) {
            //Es una sample app
            this.nameComponentInCloud=this.nameComponentInCloud+"S"
        }

        this.cloudAppState=cloudAppState
        this.environment=pipelineData.bmxStructure.environment.toUpperCase()

    }

    CloudStateUtility(PipelineData pipelineData,PomXmlStructure pomXml,String newImage, String currentColour, String currentVersion, String currentReadinessProbePath, String currentLivenessProbePath, CloudWorkflowStates cloudAppState) {
        this.pipelineData=pipelineData
        this.pomXml=pomXml
        this.newImage=newImage
        this.currentColour=currentColour
        this.cloudAppState=cloudAppState
        this.currentVersion=currentVersion
        this.currentReadinessProbePath = currentReadinessProbePath
        this.currentLivenessProbePath = currentLivenessProbePath
        this.environment=pipelineData.bmxStructure.environment.toUpperCase()

        aplicacion = MavenUtils.sanitizeArtifactName(pomXml.artifactName, pipelineData.garArtifactType).toLowerCase()

        nameComponentInCloud=aplicacion+pomXml.getArtifactMajorVersion()

        if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
            nameComponentInCloud=aplicacion + majorMicro + pipelineData.branchStructure.featureNumber
            this.pathFeature=""
        }
        if (pomXml.artifactSubType!=ArtifactSubType.MICRO_APP && pomXml.artifactSubType!=ArtifactSubType.MICRO_ARCH) {
            //Es una sample app
            this.nameComponentInCloud=this.nameComponentInCloud+"S"
        }
    }

    def setScriptContext(def scriptContext) {
        this.scriptContext = scriptContext
    }

    def getLastDeployId() {
        return ""
        //if (deployId!=null && !"".equals(deployId)) return "-"+deployId
        //else return ""
    }

    def initExtraRoute() {
        if (this.pipelineData.branchStructure.branchType == BranchType.FEATURE) {
            this.pathFeature = BmxUtilities.calculateFeatureAppName(pomXml, pipelineData)
        }

        if (this.pipelineData.branchStructure.branchType == BranchType.MASTER) {
            if (BmxUtilities.calculateArtifactId(pomXml, pipelineData.branchStructure).toLowerCase().endsWith('-dev')) {
                this.additionalRoute = BmxUtilities.calculateArtifactId(pomXml, pipelineData.branchStructure).toLowerCase()-'-dev'
            }
        }
    }

    CloudWorkflowStates getNextStateWorkflow() {
        if (cloudAppState==CloudWorkflowStates.NEW_DEPLOY) {
            if (currentColour==null) {
                return CloudWorkflowStates.ADD_STABLE_ROUTE_TO_NEW_APP
            }
            else return CloudWorkflowStates.ELIMINATE_NEW_ROUTE_TO_CURRENT_APP
        }else if (cloudAppState==CloudWorkflowStates.ELIMINATE_NEW_ROUTE_TO_CURRENT_APP) {
            return CloudWorkflowStates.ADD_STABLE_ROUTE_TO_NEW_APP
        }else if (cloudAppState==CloudWorkflowStates.ADD_STABLE_ROUTE_TO_NEW_APP) {
            if (currentColour==null) return CloudWorkflowStates.ELIMINATE_CURRENT_APP
            else return CloudWorkflowStates.ELIMINATE_STABLE_ROUTE_TO_CURRENT_APP
        }else if (cloudAppState==CloudWorkflowStates.ELIMINATE_STABLE_ROUTE_TO_CURRENT_APP) {
            return CloudWorkflowStates.ELIMINATE_CURRENT_APP
        }else return CloudWorkflowStates.END
    }

    String getNumReplicas() {
        if (this.environment!=null && this.environment.equals("PRO")) {
            return cloudResources.getNumInstances(environment)
        }else {
            return "1"
        }
    }

    String getValueOfMicroType() {
        if (microType!=null) {
            return "    type: "+microType+"\n";
        }else {
            return "";
        }
    }
    String getChartValues() {
        String applicationName = nameComponentInCloud;

        if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
			applicationName=applicationName.replace('-','')
            if(applicationName.length() > GlobalVars.LIMIT_LENGTH_FOR_PODNAME_WITHOUT_K8_SUFFIX) {
                applicationName = applicationName.substring(0, GlobalVars.LIMIT_LENGTH_FOR_PODNAME_WITHOUT_K8_SUFFIX);
            }
        }
        return "alm:\n"+
            "  app:\n"+
            "    loggingElkStack: alm0\n"+
            "    replicas: 1\n"+
            "    instance: "+applicationName+"\n"+
            "    name: "+aplicacion+"\n"+
            getValueOfMicroType()+
            cloudResources.getChartValues(this.environment)+
            getChartValuesApps()+
            getChartValuesServices()
    }

    String getNewColour() {
        if (currentColour==null || currentColour=="") return "B"
        else {
            if (currentColour=="B") return "G"
            else return "B"
        }
    }

    String getCurrentColour() {
        if (currentColour==null || currentColour=="") return "Y"
        else return currentColour
    }

    boolean isSampleApp() {
        return sampleAppFlag
    }

    /**
     apps:
     envQualifier:
     stable:
     id: micro2-G
     colour: G
     image: image-1.2.1
     version: 1.2.1
     stable: false
     new : false
     readinessProbePath : /actuator/health/readiness
     livenessProbePath : /actuator/health/liveness
     envVars:
     SPRING_PROFILES_ACTIVE: "tst"
     new:
     id: micro1-G
     colour: B
     image: image-1.2.2
     version: 1.2.2
     stable: false
     new: false
     readinessProbePath : /actuator/health/readiness
     livenessProbePath : /actuator/health/liveness
     envVars:
     SPRING_PROFILES_ACTIVE: "tst,apps"
     */

    String getChartValuesApps() {
        String newImagePlusVersion=newImage
        //No tienee la version por alguna mejora en el api de cloud
        if (!newImagePlusVersion.contains(":"))  newImagePlusVersion=newImagePlusVersion+":"+versionImage
        String forceDeploy = ""
		String forceDeployRetro = ""

        if (pipelineData!=null && pomXml!=null && pomXml.artifactVersion.contains('SNAPSHOT')) {
            forceDeploy="          forceDeploy: "+pipelineData.buildCode+"\n"
        }
		
		if (pipelineData!=null && pomXml!=null && "PRE".equalsIgnoreCase(this.environment)) {
			forceDeployRetro="          forceDeployRetro: "+pomXml.artifactVersion+"\n"			
		}

        String valuesApps= "  apps:\n"+
            "    envQualifier:\n"
        //Si es feature no nos interesa nada mas que el nuevo servicio
        if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {

            String featureAppName = nameComponentInCloud.toLowerCase().replace('-','') + "-b";
			
            if(featureAppName.length() > GlobalVars.LIMIT_LENGTH_FOR_PODNAME_WITHOUT_K8_SUFFIX) {
                featureAppName = featureAppName.substring(0, GlobalVars.LIMIT_LENGTH_FOR_PODNAME_WITHOUT_K8_SUFFIX) + "-b";
            }

            valuesApps=valuesApps+
                "      stable:\n"+///OJO
                "        id: "+featureAppName+"\n"+
                "        colour: B\n"+
                "        image: "+newImagePlusVersion+"\n"+
                "        version: "+pomXml.artifactVersion+"\n"+
                "        stable: false\n"+
                "        new: false\n"+
                "        replicas: 1\n"+
                "        readinessProbePath: "+getNewReadinessProbePath(pomXml.archVersion)+"\n"+
                "        livenessProbePath: "+getNewLivenessProbePath(pomXml.archVersion)+"\n"+
                "        envVars:\n"+
                "          SPRING_PROFILES_ACTIVE: "+cloudDeployStructure.springProfilesActive+"\n"+
                forceDeploy+
                cloudResources.getChartAppValues(this.environment)
        }else if (isSampleApp()) {
            //Es una sample app debe ser eliminada

            String sampleAppName = nameComponentInCloud.toLowerCase()+"-"+getNewColour().toLowerCase()+getLastDeployId();
            if(sampleAppName.length() > GlobalVars.LIMIT_LENGTH_FOR_PODNAME_WITHOUT_K8_SUFFIX) {
                sampleAppName = sampleAppName.substring(0, GlobalVars.LIMIT_LENGTH_FOR_PODNAME_WITHOUT_K8_SUFFIX);
            }
            valuesApps=valuesApps+
                "      stable:\n"+
                "        id: "+sampleAppName+"\n"+
                "        colour: B\n"+
                "        image: "+newImagePlusVersion+"\n"+
                "        version: "+pomXml.artifactVersion+"\n"+
                "        stable: false\n"+
                "        new: false\n"+
                "        replicas: 1\n"+
                "        readinessProbePath: "+getNewReadinessProbePath(pomXml.archVersion)+"\n"+
                "        livenessProbePath: "+getNewLivenessProbePath(pomXml.archVersion)+"\n"+
                "        envVars:\n"+
                "          SPRING_PROFILES_ACTIVE: "+cloudDeployStructure.springProfilesActive+"\n"+
                forceDeploy+
                cloudResources.getChartAppValues(this.environment)
        }else {
            if (cloudAppState==CloudWorkflowStates.NEW_DEPLOY ||  cloudAppState==CloudWorkflowStates.ADD_STABLE_ROUTE_TO_NEW_APP) {
                if (currentColour==null) { //Primera Major
                    valuesApps=valuesApps+
                        "      stable:\n"+
                        "        id: "+nameComponentInCloud.toLowerCase()+"-"+getNewColour().toLowerCase()+getLastDeployId()+"\n"+
                        "        colour: "+getNewColour()+"\n"+
                        "        image: "+newImagePlusVersion+"\n"+
                        "        version: "+pomXml.artifactVersion+"\n"+
                        "        stable: false\n"+
                        "        new: false\n"+
                        "        replicas: "+getNumReplicas()+"\n"+
                        "        readinessProbePath: "+getNewReadinessProbePath(pomXml.archVersion)+"\n"+
                        "        livenessProbePath: "+getNewLivenessProbePath(pomXml.archVersion)+"\n"+
                        "        envVars:\n"+
                        "          SPRING_PROFILES_ACTIVE: "+cloudDeployStructure.springProfilesActive+"\n"+
                        getJvmConfigFromCloudResource() +
                        cloudResources.getChartAppValues(this.environment)
                }else {
                    valuesApps=valuesApps+
                        "      stable:\n"+
                        "        id: "+nameComponentInCloud.toLowerCase()+"-"+currentColour.toLowerCase()+"\n"+
                        "        colour: "+currentColour+"\n"+
                        "        image: "+currentImage+"\n"+
                        "        version: "+currentVersion+"\n"+
                        "        stable: false\n"+
                        "        new: false\n"+
                        "        replicas: "+currentReplicas+"\n"+
                        (currentReadinessProbePath==null?"":"        readinessProbePath: "+currentReadinessProbePath+"\n")+
                        (currentLivenessProbePath==null?"":"        livenessProbePath: "+currentLivenessProbePath+"\n")+
                        getEnvVarsYmlFormat(currentEnvVars)+
						forceDeployRetro+
                        cloudResources.getChartAppValues(this.environment)+
                        "      new:\n"+
                        "        id: "+nameComponentInCloud.toLowerCase()+"-"+getNewColour().toLowerCase()+getLastDeployId()+"\n"+
                        "        colour: "+getNewColour()+"\n"+
                        "        image: "+newImagePlusVersion+"\n"+
                        "        version: "+pomXml.artifactVersion+"\n"+
                        "        stable: false\n"+
                        "        new: false\n"+
                        "        replicas: "+getNumReplicas()+"\n"+
                        "        readinessProbePath: "+getNewReadinessProbePath(pomXml.archVersion)+"\n"+
                        "        livenessProbePath: "+getNewLivenessProbePath(pomXml.archVersion)+"\n"+
                        "        envVars:\n"+
                        "          SPRING_PROFILES_ACTIVE: "+cloudDeployStructure.springProfilesActive+"\n"+
                        getJvmConfigFromCloudResource() +
                        forceDeploy+
                        cloudResources.getChartAppValues(this.environment)
                }
            }else if (cloudAppState==CloudWorkflowStates.ELIMINATE_NEW_ROUTE_TO_CURRENT_APP || cloudAppState==CloudWorkflowStates.ELIMINATE_STABLE_ROUTE_TO_CURRENT_APP) {
                valuesApps=valuesApps+
                    "      stable:\n"+
                    "        id: "+nameComponentInCloud.toLowerCase()+"-"+currentColour.toLowerCase()+"\n"+
                    "        colour: "+currentColour+"\n"+
                    "        image: "+currentImage+"\n"+
                    "        version: "+currentVersion+"\n"+
                    "        stable: false\n"+
                    "        new: false\n"+
                    "        replicas: "+currentReplicas+"\n"+
                    (currentReadinessProbePath==null?"":"        readinessProbePath: "+currentReadinessProbePath+"\n")+
                    (currentLivenessProbePath==null?"":"        livenessProbePath: "+currentLivenessProbePath+"\n")+
                    getEnvVarsYmlFormat(currentEnvVars)+
					forceDeployRetro+
                    cloudResources.getChartAppValues(this.environment)+
                    "      new:\n"+
                    "        id: "+nameComponentInCloud.toLowerCase()+"-"+getNewColour().toLowerCase()+getLastDeployId()+"\n"+
                    "        colour: "+getNewColour()+"\n"+
                    "        image: "+newImagePlusVersion+"\n"+
                    "        version: "+pomXml.artifactVersion+"\n"+
                    "        stable: false\n"+
                    "        new: false\n"+
                    "        replicas: "+getNumReplicas()+"\n"+
                    "        readinessProbePath: "+getNewReadinessProbePath(pomXml.archVersion)+"\n"+
                    "        livenessProbePath: "+getNewLivenessProbePath(pomXml.archVersion)+"\n"+
                    "        envVars:\n"+
                    "          SPRING_PROFILES_ACTIVE: "+cloudDeployStructure.springProfilesActive+"\n"+
                    getJvmConfigFromCloudResource() +
                    forceDeploy+
                    cloudResources.getChartAppValues(this.environment)
            }else if (cloudAppState==CloudWorkflowStates.ELIMINATE_CURRENT_APP) {
                valuesApps=valuesApps+
                    "      stable:\n"+
                    "        id: "+nameComponentInCloud.toLowerCase()+"-"+getNewColour().toLowerCase()+getLastDeployId()+"\n"+
                    "        colour: "+getNewColour()+"\n"+
                    "        image: "+newImagePlusVersion+"\n"+
                    "        version: "+pomXml.artifactVersion+"\n"+
                    "        stable: false\n"+
                    "        new: false\n"+
                    "        replicas: "+getNumReplicas()+"\n"+
                    "        readinessProbePath: "+getNewReadinessProbePath(pomXml.archVersion)+"\n"+
                    "        livenessProbePath: "+getNewLivenessProbePath(pomXml.archVersion)+"\n"+
                    "        envVars:\n"+
                    "          SPRING_PROFILES_ACTIVE: "+cloudDeployStructure.springProfilesActive+"\n"+
                    getJvmConfigFromCloudResource() +
                    cloudResources.getChartAppValues(this.environment)

                if (currentColour!=null && !"".equals(currentColour) && !environment.equals("DEV")) {
                    valuesApps=valuesApps+
                        "      old:\n"+
                        "        id: "+nameComponentInCloud.toLowerCase()+"-"+currentColour.toLowerCase()+"\n"+
                        "        colour: "+currentColour+"\n"+
                        "        image: "+currentImage+"\n"+
                        "        version: "+currentVersion+"\n"+
                        "        stable: false\n"+
                        "        new: false\n"+
                        "        replicas: 0\n"+
                        (currentReadinessProbePath==null?"":"        readinessProbePath: "+currentReadinessProbePath+"\n")+
                        (currentLivenessProbePath==null?"":"        livenessProbePath: "+currentLivenessProbePath+"\n")+
                        getEnvVarsYmlFormat(currentEnvVars)+
                        cloudResources.getChartAppValues(this.environment)
                }
            }
        }
        return valuesApps
    }

    String getJvmConfigFromCloudResource() {
        if (cloudResources != null) {
            def jvmConfig = cloudResources.jvmArgs
            if (jvmConfig != null) {
                return "          jvmConfig: ${jvmConfig}\n"
            } else {
                return ""
            }
        } else {
            return ""
        }
    }

    String getEnvVarsYmlFormat(Map currentEnvVars) {
        String envVars = "";
        if (currentEnvVars != null && currentEnvVars.size() > 0) {
            envVars = envVars+"        envVars:\n"
            for (entry in currentEnvVars) {
                //envVars = envVars+"          ${entry.key}: ${entry.value}\n"
                if (entry.key!=null && entry.key.equals("jvmConfig") && (entry.value==null ||  entry.value.equals(""))) {
                    //printOpen("El jvmConfig va a null... no se puede añadir", EchoLevel.ALL)
                }else {
                    envVars = envVars+"          ${entry.key}: ${entry.value}\n"
                }
            }
        }
        return envVars
    }


    /**
     services:
     envQualifier:
     stable:
     id: demmoConnectA22
     targetColour: G
     new:
     id: new-demoConnectA22
     targetColour: B
     */

    String addAditionalRoute(String colour) {

        String newColour=""
        if (colour!=null) newColour="        targetColour: "+colour+"\n"

        if (this.additionalRoute!=null) {
            return "      newNonDev:\n"+
                "        id: "+this.additionalRoute+"\n"+newColour
        }else return ""
    }

    String getChartValuesServices() {
        String valuesService =
            "  services:\n"+
                "    envQualifier:\n"
        if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {

            valuesService=valuesService+
                "      stable:\n"+
                "        id: "+this.pathFeature.toLowerCase()+"\n"+
                "        targetColour: B\n"
        }else if (isSampleApp()) {
            valuesService=valuesService+
                "      stable:\n"+
                "        id: "+BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure,true).toLowerCase()+"\n"+
                "        targetColour: B\n"
        }else {
            if (cloudAppState==CloudWorkflowStates.NEW_DEPLOY) {
                if (currentColour==null) { //Primera Major
                    valuesService=valuesService+
                        "      stable:\n"+
                        "        id: "+BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure).toLowerCase()+"\n"+
                        //"        id: "+pomXml.getBmxAppId().toLowerCase()+"\n"+
                        "        targetColour: K\n"+
                        "      new:\n"+
                        "        id: "+suffixedComponentName.replace("<componentName>", BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure)).toLowerCase()+"\n"+
                        "        targetColour: "+getNewColour()+"\n"+addAditionalRoute("K")
                }else {
                    valuesService=valuesService+
                        "      stable:\n"+
                        "        id: "+BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure).toLowerCase()+"\n"+
                        "        targetColour: "+currentColour+"\n"+
                        "      new:\n"+
                        "        id: "+suffixedComponentName.replace("<componentName>", BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure)).toLowerCase()+"\n"+
                        "        targetColour: "+currentColour+"\n"+addAditionalRoute(currentColour)
                }
            }else if (cloudAppState==CloudWorkflowStates.ELIMINATE_NEW_ROUTE_TO_CURRENT_APP) {
                valuesService=valuesService+
                    "      stable:\n"+
                    "        id: "+BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure).toLowerCase()+"\n"+
                    "        targetColour: "+currentColour+"\n"+
                    "      new:\n"+
                    "        id: "+suffixedComponentName.replace("<componentName>", BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure)).toLowerCase()+"\n"+
                    "        targetColour: "+getNewColour()+"\n"+addAditionalRoute(currentColour)
            }else if (cloudAppState==CloudWorkflowStates.ADD_STABLE_ROUTE_TO_NEW_APP) {
                valuesService=valuesService+
                    "      stable:\n"+
                    "        id: "+BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure).toLowerCase()+"\n"+
                    "      new:\n"+
                    "        id: "+suffixedComponentName.replace("<componentName>", BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure)).toLowerCase()+"\n"+
                    "        targetColour: "+getNewColour()+"\n"+addAditionalRoute(null)
            }else {
                valuesService=valuesService+
                    "      stable:\n"+
                    "        id: "+BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure).toLowerCase()+"\n"+
                    "        targetColour: "+getNewColour()+"\n"+
                    "      new:\n"+
                    "        id: "+suffixedComponentName.replace("<componentName>",BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure)).toLowerCase()+"\n"+
                    "        targetColour: "+getNewColour()+"\n"+addAditionalRoute(getNewColour())
            }
        }
        return valuesService
    }

    String getNewReadinessProbePath(String archVersion) {
        if (pomXml.isApplicationWithNewHealthGroups()) {
            return "/actuator/health/readiness"
        }
        return "/actuator/health"
    }

    String getNewLivenessProbePath(String archVersion) {
        if (pomXml.isApplicationWithNewHealthGroups()) {
            return "/actuator/health/liveness"
        }
        return "/actuator/health"
    }

}
