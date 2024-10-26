import com.project.alm.CloudAppResources
import com.project.alm.CloudAppResourcesCatMsv
import com.project.alm.GlobalVars

///config/micro-size/{namespace}/{type}/{env}/{replicas}/{cpu}/{memory}

def call(CloudAppResources cloudResources, String environment) {
	if (!environment.equals(GlobalVars.PRO_ENVIRONMENT.toUpperCase())) {		
		cloudResources.requestsCpu="25"
		cloudResources.numInstances="1"
		cloudResources.replicasSize="S"
	}
	return cloudResources
}