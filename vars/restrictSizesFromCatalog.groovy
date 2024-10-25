import com.project.alm.ICPAppResources
import com.project.alm.ICPAppResourcesCatMsv
import com.project.alm.GlobalVars

///config/micro-size/{namespace}/{type}/{env}/{replicas}/{cpu}/{memory}

def call(ICPAppResources icpResources, String environment) {
	if (!environment.equals(GlobalVars.PRO_ENVIRONMENT.toUpperCase())) {		
		icpResources.requestsCpu="25"
		icpResources.numInstances="1"
		icpResources.replicasSize="S"
	}
	return icpResources
}