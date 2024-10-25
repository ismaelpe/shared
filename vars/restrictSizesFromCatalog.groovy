import com.caixabank.absis3.ICPAppResources
import com.caixabank.absis3.ICPAppResourcesCatMsv
import com.caixabank.absis3.GlobalVars

///config/micro-size/{namespace}/{type}/{env}/{replicas}/{cpu}/{memory}

def call(ICPAppResources icpResources, String environment) {
	if (!environment.equals(GlobalVars.PRO_ENVIRONMENT.toUpperCase())) {		
		icpResources.requestsCpu="25"
		icpResources.numInstances="1"
		icpResources.replicasSize="S"
	}
	return icpResources
}