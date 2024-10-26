import com.project.alm.*
import com.project.alm.Cloudk8sComponentDeploymentInfo
import com.project.alm.BmxUtilities
import com.project.alm.Cloudk8sComponentInfo
import com.project.alm.Cloudk8sComponentServiceInfo

/**
[{"metadata":{"cluster":"cloud01-pre","company":"cxb","appName":"pasdev","componentName":"democonnecta22","platform":"cloud"},
"items":{
"deployments":[{"name":"democonnecta22-b","replicas":{"desired":1,"updated":1,"total":1,"available":1,"unavailable":null},"ready":true,"creationTimestamp":"2020-03-10T17:01:50Z"}],
"ingresses":[{"name":"democonnecta2-micro-2-beta-system","ready":true}],
"services":[{"name":"democonnecta2-micro-2-beta"}]}},
{"metadata":{"cluster":"cloud02-pre","company":"cxb","appName":"pasdev","componentName":"democonnecta22","platform":"cloud"},
"items":{
"deployments":[{"name":"democonnecta22-b","replicas":{"desired":1,"updated":1,"total":1,"available":1,"unavailable":null},"ready":true,"creationTimestamp":"2020-03-10T17:02:13Z"}],
"ingresses":[{"name":"democonnecta2-micro-2-beta-system","ready":true}],
"services":[{"name":"democonnecta2-micro-2-beta"}]}}]
 */


/**
 *
 * @param body
 * @return
 */
def call(def body) {
	
	//Tenemos un array de objetos donde tenemos que por cada elemento
	//Tenemos el atributo
	//      metadata
	//		items
	//	Por cada item tenemos arrays de deployments, ingresses, services

	def cloudZoneLength=body.size()
	Cloudk8sComponentInfoMult resultado = new Cloudk8sComponentInfoMult()
	
	boolean componentIsNull=true
	
	if (cloudZoneLength>=1) {
		def i = 0
		while(i < cloudZoneLength) {
			componentIsNull=true
			
			Cloudk8sComponentInfo k8sComponentInfo = new Cloudk8sComponentInfo()
			
			def itemsk8s=body[i].items
			
			if (itemsk8s.deployments!=null) {
				componentIsNull=false
				itemsk8s.deployments.each{
					Cloudk8sComponentDeploymentInfo deployment=new Cloudk8sComponentDeploymentInfo()
					
					deployment.name=it.name
					deployment.replicasAvailable = it.replicas.available
					deployment.replicasUnavailable = it.replicas.unavailable
					deployment.replicasDesired = it.replicas.desired
					deployment.ready = it.ready
					deployment.creationTimestamp = it.creationTimestamp				
					deployment.errors = it.errors
					
					k8sComponentInfo.deployments.add(deployment)				
					
				}
			}
			
			
			//Servicios
			if (itemsk8s.services!=null) {
				componentIsNull=false
				itemsk8s.services.each{
					Cloudk8sComponentServiceInfo service=new Cloudk8sComponentServiceInfo()
					service.name=it.name					
					k8sComponentInfo.services.add(service)
				}
			}

			String valorOutput=k8sComponentInfo.toString()			
			if (!componentIsNull) resultado.clusters.add(k8sComponentInfo)			
			i = i + 1
		}
	}
	
	return resultado
}

