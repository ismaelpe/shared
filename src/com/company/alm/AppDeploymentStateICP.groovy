package com.project.alm
import java.util.Map
import com.project.alm.AppDeploymentState

class AppDeploymentStateCloud extends AppDeploymentState{

	
	
	Map deploymentValues
	String appName
	Map services
	Map apps
	boolean isDeployed

    AppDeploymentStateCloud(Map deploymentValues, String appName) {
        this.deploymentValues=deploymentValues
		this.appName=appName
		
		isDeployed=true

		if (deploymentValues==null || deploymentValues["alm"]==null) {
			isDeployed=false
		}else {
			Map deployment=deploymentValues["alm"]

			Map almApp=deployment["apps"]
			if (almApp==null || (almApp!=null && almApp["envQualifier"]==null)) {
				isDeployed=false
			}else {
				apps=almApp["envQualifier"]

				Map preServices=deployment['services']
				if (preServices==null || (preServices!=null && preServices['envQualifier']==null)) {
					isDeployed=false
				}else {
				
					services=preServices['envQualifier']
				}
			}

		}
    }
	
	def getApp(String tag) {
		return apps[tag]
	}
	
	
	def getService(String tag) {
		return services[tag]
	}
	
	def getColourFromApp(String tag) {
		def app=getApp(tag)
		if (app!=null) return app['colour']
		else return 'KK'
	}
	
	
	def getColourFromService(String tag) {
		def service=getService(tag)
		if (service!=null) return service['targetColour']
		else return 'OO'
	}

	boolean getAncientApp() {
		if (apps!=null && apps["stable"]!=null && apps["new"]!=null) return true
		else return false
	}
	
    AppDeploymentState.Current getCurrent() {
		
		
		//Cannary es que la new tenga la ruta new y no la stable
		//La stable la tiene la app stable
		if (!isDeployed) {
			 return AppDeploymentState.Current.NOT_DEPLOYED
		}else if ((getApp('new')!=null && getApp('stable')!=null && getColourFromApp('new')!=getColourFromService('stable') && 
			 getColourFromApp('new')==getColourFromService('new') &&
			 getColourFromApp('stable')==getColourFromService('stable')) || //Esto es un upgrade de una major
			(getApp('new')==null && getApp('stable')!=null && getColourFromApp('stable')!=getColourFromService('stable') && getColourFromApp('stable')==getColourFromService('new'))) { //Este es primera major
				return AppDeploymentState.Current.CANARY		
		}else if (getApp('stable')!=null && getColourFromApp('stable')==getColourFromService('stable') && getApp('new')==null) { //Este es primera major
				return AppDeploymentState.Current.CONSOLIDATED
		//Deteccion de incidente		
		}else if (getColourFromService('stable')!=getColourFromApp('stable') && getColourFromService('stable')!=getColourFromApp('new')) {
				return AppDeploymentState.Current.INCIDENT_LIKELY
	    } else {
		  return AppDeploymentState.Current.UNKNOWN	     
		}
    }

    String toString() {
        return "AppDeploymentState:\n" +
                "\tisDeployed: $isDeployed\n" +
                "\tapps: $apps\n" +
                "\tservices: $services\n" 
    }


}
	
	
