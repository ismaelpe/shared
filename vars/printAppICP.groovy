import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.DeployStructure
import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.ICPDeployStructure

import com.caixabank.absis3.ICPApiResponse

import java.util.Map


def printApp(String colour, Map app) {
	if (app!=null) {
		if (colour!=null && ((colour=="B" || colour=="G")) && colour==app["colour"]) {
			printOpen("       APP ID: ${app['id']}", EchoLevel.ALL)
			printOpen("       IMAGE: ${app['image']}", EchoLevel.ALL)
			printOpen("       VERSION: ${app['version']}", EchoLevel.ALL)
			printOpen("       INSTANCIAS: ${app['replicas']}", EchoLevel.ALL)
			printOpen("       MEMORY: ${app['requests_memory']}_${app['limits_memory']}", EchoLevel.ALL)
			printOpen("       CPU: ${app['requests_cpu']}_${app['limits_cpu']}", EchoLevel.ALL)
		}
	}	
}

def call(Map infoApp) {
	printOpen("The info is ${infoApp}", EchoLevel.ALL)
	if (infoApp!=null) {
		
		Map absis=infoApp["absis"]
		printOpen("The apps ${absis} ", EchoLevel.ALL)
		Map absisApp=absis["apps"]
		Map absisAppEnvQualifier=absisApp["envQualifier"]
		//Este map es el que contiene la info del micro desplegado
		Map stable=null
		Map old=null
		Map newApp=null
		
		boolean isReady=false
		def body=null
		def values=null
		def response=null
		
		stable=absisAppEnvQualifier["stable"]
		newApp=absisAppEnvQualifier["new"]
		old=absisAppEnvQualifier["old"]
		
		absisApp=absis["services"]
		absisAppEnvQualifier=absisApp["envQualifier"]
		
		Map newService=absisAppEnvQualifier["new"]
		Map stableService=absisAppEnvQualifier["stable"]
		Map newNonDev=absisAppEnvQualifier["newNonDev"]
		
		printOpen("==========================================================================================================", EchoLevel.ALL)
		
		
		
		
		
		if (stableService!=null) {
			printOpen("APP ESTABLE ", EchoLevel.ALL)
			printOpen("   route: ${stableService['id']}", EchoLevel.ALL)
			if (newService!=null && newService['targetColour']==stableService['targetColour']) {
				printOpen("   route: ${newService['id']}", EchoLevel.ALL)
				newService=null
			}
			if (newNonDev!=null && newNonDev['targetColour']==stableService['targetColour']) {
				printOpen("   route: ${newNonDev['id']}", EchoLevel.ALL)
				newNonDev=null
			}
			printOpen("   colour: ${stableService['targetColour']}", EchoLevel.ALL)
			printApp("${stableService['targetColour']}",stable)
			printApp("${stableService['targetColour']}",newApp)
			printApp("${stableService['targetColour']}",old)
		}
		if (newService!=null) {
			printOpen("APP NUEVA ", EchoLevel.ALL)
			printOpen("   route: ${newService['id']}", EchoLevel.ALL)
			if (newNonDev!=null && newNonDev['targetColour']==newService['targetColour']) {
				printOpen("   route: ${newNonDev['id']}", EchoLevel.ALL)
				newNonDev=null
			}
			printOpen("   colour: ${newService['targetColour']}", EchoLevel.ALL)
			printApp("${newService['targetColour']}",stable)
			printApp("${newService['targetColour']}",newApp)
			printApp("${newService['targetColour']}",old)
		}
		if (newNonDev!=null) {
			printOpen("APP OTRAS ", EchoLevel.ALL)
			printOpen("   route: ${newNonDev['id']}", EchoLevel.ALL)
			printOpen("   colour: ${newNonDev['targetColour']}", EchoLevel.ALL)
			printApp("${newNonDev['targetColour']}",stable)
			printApp("${newNonDev['targetColour']}",newApp)
			printApp("${newNonDev['targetColour']}",old)
		}
		

	}	
}
