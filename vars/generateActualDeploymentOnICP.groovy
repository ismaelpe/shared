import com.caixabank.absis3.*
import com.caixabank.absis3.ICPk8sComponentDeploymentInfo
import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.ICPk8sComponentInfo
import com.caixabank.absis3.ICPk8sComponentServiceInfo

/**
[{"metadata":{"cluster":"icp01-pre","company":"cxb","appName":"pasdev","componentName":"democonnecta22","platform":"icp"},
"items":{
"deployments":[{"name":"democonnecta22-b","replicas":{"desired":1,"updated":1,"total":1,"available":1,"unavailable":null},"ready":true,"creationTimestamp":"2020-03-10T17:01:50Z"}],
"ingresses":[{"name":"democonnecta2-micro-2-beta-system","ready":true}],
"services":[{"name":"democonnecta2-micro-2-beta"}]}},
{"metadata":{"cluster":"icp02-pre","company":"cxb","appName":"pasdev","componentName":"democonnecta22","platform":"icp"},
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

	def icpZoneLength=body.size()
	ICPk8sComponentInfoMult resultado = new ICPk8sComponentInfoMult()
	
	boolean componentIsNull=true
	
	if (icpZoneLength>=1) {
		def i = 0
		while(i < icpZoneLength) {
			componentIsNull=true
			
			ICPk8sComponentInfo k8sComponentInfo = new ICPk8sComponentInfo()
			
			def itemsk8s=body[i].items
			
			if (itemsk8s.deployments!=null) {
				componentIsNull=false
				itemsk8s.deployments.each{
					ICPk8sComponentDeploymentInfo deployment=new ICPk8sComponentDeploymentInfo()
					
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
					ICPk8sComponentServiceInfo service=new ICPk8sComponentServiceInfo()
					service.name=it.name					
					k8sComponentInfo.services.add(service)
				}
			}
			
			//Pods
			if (itemsk8s.pods!=null) {
				itemsk8s.pods.each{
					ICPk8sComponentPodInfo pod=new ICPk8sComponentPodInfo()
					pod.name = it.name
					pod.controlledByName = it.controlledByName
					pod.restarts = it.restarts
					pod.creationTimestamp = it.creationTimestamp
					
					k8sComponentInfo.pods.add(pod)
				}
			}

			String valorOutput=k8sComponentInfo.toString()			
			if (!componentIsNull) resultado.clusters.add(k8sComponentInfo)			
			i = i + 1
		}
	}
	
	return resultado
}

