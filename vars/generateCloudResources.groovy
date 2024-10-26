import com.project.alm.EchoLevel
import com.project.alm.CloudAppResources
import com.project.alm.CloudAppResourcesCatalog
import com.project.alm.GlobalVars
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage


def setCloudResources( def type, def app, def major, def environment, def namespace){
	//CloudAppResources cloudResources=new CloudAppResources()
	CloudAppResourcesCatalog cloudResources=new CloudAppResourcesCatalog()
 
	printOpen("Requesting application's sizes to Open's catalogue...", EchoLevel.INFO)

    def response = sendRequestToAlm3MS(
        'GET',
        "${GlobalVars.URL_CATALOGO_ALM_PRO}/app/${type}/${app}/${major}/config?env=${environment}",
        null,
        "${GlobalVars.CATALOGO_ALM_ENV}",
        [
            kpiAlmEvent: new KpiAlmEvent(
                null, null,
                KpiAlmEventStage.UNDEFINED,
                KpiAlmEventOperation.CATALOG_HTTP_CALL)
        ])
	
	if (response.status == 200) {
        printOpen("Get : ${response.content}", EchoLevel.DEBUG)
	
		//def json = new groovy.json.JsonSlurper().parseText(response.content)
		def json = response.content
		printOpen("Parametros de arranque son de ${json.jvmArgs} ", EchoLevel.DEBUG)
		cloudResources.requestsMemory = "${json.memRequests}"
		cloudResources.requestsCpu="${json.cpuRequests}"
		cloudResources.limitsMemory="${json.memLimits}"
		cloudResources.limitsCpu="${json.cpuLimits}"
		cloudResources.numInstances="${json.replicas}"
		cloudResources.memSize="${json.memSize}".trim()
		cloudResources.cpuSize="${json.cpuSize}".trim()
        if (json.jvmArgs!=null){
            cloudResources.jvmArgs=json.jvmArgs
        }else{
            cloudResources.jvmArgs=null
        }
		cloudResources.replicasSize="${json.replicaSize}".trim()

        printOpen("El tamaño recuperado del dimensionamiento del micro es de ${cloudResources.toString()} este esta en el catalogo", EchoLevel.DEBUG)
		
		// Get : {"id":41,"srvAppId":2417,
		//"cpuSize":"L     ",
		//"memSize":"M     ",
		//"replicaSize":"M     ",
		//"replicas":1,
		//"cpuRequests":600,
		//"cpuLimits":600,
		//"major":4}
		//Si no funciona que hacemos???
	}else {
		//Tendriamos que recuperar la default size para los micros de talla M del entorno en concreto
		//@GetMapping(value = "/config/micro-size/{namespace}/{type}/{env}/{replicas}/{cpu}/{memory}
		cloudResources.memSize='M'
		cloudResources.cpuSize='M'
		cloudResources.replicasSize='M'
  
		printOpen("Requesting default sizes to Open's catalogue for namespace:${namespace} type:${type} app:${app}", EchoLevel.INFO)

		response = sendRequestToAlm3MS(
            'GET',
            "${GlobalVars.URL_CATALOGO_ALM_PRO}/config/micro-size/${namespace}/${type}/${environment}/M/M/M",
            null,
            "${GlobalVars.CATALOGO_ALM_ENV}",
            [
                kpiAlmEvent: new KpiAlmEvent(
                    null, null,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.CATALOG_HTTP_CALL)
            ])

        if (response.status == 200) {
            printOpen("Get : ${response.content}", EchoLevel.DEBUG)
			//def json = new groovy.json.JsonSlurper().parseText(response.content)
			def json = response.content
			cloudResources.requestsMemory = "${json.memRequests}"
			cloudResources.requestsCpu="${json.cpuRequests}"
			cloudResources.limitsMemory="${json.memLimits}"
			cloudResources.limitsCpu="${json.cpuLimits}"
			cloudResources.jvmArgs=json.jvmArgs
			cloudResources.numInstances="${json.replicas}"
   
		}else{
            printOpen("There are no default sizes for namespace:${namespace} type:${type} app:${app}", EchoLevel.INFO)
			return null
		}

        printOpen("The application sizes are ${cloudResources.getValue()}", EchoLevel.INFO)
		
	}
	return cloudResources

}

def call(String memory, String environment, boolean isArchProjectOrSkipMinimum, String applicationNameWithoutVersion, String domain) {
	generateCloudResources(memory,environment,isArchProjectOrSkipMinimum,applicationNameWithoutVersion,domain,null,null,null)
}

def call(String memory, String environment, boolean isArchProjectOrSkipMinimum, String applicationNameWithoutVersion, String domain,String type, String major, String namespace) {
	CloudAppResources cloudResources=null
	if (environment=='EDEN') environment='DEV'
	
	if ( type!=null && env.CATALOG_SIZE!=null && "true".equals(env.CATALOG_SIZE) && major!=null && applicationNameWithoutVersion!=null) {
		//CloudAppResourcesCatalog cloudResources=new CloudAppResourcesCatalog()
		cloudResources=setCloudResources(type,applicationNameWithoutVersion,major,environment,namespace)
		//return cloudResources		
	}
	

	if (cloudResources==null) {

        printOpen("En el catalogo no tenemos la info... tenemos que recurrir al sistema antiguo mediante variables jenkins", EchoLevel.INFO)
		cloudResources=new CloudAppResources()
		cloudResources.environment=environment
		cloudResources.isArchProject=isArchProjectOrSkipMinimum

        printOpen("Vamos a comprobar el size del artifact ${applicationNameWithoutVersion.toLowerCase()} Entorno: ${environment} Memory: ${memory} ArchProject: ${isArchProjectOrSkipMinimum} domain: ${domain} los dominios de ARQ son: ${env.DEMO_DOMAIN_ARQ}", EchoLevel.DEBUG)
		if(domain == null) {
			domain = "NO_DOMAIN"
		}
		
		if ("NO_DOMAIN" != domain && env.DEMO_DOMAIN_ARQ!=null && env.DEMO_DOMAIN_ARQ.indexOf(domain) !=-1) {
			
			cloudResources.replicasSize="S"
			cloudResources.memSize="S"
			cloudResources.cpuSize="S"
			
		} else {
			if (isArchProjectOrSkipMinimum==false && env.Cloud_ALL_APPS_MINIMUM!=null && env.Cloud_ALL_APPS_MINIMUM.contains("true")) cloudResources.allAppsMinimum=true
			else if (env.Cloud_REPLICA_SIZE_S!=null && env.Cloud_REPLICA_SIZE_S.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.replicasSize="S"
			else if (env.Cloud_REPLICA_SIZE_L!=null && env.Cloud_REPLICA_SIZE_L.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.replicasSize="L"
			else if (env.Cloud_REPLICA_SIZE_XL!=null && env.Cloud_REPLICA_SIZE_XL.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.replicasSize="XL"
			else if (env.Cloud_REPLICA_SIZE_XXL!=null && env.Cloud_REPLICA_SIZE_XXL.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.replicasSize="XXL"
			else if (env.Cloud_REPLICA_SIZE_XXXL!=null && env.Cloud_REPLICA_SIZE_XXXL.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.replicasSize="XXXL"
		
			if (env.Cloud_MEM_SIZE_S!=null && env.Cloud_MEM_SIZE_S.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.memSize="S"
			else if (env.Cloud_MEM_SIZE_L!=null && env.Cloud_MEM_SIZE_L.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.memSize="L"
			else if (env.Cloud_MEM_SIZE_XL!=null && env.Cloud_MEM_SIZE_XL.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.memSize="XL"
			else if (env.Cloud_MEM_SIZE_XXL!=null && env.Cloud_MEM_SIZE_XXL.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.memSize="XXL"
			else if (env.Cloud_MEM_SIZE_XXXL!=null && env.Cloud_MEM_SIZE_XXXL.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.memSize="XXXL"
			
			if (env.Cloud_CPU_SIZE_S!=null && env.Cloud_CPU_SIZE_S.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.cpuSize="S"
			else if (env.Cloud_CPU_SIZE_L!=null && env.Cloud_CPU_SIZE_L.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.cpuSize="L"
			else if (env.Cloud_CPU_SIZE_XL!=null && env.Cloud_CPU_SIZE_XL.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.cpuSize="XL"
			else if (env.Cloud_CPU_SIZE_XXL!=null && env.Cloud_CPU_SIZE_XXL.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.cpuSize="XXL"
			else if (env.Cloud_CPU_SIZE_XXXL!=null && env.Cloud_CPU_SIZE_XXXL.contains(applicationNameWithoutVersion.toLowerCase())) cloudResources.cpuSize="XXXL"
		}
		
	    /*Esto es obsoleto... esto viene imagino de cuando nos parametrizaban los tamaños en los ficheros
		if (memory==null || memory=="") memory="768"		
		cloudResources.limitsMemory=memory+"Mi"*/

        printOpen("Resources para artifact ${applicationNameWithoutVersion.toLowerCase()} replicasSize: ${cloudResources.replicasSize} memSize: ${cloudResources.memSize} cpuSize: ${cloudResources.cpuSize}", EchoLevel.INFO)
        printOpen("${cloudResources.toString()}", EchoLevel.INFO)
		return cloudResources
				
	}else {
		cloudResources.environment=environment		
		cloudResources.isArchProject=isArchProjectOrSkipMinimum
		/*
		if (memory==null || memory=="") memory="768"
		cloudResources.limitsMemory=memory+"Mi"*/
		return cloudResources
	}	

}