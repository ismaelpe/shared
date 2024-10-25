import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.ICPApiResponse
import com.caixabank.absis3.ICPAppResources
import com.caixabank.absis3.KpiAlmEvent
import com.caixabank.absis3.KpiAlmEventOperation
import com.caixabank.absis3.KpiAlmEventStage
import groovy.json.JsonSlurperClassic

def call(Map valuesDeployed, String app, String center, String namespace, String environment, boolean startOrStop, String stableOrNewOrBoth, String garAppName, String jvmConfig, def scalingMap = [:], String workspace=null) {
	startAndStopApp(valuesDeployed,app,center,namespace,environment,startOrStop,stableOrNewOrBoth,garAppName,jvmConfig,scalingMap, null,workspace)
}

def call(Map valuesDeployed, String app, String center, String namespace, String environment, boolean startOrStop, String stableOrNewOrBoth, String garAppName, String jvmConfig, def scalingMap = [:], ICPAppResources icpAppResources, String workspace=null ) {
	startAndStopApp(valuesDeployed,app,center,namespace,environment,startOrStop,stableOrNewOrBoth,garAppName,jvmConfig,scalingMap, null, null,workspace)
}

def isAnyToScale(Map valuesDeployed) {
	if  (valuesDeployed.scaleCPUCores!='NO' || valuesDeployed.scaleMemory!='NO' || valuesDeployed.scaleNumInstances!='NO' ) {
		return true
	}else {
		return false
	}
}

def call(Map valuesDeployed, String app, String center, String namespace, String environment, boolean startOrStop, String stableOrNewOrBoth, String garAppName, String jvmConfig, def scalingMap = [:], ICPAppResources icpAppResources, def  type, String workspace=null ) {

    if(workspace==null)
        workspace=""
    else workspace = "/${workspace}"

    String appICPId = namespace=="ARCH" ? GlobalVars.ICP_APP_ID_ARCH : GlobalVars.ICP_APP_ID_APPS
	String appICP = namespace=="ARCH" ? GlobalVars.ICP_APP_ARCH : GlobalVars.ICP_APP_APPS

    String componentId="0"
	ICPApiResponse response = sendRequestToICPApi("v1/application/${appICPId}/component",null,"GET","${appICP}","", false, false)
	if (response.statusCode>=200 && response.statusCode<300 && valuesDeployed!=null) {
		
		if (response.body!=null && response.body.size()>=1) {
            componentId = sh(script: "jq '.[] | select(.name==\"${app}\").id' ${env.WORKSPACE}${workspace}@tmp/outputCommand.json ", returnStdout:true ).trim()
            if ("".equals(componentId)) {
                componentId="0"    
            }

            if (icpAppResources) {
                printOpen("The component Id is '${componentId}' los recursos son de ${icpAppResources}",EchoLevel.INFO)
            } else {
                printOpen("The component Id is '${componentId}'",EchoLevel.INFO)
            }
			
			response = sendRequestToICPApi("v1/application/PCLD/${appICP}/component/${componentId}/environment/${environment.toUpperCase()}/availabilityzone/${center}/status",null,"GET","${appICP}","",false,false)

            analyzeResponseAndThrowExceptionIfApplicable(response, center, true)

            def appMetadata = [
                environment: "${environment}",
                app: "${app}",
                garAppName: "${garAppName}",
                namespace: "${namespace}"
            ]

            def valuesDeployedLocalEnvVarsList = retrieveNewAndStableAppICPDeploymentMetadata(valuesDeployed)

            if (icpAppResources) {
                printOpen("Los jvm son de #${icpAppResources?.jvmArgs}# #${jvmConfig}#",EchoLevel.INFO)
            } 
			
            if ((stableOrNewOrBoth=="BOTH" || stableOrNewOrBoth=="STABLE") && valuesDeployedLocalEnvVarsList["stable"] != null ) {

                Map app1 = valuesDeployedLocalEnvVarsList["stable"]
                icpAppResources=configureAppScaling(app1, startOrStop, scalingMap, appMetadata, icpAppResources)
				configureJvm(app1, startOrStop, icpAppResources.getJvmConfig(jvmConfig))
				printOpen("El Json a desplegar es de  ${app1}", EchoLevel.INFO)
            }
            if ((stableOrNewOrBoth=="BOTH" || stableOrNewOrBoth=="NEW") && valuesDeployedLocalEnvVarsList["new"]!=null ) {

                Map app1 = valuesDeployedLocalEnvVarsList["new"]
                icpAppResources=configureAppScaling(app1, startOrStop, scalingMap, appMetadata, icpAppResources)
				configureJvm(app1, startOrStop, icpAppResources.getJvmConfig(jvmConfig))
				printOpen("El Json a desplegar es es de  ${app1}", EchoLevel.INFO)
            }
			

			
			def body = [
				az: "${center}",
				environment: "${environment.toUpperCase()}",
				values: "${objectsParseUtils.toYamlString(valuesDeployed)}"
			]
			printOpen("El Json a desplegar ed de  ${body}", EchoLevel.INFO)
			
			response = sendRequestToICPApi("v1/application/PCLD/${appICP}/component/${componentId}/deploy",body,"POST","${appICP}","v1/application/PCLD/${appICP}/component/${componentId}/deploy",true,true)

            if (response?.body?.id != 0) {
                //Tenemos que actualizar el catalogo... con los nuevos valores
                printOpen("Se pasara a validar el ${type}", EchoLevel.ALL)
                //No se tiene que llamar al catalogo sino toca
                if (type!=null && icpAppResources!=null && isAnyToScale(valuesDeployed)) {
                    updateCat(type,app,environment,garAppName,icpAppResources)
                } else {
                    printOpen("No se ha indicado el tipo de artefacto o no han modificado nada de nada", EchoLevel.ALL)
                }
            } else {
                throw new Exception("Invalid response code from ICP API: ${response?.body?.id}")                        
            }
		}

	} else {
		printOpen("APP NOT FOUND", EchoLevel.ALL)
	}

	return valuesDeployed
}

private def updateCat(def type, def app, def environment, def garApp, ICPAppResources icpResources) {
	if (env.SEND_TO_ABSIS3_CATALOG!="" && env.SEND_TO_ABSIS3_CATALOG=="true" && icpResources!=null) {
		
		def major=app-garApp
		//https://catmsv-micro-server-1.pro.int.srv.caixabank.com/app/ARQ.MIA/catmsv/version/1/environment/PRO

        def response = sendRequestToAbsis3MS(
            'GET',			
            "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${type}/${garApp}/version/${major}/environment/${environment}",			
            null,
            "${GlobalVars.CATALOGO_ABSIS3_ENV}",		
			
            [
                kpiAlmEvent: new KpiAlmEvent(
                    null, null,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.CATMSV_HTTP_CALL)
            ])

		if (response.status == 200) {
			//def json = new JsonSlurperClassic().parseText(response.content)
			def json = response.content
			printOpen("Recuperado los datos ${json}", EchoLevel.ALL)
			
			updateCatMsv(type,garApp,major,json.minor,json.fix,json.typeVersion,environment,icpResources)
		}else {
			printOpen("Error al proceder al despliegue del micro", EchoLevel.ALL)
		}
	}		
} 

//Tenemos que recoger esta info del catalogo
private def updateCatMsv(def type, def app, def major, def minor, def fix, def typeVersion, def environmetNew, ICPAppResources icpResources) {
	if (env.SEND_TO_ABSIS3_CATALOG!="" && env.SEND_TO_ABSIS3_CATALOG=="true" && icpResources!=null) {

		def deployParams = 
		   [ 
			 deploy:
			   [
				   replicas: icpResources.getNumInstances(environmetNew),
				   memoryLimits: icpResources.getLimitsMemory(environmetNew)-'Mi',
				   memoryRequests: icpResources.getRequestsMemory(environmetNew)-'Mi',
				   cpuLimits: icpResources.getLimitsCPU(environmetNew)-'m',
				   cpuRequests: icpResources.getRequestsCPU(environmetNew)-'m'			
			   ],
			   srvEnvId: environmetNew.toUpperCase()
		   ]

		def response = sendRequestToAbsis3MS(
            'PUT',
            "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${type}/${app}/version/${major}/${minor}/${fix}/${typeVersion}/deploy",
            deployParams,
            "${GlobalVars.CATALOGO_ABSIS3_ENV}",			
            [
                kpiAlmEvent: new KpiAlmEvent(
                    null, null,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.CATMSV_HTTP_CALL)
            ])

		if (response.status == 200) {
			printOpen("Deploy realizado", EchoLevel.ALL)
		}else {
			printOpen("Error al proceder al despliegue del micro", EchoLevel.ALL)
		}
	}
}


private void analyzeResponseAndThrowExceptionIfApplicable(def response, String center, boolean force = false) {

    if (response.statusCode>=200 && response.statusCode<300) {

        if (response.body==null) {
            printOpen("El micro no existe posiblemente es un primer deploy", EchoLevel.ALL)
            throw new Exception("El micro no esta desplegado")
        }

        if (!force) {
            if (center!="ALL") {
                if (response.body.size()==1 && response.body.getAt(0).items!=null && response.body.getAt(0).items.size()!=0) {
                    printOpen("Existe una version anterior del micro ${response.body.getAt(0).items} ", EchoLevel.ALL)
                }else {
                    printOpen("El micro no existe posiblemente es un primer deploy", EchoLevel.ALL)
                    throw new Exception ("El micro no esta desplegado")
                }
            }
            if (center=="ALL") {
                if (response.body.size()==2 && response.body.getAt(0).items!=null && response.body.getAt(0).items.size()!=0) {
                    printOpen("Existe una version anterior del micro ${response.body.getAt(0).items} ", EchoLevel.ALL)
                }else {
                    printOpen("El micro no existe posiblemente es un primer deploy", EchoLevel.ALL)
                    throw new Exception ("El micro no esta desplegado")
                }
            }
        }

    }

}

private Map retrieveNewAndStableAppICPDeploymentMetadata(Map valuesDeployed) {

    if (valuesDeployed["absis"]!=null) {

        Map valuesDeployedLocal=valuesDeployed["absis"]


        if (valuesDeployedLocal["apps"]!=null) {

            Map valuesDeployedLocalApp=valuesDeployedLocal["apps"]

            if (valuesDeployedLocalApp["envQualifier"]!=null) {

                def valuesDeployedLocalEnvVarsList = valuesDeployedLocalApp["envQualifier"]
                return valuesDeployedLocalEnvVarsList
            }

        }
    }

    return [:]
}

def getNumInstances(String environment, ICPAppResources resourcesICP) {
	
	return Integer.parseInt(resourcesICP.getNumInstances(environment,false))
}


private ICPAppResources configureAppScaling(Map appICPDeploymentMetadata, boolean startOrStop, Map scalingMap, Map appMetadata = [:], ICPAppResources icpAppResources) {
	
    String cpuCoresNewSize = scalingMap.scaleCPUCores
    String memoryNewSize = scalingMap.scaleMemory
    String numberInstancesNewSize = scalingMap.scaleNumInstances

    String environment = appMetadata.environment
    String app = appMetadata.app
    String garAppName = appMetadata.garAppName
    String namespace = appMetadata.namespace

    ICPAppResources resourcesICP = new ICPAppResources()
	if (icpAppResources!=null) resourcesICP=icpAppResources
    resourcesICP.environment=environment.toUpperCase()
    resourcesICP.isArchProject = namespace=="ARCH"

	printOpen("El appICPDeploymentMetadata ${appICPDeploymentMetadata}",EchoLevel.INFO)
	printOpen("El icpAppResources ${icpAppResources}",EchoLevel.INFO)
	printOpen("El numberInstancesNewSize ${numberInstancesNewSize}",EchoLevel.INFO)
	printOpen("El sscalingMap ${scalingMap}",EchoLevel.INFO)
	printOpen("El startOrStop ${startOrStop}",EchoLevel.INFO)
	
    if (startOrStop) {

        if (numberInstancesNewSize == 'DEFAULT') {

            appICPDeploymentMetadata.put("replicas", getNumInstances(environment,app,garAppName))

        } else {
			if (numberInstancesNewSize != 'NO') {				
				appICPDeploymentMetadata.put("replicas",getNumInstances(environment,resourcesICP))
				printOpen("Numero de instancias1 ${getNumInstances(environment,resourcesICP)}",EchoLevel.INFO)
				printOpen("Numero de instancias2 ${appICPDeploymentMetadata})",EchoLevel.INFO)
			}
			/*
            def instancesSizing = [
                S: 1,
                M: 2,
                L: 3,
				XL: 4,
				XXL: 5,
				XXXL: 6
            ]*/
			
            //appICPDeploymentMetadata.put("replicas", instancesSizing[numberInstancesNewSize])

        }

        if (cpuCoresNewSize!='NO') {

            appICPDeploymentMetadata.put("requests_cpu", getRequestsCpu(resourcesICP, cpuCoresNewSize))
            appICPDeploymentMetadata.put("limits_cpu", getLimitsCpu(resourcesICP, cpuCoresNewSize))
			appICPDeploymentMetadata.put("limits_cpu", getLimitsCpu(resourcesICP, cpuCoresNewSize))
			

        }
        if (memoryNewSize!='NO') {

            appICPDeploymentMetadata.put("requests_memory", getRequestsMemory(resourcesICP,memoryNewSize))
            appICPDeploymentMetadata.put("limits_memory", getLimitsMemory(resourcesICP,memoryNewSize))
			appICPDeploymentMetadata.put("limits_cpu", getLimitsCpu(resourcesICP, cpuCoresNewSize))

        }



    } else {
        appICPDeploymentMetadata.put("replicas", 0)
    }

	printOpen("El ${resourcesICP} para el nuevo start y el original es de ${icpAppResources}",EchoLevel.INFO)
	
    return resourcesICP
}

private void configureJvm(Map appICPDeploymentMetadata, boolean startOrStop, String jvmConfig) {
    if (startOrStop && jvmConfig!=null && !"".equals(jvmConfig)) {
		printOpen("Adding JVM Config: ${jvmConfig}", EchoLevel.INFO)
		if(!appICPDeploymentMetadata.containsKey("envVars")) {
			printOpen("Creating envVars map since it doesn't exist", EchoLevel.INFO)
			appICPDeploymentMetadata.put("envVars", [:])
		}
		appICPDeploymentMetadata.get("envVars").put("jvmConfig", jvmConfig)
	}else {
		printOpen("No tenemos jvmconfig en la entrada", EchoLevel.INFO)
		//Revisaremos que no tenga nada raro el jvmConfig
		if(appICPDeploymentMetadata.containsKey("envVars")) {
			//Tenemos envVars del estable o no
			if (appICPDeploymentMetadata.get("envVars").containsKey("jvmConfig")) {
				String actualJvmconfig=appICPDeploymentMetadata.get("envVars").get("jvmConfig")
			    if (actualJvmconfig==null || "null".equals(actualJvmconfig.trim())) {
					printOpen("Procedemos a eliminar el jvmconfig", EchoLevel.INFO)
					appICPDeploymentMetadata.get("envVars").remove("jvmConfig")
				}else {
					printOpen("Tiene un jvmConfig correcto ${appICPDeploymentMetadata.get('envVars').get('jvmConfig')}", EchoLevel.INFO)
				}
			}else {
				printOpen("El envVars no tiene jvmConfig", EchoLevel.INFO)
			}
		}else {
			printOpen("No tenemos envVars", EchoLevel.INFO)
		}
	}
	printOpen("El nuevo deployment map es de ${appICPDeploymentMetadata}", EchoLevel.INFO)
}

def getNumInstances(String environment, String app, String garAppName) {


    if (environment.toUpperCase()=="PRO") {
        if (env.ICP_REPLICA_SIZE_S!=null && env.ICP_REPLICA_SIZE_S.contains(garAppName)) return 1
        else if (env.ICP_REPLICA_SIZE_L!=null && env.ICP_REPLICA_SIZE_L.contains(garAppName)) return 3
		else if (env.ICP_REPLICA_SIZE_XL!=null && env.ICP_REPLICA_SIZE_XL.contains(garAppName)) return 4
		else if (env.ICP_REPLICA_SIZE_XXL!=null && env.ICP_REPLICA_SIZE_XXL.contains(garAppName)) return 5
		else if (env.ICP_REPLICA_SIZE_XXXL!=null && env.ICP_REPLICA_SIZE_XXXL.contains(garAppName)) return 6
        return 2
    }else {
        return 1
    }
}

def getRequestsCpu(ICPAppResources resourcesICP,String newSize) {
    resourcesICP.cpuSize=newSize

    return resourcesICP.getRequestCPUPersonalized()
}

def getRequestsMemory(ICPAppResources resourcesICP,String newSize) {
    resourcesICP.memSize=newSize
    return resourcesICP.getRequestMemoryPersonalized()
}

def getLimitsCpu(ICPAppResources resourcesICP,String newSize) {
    resourcesICP.cpuSize=newSize
    return resourcesICP.getLimitsCPUPersonalized()
}

def getLimitsMemory(ICPAppResources resourcesICP,String newSize) {
    resourcesICP.memSize=newSize
    return resourcesICP.getLimitsMemoryPersonalized()
}
