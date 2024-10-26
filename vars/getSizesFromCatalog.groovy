import com.project.alm.CloudAppResourcesCatMsv
import com.project.alm.GlobalVars
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage

///config/micro-size/{namespace}/{type}/{env}/{replicas}/{cpu}/{memory}

def call(String namespace, String type, String environment, boolean isArchProject, String memory, String cpu, String replicas) {
	CloudAppResourcesCatMsv cloudResources = new CloudAppResourcesCatMsv()
	environment=environment.toUpperCase()
	
	if (environment=='EDEN') environment='DEV'
	if ( type!=null && env.CATMSV_SIZE!=null && "true".equals(env.CATMSV_SIZE)) {
		def response = sendRequestToAlm3MS(
            'GET',
            "${GlobalVars.URL_CATALOGO_ALM_PRO}/config/micro-size/${namespace}/${type}/${environment}/${replicas}/${cpu}/${memory}",
            null,
            "${GlobalVars.CATALOGO_ALM_ENV}",
            [
                kpiAlmEvent: new KpiAlmEvent(
                    null, null,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.CATMSV_HTTP_CALL)
            ])
		
		if (response.status == 200) {
			//def json = new groovy.json.JsonSlurper().parseText(response.content)
			def json = response.content
			//{"cpuSize":"L     ","memSize":"L     ","replicaSize":"L     ","replicas":3,"memRequests":700,"memLimits":700,"cpuRequests":300,"cpuLimits":700}
			cloudResources.requestsMemory=json.memRequests
			cloudResources.requestsCpu=json.cpuRequests
			cloudResources.limitsMemory=json.memLimits
			cloudResources.limitsCpu=json.cpuLimits
			cloudResources.numInstances=json.replicas
			cloudResources.jvmArgs=json.jvmArgs
		}
	}

	cloudResources.environment = environment
	cloudResources.isArchProject = true
	cloudResources.replicasSize=replicas
	cloudResources.memSize=memory
	cloudResources.cpuSize=cpu

	return cloudResources
}



