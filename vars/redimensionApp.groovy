import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.CloudApiResponse
import com.project.alm.CloudAppResources
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage
import groovy.json.JsonSlurperClassic


def isAnyToScale(Map valuesDeployed) {
	if  (valuesDeployed.scaleCPUCores!='NO' || valuesDeployed.scaleMemory!='NO' || valuesDeployed.scaleNumInstances!='NO' ) {
		return true
	}else {
		return false
	}
}

def call(Map valuesDeployed, String app, String center, String namespace, String environment, String stableOrNewOrBoth, String garAppName, String jvmConfig, def scalingMap = [:], CloudAppResources cloudAppResources, def  type ) {

    String appCloudId = namespace=="ARCH" ? GlobalVars.Cloud_APP_ID_ARCH : GlobalVars.Cloud_APP_ID_APPS
	String appCloud = namespace=="ARCH" ? GlobalVars.Cloud_APP_ARCH : GlobalVars.Cloud_APP_APPS

    String componentId="0"
	
	CloudApiResponse response = sendRequestToCloudApi("v1/application/${appCloudId}/component",null,"GET","${appCloud}","", false, false)
	
	if (response.statusCode>=200 && response.statusCode<300 && valuesDeployed!=null) {
		
		if (response.body!=null && response.body.size()>=1) {
            componentId = sh(script: "jq '.[] | select(.name==\"${app}\").id' ${env.WORKSPACE}@tmp/outputCommand.json ", returnStdout:true ).trim()
            if ("".equals(componentId)) {
                componentId="0"    
            }

            printOpen("The component Id is '${componentId}' los recursos son de ${cloudAppResources}",EchoLevel.INFO)
			
			response = sendRequestToCloudApi("v1/application/PCLD/${appCloud}/component/${componentId}/environment/${environment.toUpperCase()}/availabilityzone/${center}/status",null,"GET","${appCloud}","",false,false)

            analyzeResponseAndThrowExceptionIfApplicable(response, center)

            def appMetadata = [
                environment: "${environment}",
                app: "${app}",
                garAppName: "${garAppName}",
                namespace: "${namespace}"
            ]

            def valuesDeployedLocalEnvVarsList = retrieveNewAndStableAppCloudDeploymentMetadata(valuesDeployed)
			
			printOpen("Los jvm son de #${cloudAppResources.jvmArgs}# #${jvmConfig}#",EchoLevel.INFO)

            if ((stableOrNewOrBoth=="BOTH" || stableOrNewOrBoth=="STABLE") && valuesDeployedLocalEnvVarsList["stable"] != null ) {

                Map app1 = valuesDeployedLocalEnvVarsList["stable"]
                cloudAppResources=configureAppScaling(app1, scalingMap, appMetadata, cloudAppResources)
				configureJvm(app1, cloudAppResources.getJvmConfig(jvmConfig))

            }
            if ((stableOrNewOrBoth=="BOTH" || stableOrNewOrBoth=="NEW") && valuesDeployedLocalEnvVarsList["new"]!=null ) {

                Map app1 = valuesDeployedLocalEnvVarsList["new"]
                cloudAppResources=configureAppScaling(app1, scalingMap, appMetadata, cloudAppResources)
				configureJvm(app1, cloudAppResources.getJvmConfig(jvmConfig))

            }
			

			
			def body = [
				az: "${center}",
				environment: "${environment.toUpperCase()}",
				values: "${objectsParseUtils.toYamlString(valuesDeployed)}"
			]
			printOpen("El Json a desplegar ed de  ${body}", EchoLevel.INFO)
			
			response = sendRequestToCloudApi("v1/application/PCLD/${appCloud}/component/${componentId}/deploy",body,"POST","${appCloud}","v1/application/PCLD/${appCloud}/component/${componentId}/deploy",true,true)

            if (response?.body?.id != 0) {
                //Tenemos que actualizar el catalogo... con los nuevos valores
                printOpen("Se pasara a validar el ${type}", EchoLevel.ALL)
                //No se tiene que llamar al catalogo sino toca
                if (type!=null && cloudAppResources!=null && isAnyToScale(valuesDeployed)) {
                    updateCat(type,app,environment,garAppName,cloudAppResources)
                } else {
                    printOpen("No se ha indicado el tipo de artefacto o no han modificado nada de nada", EchoLevel.ALL)
                }
            } else {
                throw new Exception("Invalid response code from Cloud API: ${response?.body?.id}")                        
            }
		}

	} else {
		printOpen("APP NOT FOUND", EchoLevel.ALL)
	}

	return valuesDeployed
}

private def updateCat(def type, def app, def environment, def garApp, CloudAppResources cloudResources) {
	if (env.SEND_TO_ALM_CATALOG!="" && env.SEND_TO_ALM_CATALOG=="true" && cloudResources!=null) {
		
		def major=app-garApp
		//https://catalog-micro-server-1.pro.int.srv.project.com/app/ARQ.MIA/catalog/version/1/environment/PRO

        def response = sendRequestToAlm3MS(
            'GET',			
            "${GlobalVars.URL_CATALOGO_ALM_PRO}/app/${type}/${garApp}/version/${major}/environment/${environment}",			
            null,
            "${GlobalVars.CATALOGO_ALM_ENV}",		
			
            [
                kpiAlmEvent: new KpiAlmEvent(
                    null, null,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.CATALOG_HTTP_CALL)
            ])

		if (response.status == 200) {
			//def json = new JsonSlurperClassic().parseText(response.content)
			def json = response.content
			printOpen("Recuperado los datos ${json}", EchoLevel.ALL)
			
			updateCatalog(type,garApp,major,json.minor,json.fix,json.typeVersion,environment,cloudResources)
		}else {
			printOpen("Error al proceder al despliegue del micro", EchoLevel.ALL)
		}
	}		
} 

//Tenemos que recoger esta info del catalogo
private def updateCatalog(def type, def app, def major, def minor, def fix, def typeVersion, def environmetNew, CloudAppResources cloudResources) {
	if (env.SEND_TO_ALM_CATALOG!="" && env.SEND_TO_ALM_CATALOG=="true" && cloudResources!=null) {

		def deployParams = 
		   [ 
			 deploy:
			   [
				   replicas: cloudResources.getNumInstances(environmetNew),
				   memoryLimits: cloudResources.getLimitsMemory(environmetNew)-'Mi',
				   memoryRequests: cloudResources.getRequestsMemory(environmetNew)-'Mi',
				   cpuLimits: cloudResources.getLimitsCPU(environmetNew)-'m',
				   cpuRequests: cloudResources.getRequestsCPU(environmetNew)-'m'			
			   ],
			   srvEnvId: environmetNew.toUpperCase()
		   ]

		def response = sendRequestToAlm3MS(
            'PUT',
            "${GlobalVars.URL_CATALOGO_ALM_PRO}/app/${type}/${app}/version/${major}/${minor}/${fix}/${typeVersion}/deploy",
            deployParams,
            "${GlobalVars.CATALOGO_ALM_ENV}",			
            [
                kpiAlmEvent: new KpiAlmEvent(
                    null, null,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.CATALOG_HTTP_CALL)
            ])

		if (response.status == 200) {
			printOpen("Deploy realizado", EchoLevel.ALL)
		}else {
			printOpen("Error al proceder al despliegue del micro", EchoLevel.ALL)
		}
	}
}


private void analyzeResponseAndThrowExceptionIfApplicable(def response, String center) {

    if (response.statusCode>=200 && response.statusCode<300) {

        if (response.body==null) {
            printOpen("El micro no existe posiblemente es un primer deploy", EchoLevel.ALL)
            throw new Exception("El micro no esta desplegado")
        }

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

private Map retrieveNewAndStableAppCloudDeploymentMetadata(Map valuesDeployed) {

    if (valuesDeployed["alm"]!=null) {

        Map valuesDeployedLocal=valuesDeployed["alm"]


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

def getNumInstances(String environment, CloudAppResources resourcesCloud) {
	
	return resourcesCloud.getNumInstances(environment,false)
}


private CloudAppResources configureAppScaling(Map appCloudDeploymentMetadata, Map scalingMap, Map appMetadata = [:], CloudAppResources cloudAppResources) {
	
    String cpuCoresNewSize = scalingMap.scaleCPUCores
    String memoryNewSize = scalingMap.scaleMemory
    String numberInstancesNewSize = scalingMap.scaleNumInstances

    String environment = appMetadata.environment
    String app = appMetadata.app
    String garAppName = appMetadata.garAppName
    String namespace = appMetadata.namespace

    CloudAppResources resourcesCloud = new CloudAppResources()
	if (cloudAppResources!=null) resourcesCloud=cloudAppResources
    resourcesCloud.environment=environment.toUpperCase()
    resourcesCloud.isArchProject = namespace=="ARCH"

	printOpen("El appCloudDeploymentMetadata ${appCloudDeploymentMetadata}",EchoLevel.INFO)
	printOpen("El cloudAppResources ${cloudAppResources}",EchoLevel.INFO)
	printOpen("El numberInstancesNewSize ${numberInstancesNewSize}",EchoLevel.INFO)
	printOpen("El scalingMap ${scalingMap}",EchoLevel.INFO)


        if (numberInstancesNewSize == 'DEFAULT') {

            appCloudDeploymentMetadata.put("replicas", getNumInstances(environment,app,garAppName))

        } else {
			if (numberInstancesNewSize != 'NO') {
				appCloudDeploymentMetadata.put("replicas",getNumInstances(environment,resourcesCloud))
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
			
            //appCloudDeploymentMetadata.put("replicas", instancesSizing[numberInstancesNewSize])

        }

        if (cpuCoresNewSize!='NO') {

            appCloudDeploymentMetadata.put("requests_cpu", getRequestsCpu(resourcesCloud, cpuCoresNewSize))
            appCloudDeploymentMetadata.put("limits_cpu", getLimitsCpu(resourcesCloud, cpuCoresNewSize))
			appCloudDeploymentMetadata.put("limits_cpu", getLimitsCpu(resourcesCloud, cpuCoresNewSize))
			

        }
        if (memoryNewSize!='NO') {

            appCloudDeploymentMetadata.put("requests_memory", getRequestsMemory(resourcesCloud,memoryNewSize))
            appCloudDeploymentMetadata.put("limits_memory", getLimitsMemory(resourcesCloud,memoryNewSize))
			appCloudDeploymentMetadata.put("limits_cpu", getLimitsCpu(resourcesCloud, cpuCoresNewSize))

        }


	printOpen("El ${resourcesCloud} para el nuevo start y el original es de ${cloudAppResources}",EchoLevel.INFO)
	
    return resourcesCloud
}

private void configureJvm(Map appCloudDeploymentMetadata, String jvmConfig) {
    if (jvmConfig!=null && !"".equals(jvmConfig)) {
		printOpen("Adding JVM Config: ${jvmConfig}", EchoLevel.INFO)
		if(!appCloudDeploymentMetadata.containsKey("envVars")) {
			printOpen("Creating envVars map since it doesn't exist", EchoLevel.INFO)
			appCloudDeploymentMetadata.put("envVars", [:])
		}
		appCloudDeploymentMetadata.get("envVars").put("jvmConfig", jvmConfig)
	}else {
		printOpen("No tenemos jvmconfig en la entrada", EchoLevel.INFO)
		//Revisaremos que no tenga nada raro el jvmConfig
		if(appCloudDeploymentMetadata.containsKey("envVars")) {
			//Tenemos envVars del estable o no
			if (appCloudDeploymentMetadata.get("envVars").containsKey("jvmConfig")) {
				String actualJvmconfig=appCloudDeploymentMetadata.get("envVars").get("jvmConfig")
			    if (actualJvmconfig==null || "null".equals(actualJvmconfig.trim())) {
					printOpen("Procedemos a eliminar el jvmconfig", EchoLevel.INFO)
					appCloudDeploymentMetadata.get("envVars").remove("jvmConfig")
				}else {
					printOpen("Tiene un jvmConfig correcto ${appCloudDeploymentMetadata.get('envVars').get('jvmConfig')}", EchoLevel.INFO)
				}
			}else {
				printOpen("El envVars no tiene jvmConfig", EchoLevel.INFO)
			}
		}else {
			printOpen("No tenemos envVars", EchoLevel.INFO)
		}
	}
	printOpen("El nuevo deployment map es de ${appCloudDeploymentMetadata}", EchoLevel.INFO)
}

def getNumInstances(String environment, String app, String garAppName) {


    if (environment.toUpperCase()=="PRO") {
        if (env.Cloud_REPLICA_SIZE_S!=null && env.Cloud_REPLICA_SIZE_S.contains(garAppName)) return "1"
        else if (env.Cloud_REPLICA_SIZE_L!=null && env.Cloud_REPLICA_SIZE_L.contains(garAppName)) return "3"
		else if (env.Cloud_REPLICA_SIZE_XL!=null && env.Cloud_REPLICA_SIZE_XL.contains(garAppName)) return "4"
		else if (env.Cloud_REPLICA_SIZE_XXL!=null && env.Cloud_REPLICA_SIZE_XXL.contains(garAppName)) return "5"
		else if (env.Cloud_REPLICA_SIZE_XXXL!=null && env.Cloud_REPLICA_SIZE_XXXL.contains(garAppName)) return "6"
        return "2"
    }else {
        return "1"
    }
}

def getRequestsCpu(CloudAppResources resourcesCloud,String newSize) {
    resourcesCloud.cpuSize=newSize

    return resourcesCloud.getRequestCPUPersonalized()
}

def getRequestsMemory(CloudAppResources resourcesCloud,String newSize) {
    resourcesCloud.memSize=newSize
    return resourcesCloud.getRequestMemoryPersonalized()
}

def getLimitsCpu(CloudAppResources resourcesCloud,String newSize) {
    resourcesCloud.cpuSize=newSize
    return resourcesCloud.getLimitsCPUPersonalized()
}

def getLimitsMemory(CloudAppResources resourcesCloud,String newSize) {
    resourcesCloud.memSize=newSize
    return resourcesCloud.getLimitsMemoryPersonalized()
}
