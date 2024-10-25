package com.caixabank.absis3


import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.BmxStructure
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.ICPWorkflowStates
import com.caixabank.absis3.ICPAppResources
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.ICPDeployStructure
import com.caixabank.absis3.BranchType
import com.caixabank.absis3.BranchStructure
import com.caixabank.absis3.ArtifactSubType
import com.caixabank.absis3.MavenVersionUtilities
import com.caixabank.absis3.Utilities
import java.util.Random
import org.apache.commons.lang.RandomStringUtils

class ICPStateUtility {

    /**
     * Generate
     absis:
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
    ICPAppResources icpResources
    PipelineData pipelineData
    PomXmlStructure pomXml
    String buildId
    ICPDeployStructure icpDeployStructure

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

    String nameComponentInICP
    String aplicacion
    String deployId
    String additionalRoute=null
    boolean sampleAppFlag=false
    String microType=null

    ICPWorkflowStates icpAppState

    def scriptContext

    ICPStateUtility(PipelineData pipelineData,PomXmlStructure pomXml, String aplicacion, String majorMicro) {
        this.pipelineData=pipelineData
        this.pomXml=pomXml
        this.aplicacion = aplicacion
        this.nameComponentInICP = aplicacion+majorMicro

        if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
            this.nameComponentInICP=this.nameComponentInICP + pipelineData.branchStructure.featureNumber
            this.pathFeature=""
        }
        if (pomXml.artifactSubType!=ArtifactSubType.MICRO_APP && pomXml.artifactSubType!=ArtifactSubType.MICRO_ARCH) {
            //Es una sample app
            this.nameComponentInICP=this.nameComponentInICP+"S"
            this.sampleAppFlag = true
        }

        this.environment=pipelineData.bmxStructure.environment.toUpperCase()
    }

    ICPStateUtility(PipelineData pipelineData,PomXmlStructure pomXml,String newImage, ICPk8sActualStatusInfo actualStatusInfo, ICPWorkflowStates icpAppState, String aplicacion, String majorMicro) {
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
        this.nameComponentInICP = aplicacion+majorMicro

        if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
            this.nameComponentInICP=this.nameComponentInICP + pipelineData.branchStructure.featureNumber
            this.pathFeature=""
        }
        if (pomXml.artifactSubType != ArtifactSubType.MICRO_APP && pomXml.artifactSubType != ArtifactSubType.MICRO_ARCH) {
            //Es una sample app
            this.nameComponentInICP = this.nameComponentInICP+"S"
        }

        this.icpAppState=icpAppState
        this.environment=pipelineData.bmxStructure.environment.toUpperCase()
    }

    ICPStateUtility(PipelineData pipelineData,PomXmlStructure pomXml,String newImage, String currentImage, String currentColour, String currentVersion, String currentReadinessProbePath, String currentLivenessProbePath, ICPWorkflowStates icpAppState, String aplicacion, String majorMicro) {
        this.pipelineData=pipelineData
        this.pomXml=pomXml
        this.newImage=newImage
        this.currentColour=currentColour
        this.currentVersion=currentVersion
        this.currentImage=currentImage
        this.currentReadinessProbePath = currentReadinessProbePath
        this.currentLivenessProbePath = currentLivenessProbePath

        this.aplicacion = aplicacion
        this.nameComponentInICP = aplicacion+majorMicro

        if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
            this.nameComponentInICP=this.nameComponentInICP + pipelineData.branchStructure.featureNumber
            this.pathFeature=""
        }
        if (pomXml.artifactSubType!=ArtifactSubType.MICRO_APP && pomXml.artifactSubType!=ArtifactSubType.MICRO_ARCH) {
            //Es una sample app
            this.nameComponentInICP=this.nameComponentInICP+"S"
        }

        this.icpAppState=icpAppState
        this.environment=pipelineData.bmxStructure.environment.toUpperCase()

    }

    ICPStateUtility(PipelineData pipelineData,PomXmlStructure pomXml,String newImage, String currentColour, String currentVersion, String currentReadinessProbePath, String currentLivenessProbePath, ICPWorkflowStates icpAppState) {
        this.pipelineData=pipelineData
        this.pomXml=pomXml
        this.newImage=newImage
        this.currentColour=currentColour
        this.icpAppState=icpAppState
        this.currentVersion=currentVersion
        this.currentReadinessProbePath = currentReadinessProbePath
        this.currentLivenessProbePath = currentLivenessProbePath
        this.environment=pipelineData.bmxStructure.environment.toUpperCase()

        aplicacion = MavenUtils.sanitizeArtifactName(pomXml.artifactName, pipelineData.garArtifactType).toLowerCase()

        nameComponentInICP=aplicacion+pomXml.getArtifactMajorVersion()

        if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
            nameComponentInICP=aplicacion + majorMicro + pipelineData.branchStructure.featureNumber
            this.pathFeature=""
        }
        if (pomXml.artifactSubType!=ArtifactSubType.MICRO_APP && pomXml.artifactSubType!=ArtifactSubType.MICRO_ARCH) {
            //Es una sample app
            this.nameComponentInICP=this.nameComponentInICP+"S"
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

    ICPWorkflowStates getNextStateWorkflow() {
        if (icpAppState==ICPWorkflowStates.NEW_DEPLOY) {
            if (currentColour==null) {
                return ICPWorkflowStates.ADD_STABLE_ROUTE_TO_NEW_APP
            }
            else return ICPWorkflowStates.ELIMINATE_NEW_ROUTE_TO_CURRENT_APP
        }else if (icpAppState==ICPWorkflowStates.ELIMINATE_NEW_ROUTE_TO_CURRENT_APP) {
            return ICPWorkflowStates.ADD_STABLE_ROUTE_TO_NEW_APP
        }else if (icpAppState==ICPWorkflowStates.ADD_STABLE_ROUTE_TO_NEW_APP) {
            if (currentColour==null) return ICPWorkflowStates.ELIMINATE_CURRENT_APP
            else return ICPWorkflowStates.ELIMINATE_STABLE_ROUTE_TO_CURRENT_APP
        }else if (icpAppState==ICPWorkflowStates.ELIMINATE_STABLE_ROUTE_TO_CURRENT_APP) {
            return ICPWorkflowStates.ELIMINATE_CURRENT_APP
        }else return ICPWorkflowStates.END
    }

    String getNumReplicas() {
        if (this.environment!=null && this.environment.equals("PRO")) {
            return icpResources.getNumInstances(environment)
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
        String applicationName = nameComponentInICP;

        if (pipelineData.branchStructure.branchType == BranchType.FEATURE) {
			applicationName=applicationName.replace('-','')
            if(applicationName.length() > GlobalVars.LIMIT_LENGTH_FOR_PODNAME_WITHOUT_K8_SUFFIX) {
                applicationName = applicationName.substring(0, GlobalVars.LIMIT_LENGTH_FOR_PODNAME_WITHOUT_K8_SUFFIX);
            }
        }
        return "absis:\n"+
            "  app:\n"+
            "    loggingElkStack: absis30\n"+
            "    replicas: 1\n"+
            "    instance: "+applicationName+"\n"+
            "    name: "+aplicacion+"\n"+
            getValueOfMicroType()+
            icpResources.getChartValues(this.environment)+
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
        //No tienee la version por alguna mejora en el api de icp
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

            String featureAppName = nameComponentInICP.toLowerCase().replace('-','') + "-b";
			
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
                "          SPRING_PROFILES_ACTIVE: "+icpDeployStructure.springProfilesActive+"\n"+
                forceDeploy+
                icpResources.getChartAppValues(this.environment)
        }else if (isSampleApp()) {
            //Es una sample app debe ser eliminada

            String sampleAppName = nameComponentInICP.toLowerCase()+"-"+getNewColour().toLowerCase()+getLastDeployId();
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
                "          SPRING_PROFILES_ACTIVE: "+icpDeployStructure.springProfilesActive+"\n"+
                forceDeploy+
                icpResources.getChartAppValues(this.environment)
        }else {
            if (icpAppState==ICPWorkflowStates.NEW_DEPLOY ||  icpAppState==ICPWorkflowStates.ADD_STABLE_ROUTE_TO_NEW_APP) {
                if (currentColour==null) { //Primera Major
                    valuesApps=valuesApps+
                        "      stable:\n"+
                        "        id: "+nameComponentInICP.toLowerCase()+"-"+getNewColour().toLowerCase()+getLastDeployId()+"\n"+
                        "        colour: "+getNewColour()+"\n"+
                        "        image: "+newImagePlusVersion+"\n"+
                        "        version: "+pomXml.artifactVersion+"\n"+
                        "        stable: false\n"+
                        "        new: false\n"+
                        "        replicas: "+getNumReplicas()+"\n"+
                        "        readinessProbePath: "+getNewReadinessProbePath(pomXml.archVersion)+"\n"+
                        "        livenessProbePath: "+getNewLivenessProbePath(pomXml.archVersion)+"\n"+
                        "        envVars:\n"+
                        "          SPRING_PROFILES_ACTIVE: "+icpDeployStructure.springProfilesActive+"\n"+
                        getJvmConfigFromICPResource() +
                        icpResources.getChartAppValues(this.environment)
                }else {
                    valuesApps=valuesApps+
                        "      stable:\n"+
                        "        id: "+nameComponentInICP.toLowerCase()+"-"+currentColour.toLowerCase()+"\n"+
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
                        icpResources.getChartAppValues(this.environment)+
                        "      new:\n"+
                        "        id: "+nameComponentInICP.toLowerCase()+"-"+getNewColour().toLowerCase()+getLastDeployId()+"\n"+
                        "        colour: "+getNewColour()+"\n"+
                        "        image: "+newImagePlusVersion+"\n"+
                        "        version: "+pomXml.artifactVersion+"\n"+
                        "        stable: false\n"+
                        "        new: false\n"+
                        "        replicas: "+getNumReplicas()+"\n"+
                        "        readinessProbePath: "+getNewReadinessProbePath(pomXml.archVersion)+"\n"+
                        "        livenessProbePath: "+getNewLivenessProbePath(pomXml.archVersion)+"\n"+
                        "        envVars:\n"+
                        "          SPRING_PROFILES_ACTIVE: "+icpDeployStructure.springProfilesActive+"\n"+
                        getJvmConfigFromICPResource() +
                        forceDeploy+
                        icpResources.getChartAppValues(this.environment)
                }
            }else if (icpAppState==ICPWorkflowStates.ELIMINATE_NEW_ROUTE_TO_CURRENT_APP || icpAppState==ICPWorkflowStates.ELIMINATE_STABLE_ROUTE_TO_CURRENT_APP) {
                valuesApps=valuesApps+
                    "      stable:\n"+
                    "        id: "+nameComponentInICP.toLowerCase()+"-"+currentColour.toLowerCase()+"\n"+
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
                    icpResources.getChartAppValues(this.environment)+
                    "      new:\n"+
                    "        id: "+nameComponentInICP.toLowerCase()+"-"+getNewColour().toLowerCase()+getLastDeployId()+"\n"+
                    "        colour: "+getNewColour()+"\n"+
                    "        image: "+newImagePlusVersion+"\n"+
                    "        version: "+pomXml.artifactVersion+"\n"+
                    "        stable: false\n"+
                    "        new: false\n"+
                    "        replicas: "+getNumReplicas()+"\n"+
                    "        readinessProbePath: "+getNewReadinessProbePath(pomXml.archVersion)+"\n"+
                    "        livenessProbePath: "+getNewLivenessProbePath(pomXml.archVersion)+"\n"+
                    "        envVars:\n"+
                    "          SPRING_PROFILES_ACTIVE: "+icpDeployStructure.springProfilesActive+"\n"+
                    getJvmConfigFromICPResource() +
                    forceDeploy+
                    icpResources.getChartAppValues(this.environment)
            }else if (icpAppState==ICPWorkflowStates.ELIMINATE_CURRENT_APP) {
                valuesApps=valuesApps+
                    "      stable:\n"+
                    "        id: "+nameComponentInICP.toLowerCase()+"-"+getNewColour().toLowerCase()+getLastDeployId()+"\n"+
                    "        colour: "+getNewColour()+"\n"+
                    "        image: "+newImagePlusVersion+"\n"+
                    "        version: "+pomXml.artifactVersion+"\n"+
                    "        stable: false\n"+
                    "        new: false\n"+
                    "        replicas: "+getNumReplicas()+"\n"+
                    "        readinessProbePath: "+getNewReadinessProbePath(pomXml.archVersion)+"\n"+
                    "        livenessProbePath: "+getNewLivenessProbePath(pomXml.archVersion)+"\n"+
                    "        envVars:\n"+
                    "          SPRING_PROFILES_ACTIVE: "+icpDeployStructure.springProfilesActive+"\n"+
                    getJvmConfigFromICPResource() +
                    icpResources.getChartAppValues(this.environment)

                if (currentColour!=null && !"".equals(currentColour) && !environment.equals("DEV")) {
                    valuesApps=valuesApps+
                        "      old:\n"+
                        "        id: "+nameComponentInICP.toLowerCase()+"-"+currentColour.toLowerCase()+"\n"+
                        "        colour: "+currentColour+"\n"+
                        "        image: "+currentImage+"\n"+
                        "        version: "+currentVersion+"\n"+
                        "        stable: false\n"+
                        "        new: false\n"+
                        "        replicas: 0\n"+
                        (currentReadinessProbePath==null?"":"        readinessProbePath: "+currentReadinessProbePath+"\n")+
                        (currentLivenessProbePath==null?"":"        livenessProbePath: "+currentLivenessProbePath+"\n")+
                        getEnvVarsYmlFormat(currentEnvVars)+
                        icpResources.getChartAppValues(this.environment)
                }
            }
        }
        return valuesApps
    }

    String getJvmConfigFromICPResource() {
        if (icpResources != null) {
            def jvmConfig = icpResources.jvmArgs
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
            if (icpAppState==ICPWorkflowStates.NEW_DEPLOY) {
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
            }else if (icpAppState==ICPWorkflowStates.ELIMINATE_NEW_ROUTE_TO_CURRENT_APP) {
                valuesService=valuesService+
                    "      stable:\n"+
                    "        id: "+BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure).toLowerCase()+"\n"+
                    "        targetColour: "+currentColour+"\n"+
                    "      new:\n"+
                    "        id: "+suffixedComponentName.replace("<componentName>", BmxUtilities.calculateArtifactId(pomXml,pipelineData.branchStructure)).toLowerCase()+"\n"+
                    "        targetColour: "+getNewColour()+"\n"+addAditionalRoute(currentColour)
            }else if (icpAppState==ICPWorkflowStates.ADD_STABLE_ROUTE_TO_NEW_APP) {
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
