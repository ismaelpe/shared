import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.ICPAppResources
import com.caixabank.absis3.ICPAppResourcesCatMsv
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.KpiAlmEvent
import com.caixabank.absis3.KpiAlmEventOperation
import com.caixabank.absis3.KpiAlmEventStage


def setICPResources( def type, def app, def major, def environment, def namespace){
	//ICPAppResources icpResources=new ICPAppResources()
	ICPAppResourcesCatMsv icpResources=new ICPAppResourcesCatMsv()
 
	printOpen("Requesting application's sizes to Open's catalogue...", EchoLevel.INFO)

    def response = sendRequestToAbsis3MS(
        'GET',
        "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${type}/${app}/${major}/config?env=${environment}",
        null,
        "${GlobalVars.CATALOGO_ABSIS3_ENV}",
        [
            kpiAlmEvent: new KpiAlmEvent(
                null, null,
                KpiAlmEventStage.UNDEFINED,
                KpiAlmEventOperation.CATMSV_HTTP_CALL)
        ])
	
	if (response.status == 200) {
        printOpen("Get : ${response.content}", EchoLevel.DEBUG)
	
		//def json = new groovy.json.JsonSlurper().parseText(response.content)
		def json = response.content
		printOpen("Parametros de arranque son de ${json.jvmArgs} ", EchoLevel.DEBUG)
		icpResources.requestsMemory = "${json.memRequests}"
		icpResources.requestsCpu="${json.cpuRequests}"
		icpResources.limitsMemory="${json.memLimits}"
		icpResources.limitsCpu="${json.cpuLimits}"
		icpResources.numInstances="${json.replicas}"
		icpResources.memSize="${json.memSize}".trim()
		icpResources.cpuSize="${json.cpuSize}".trim()
        if (json.jvmArgs!=null){
            icpResources.jvmArgs=json.jvmArgs
        }else{
            icpResources.jvmArgs=null
        }
		icpResources.replicasSize="${json.replicaSize}".trim()

        printOpen("El tamaño recuperado del dimensionamiento del micro es de ${icpResources.toString()} este esta en el catalogo", EchoLevel.DEBUG)
		
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
		icpResources.memSize='M'
		icpResources.cpuSize='M'
		icpResources.replicasSize='M'
  
		printOpen("Requesting default sizes to Open's catalogue for namespace:${namespace} type:${type} app:${app}", EchoLevel.INFO)

		response = sendRequestToAbsis3MS(
            'GET',
            "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/config/micro-size/${namespace}/${type}/${environment}/M/M/M",
            null,
            "${GlobalVars.CATALOGO_ABSIS3_ENV}",
            [
                kpiAlmEvent: new KpiAlmEvent(
                    null, null,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.CATMSV_HTTP_CALL)
            ])

        if (response.status == 200) {
            printOpen("Get : ${response.content}", EchoLevel.DEBUG)
			//def json = new groovy.json.JsonSlurper().parseText(response.content)
			def json = response.content
			icpResources.requestsMemory = "${json.memRequests}"
			icpResources.requestsCpu="${json.cpuRequests}"
			icpResources.limitsMemory="${json.memLimits}"
			icpResources.limitsCpu="${json.cpuLimits}"
			icpResources.jvmArgs=json.jvmArgs
			icpResources.numInstances="${json.replicas}"
   
		}else{
            printOpen("There are no default sizes for namespace:${namespace} type:${type} app:${app}", EchoLevel.INFO)
			return null
		}

        printOpen("The application sizes are ${icpResources.getValue()}", EchoLevel.INFO)
		
	}
	return icpResources

}

def call(String memory, String environment, boolean isArchProjectOrSkipMinimum, String applicationNameWithoutVersion, String domain) {
	generateICPResources(memory,environment,isArchProjectOrSkipMinimum,applicationNameWithoutVersion,domain,null,null,null)
}

def call(String memory, String environment, boolean isArchProjectOrSkipMinimum, String applicationNameWithoutVersion, String domain,String type, String major, String namespace) {
	ICPAppResources icpResources=null
	if (environment=='EDEN') environment='DEV'
	
	if ( type!=null && env.CATMSV_SIZE!=null && "true".equals(env.CATMSV_SIZE) && major!=null && applicationNameWithoutVersion!=null) {
		//ICPAppResourcesCatMsv icpResources=new ICPAppResourcesCatMsv()
		icpResources=setICPResources(type,applicationNameWithoutVersion,major,environment,namespace)
		//return icpResources		
	}
	

	if (icpResources==null) {

        printOpen("En el catalogo no tenemos la info... tenemos que recurrir al sistema antiguo mediante variables jenkins", EchoLevel.INFO)
		icpResources=new ICPAppResources()
		icpResources.environment=environment
		icpResources.isArchProject=isArchProjectOrSkipMinimum

        printOpen("Vamos a comprobar el size del artifact ${applicationNameWithoutVersion.toLowerCase()} Entorno: ${environment} Memory: ${memory} ArchProject: ${isArchProjectOrSkipMinimum} domain: ${domain} los dominios de ARQ son: ${env.DEMO_DOMAIN_ARQ}", EchoLevel.DEBUG)
		if(domain == null) {
			domain = "NO_DOMAIN"
		}
		
		if ("NO_DOMAIN" != domain && env.DEMO_DOMAIN_ARQ!=null && env.DEMO_DOMAIN_ARQ.indexOf(domain) !=-1) {
			
			icpResources.replicasSize="S"
			icpResources.memSize="S"
			icpResources.cpuSize="S"
			
		} else {
			if (isArchProjectOrSkipMinimum==false && env.ICP_ALL_APPS_MINIMUM!=null && env.ICP_ALL_APPS_MINIMUM.contains("true")) icpResources.allAppsMinimum=true
			else if (env.ICP_REPLICA_SIZE_S!=null && env.ICP_REPLICA_SIZE_S.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.replicasSize="S"
			else if (env.ICP_REPLICA_SIZE_L!=null && env.ICP_REPLICA_SIZE_L.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.replicasSize="L"
			else if (env.ICP_REPLICA_SIZE_XL!=null && env.ICP_REPLICA_SIZE_XL.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.replicasSize="XL"
			else if (env.ICP_REPLICA_SIZE_XXL!=null && env.ICP_REPLICA_SIZE_XXL.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.replicasSize="XXL"
			else if (env.ICP_REPLICA_SIZE_XXXL!=null && env.ICP_REPLICA_SIZE_XXXL.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.replicasSize="XXXL"
		
			if (env.ICP_MEM_SIZE_S!=null && env.ICP_MEM_SIZE_S.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.memSize="S"
			else if (env.ICP_MEM_SIZE_L!=null && env.ICP_MEM_SIZE_L.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.memSize="L"
			else if (env.ICP_MEM_SIZE_XL!=null && env.ICP_MEM_SIZE_XL.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.memSize="XL"
			else if (env.ICP_MEM_SIZE_XXL!=null && env.ICP_MEM_SIZE_XXL.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.memSize="XXL"
			else if (env.ICP_MEM_SIZE_XXXL!=null && env.ICP_MEM_SIZE_XXXL.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.memSize="XXXL"
			
			if (env.ICP_CPU_SIZE_S!=null && env.ICP_CPU_SIZE_S.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.cpuSize="S"
			else if (env.ICP_CPU_SIZE_L!=null && env.ICP_CPU_SIZE_L.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.cpuSize="L"
			else if (env.ICP_CPU_SIZE_XL!=null && env.ICP_CPU_SIZE_XL.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.cpuSize="XL"
			else if (env.ICP_CPU_SIZE_XXL!=null && env.ICP_CPU_SIZE_XXL.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.cpuSize="XXL"
			else if (env.ICP_CPU_SIZE_XXXL!=null && env.ICP_CPU_SIZE_XXXL.contains(applicationNameWithoutVersion.toLowerCase())) icpResources.cpuSize="XXXL"
		}
		
	    /*Esto es obsoleto... esto viene imagino de cuando nos parametrizaban los tamaños en los ficheros
		if (memory==null || memory=="") memory="768"		
		icpResources.limitsMemory=memory+"Mi"*/

        printOpen("Resources para artifact ${applicationNameWithoutVersion.toLowerCase()} replicasSize: ${icpResources.replicasSize} memSize: ${icpResources.memSize} cpuSize: ${icpResources.cpuSize}", EchoLevel.INFO)
        printOpen("${icpResources.toString()}", EchoLevel.INFO)
		return icpResources
				
	}else {
		icpResources.environment=environment		
		icpResources.isArchProject=isArchProjectOrSkipMinimum
		/*
		if (memory==null || memory=="") memory="768"
		icpResources.limitsMemory=memory+"Mi"*/
		return icpResources
	}	

}