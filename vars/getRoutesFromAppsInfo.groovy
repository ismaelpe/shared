import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.DeployStructure
import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.ICPDeployStructure

import com.caixabank.absis3.ICPApiResponse

import java.util.Map

def call(Map infoApps) {
	Map appsRoutes=new HashMap<String, Map>()
	infoApps.each { 
		
		if (it.value!=null) {
			Map infoApp = it.value
			if (infoApp != null && infoApp.absis != null && infoApp.absis.services != null && infoApp.absis.services.envQualifier != null) {
				printOpen("The info from ${it.key} is:", EchoLevel.ALL)
				Map absisServicesEnvQualifier=infoApp.absis.services.envQualifier
				Map newService=absisServicesEnvQualifier["new"]
				Map stableService=absisServicesEnvQualifier["stable"]
				Map newNonDevService=absisServicesEnvQualifier["newNonDev"]
				
				Map routes = new HashMap<String, Map>()
				
				if (stableService!=null) {
					printOpen("stable route: ${stableService.id}", EchoLevel.ALL)
					routes.put("stable", stableService.id)
				}
				if (newService!=null) {
					printOpen("new route: ${newService.id}", EchoLevel.ALL)
					routes.put("new", newService.id)
				}
				if (newNonDevService!=null) {
					printOpen("other route: ${newNonDevService.id}", EchoLevel.ALL)
					routes.put("other", newNonDevService.id)
				}
				appsRoutes.put(it.key, routes)
				printOpen("==========================================================================================================", EchoLevel.ALL)
			} else {
				printOpen("There are no services from ${it.key}.", EchoLevel.ALL)
				printOpen("==========================================================================================================", EchoLevel.ALL)
			}
		} else {
			printOpen("There is no info from ${it.key}.", EchoLevel.ALL)
			printOpen("==========================================================================================================", EchoLevel.ALL)
		}
	}
	return appsRoutes
}