import com.project.alm.ICPAppResourcesCatMsv
import com.project.alm.GlobalVars
import com.project.alm.KpiAlmEvent
import com.project.alm.KpiAlmEventOperation
import com.project.alm.KpiAlmEventStage

///config/micro-size/{namespace}/{type}/{env}/{replicas}/{cpu}/{memory}

def call(String namespace, String type, String environment, boolean isArchProject, String memory, String cpu, String replicas) {
	ICPAppResourcesCatMsv icpResources = new ICPAppResourcesCatMsv()
	environment=environment.toUpperCase()
	
	if (environment=='EDEN') environment='DEV'
	if ( type!=null && env.CATMSV_SIZE!=null && "true".equals(env.CATMSV_SIZE)) {
		def response = sendRequestToAbsis3MS(
            'GET',
            "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/config/micro-size/${namespace}/${type}/${environment}/${replicas}/${cpu}/${memory}",
            null,
            "${GlobalVars.CATALOGO_ABSIS3_ENV}",
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
			icpResources.requestsMemory=json.memRequests
			icpResources.requestsCpu=json.cpuRequests
			icpResources.limitsMemory=json.memLimits
			icpResources.limitsCpu=json.cpuLimits
			icpResources.numInstances=json.replicas
			icpResources.jvmArgs=json.jvmArgs
		}
	}

	icpResources.environment = environment
	icpResources.isArchProject = true
	icpResources.replicasSize=replicas
	icpResources.memSize=memory
	icpResources.cpuSize=cpu

	return icpResources
}



