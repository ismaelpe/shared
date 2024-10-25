package com.caixabank.absis3

import org.junit.Test
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK
import com.caixabank.absis3.AppDeploymentStateICP
import com.caixabank.absis3.AppDeploymentState
import java.util.Map

class AppDeploymentStateICPTest extends GroovyTestCase {

	
	Map parseFromYamlToMap(String valuesYaml) {
		Map valuesMap=null
		def opts = new DumperOptions()
		opts.setDefaultFlowStyle(BLOCK)
		Yaml yaml= new Yaml(opts)
		
		valuesMap=(Map)yaml.load( valuesYaml)
		println "The valuesMap is "+valuesMap
		return valuesMap
	}
	
    String consolidatedLogDumpWithOld =
            "absis:\n  app:\n    loggingElkStack: absis30\n    replicas: 1\n    instance: operatormanager1\n    name: operatormanager\n  resources:\n    requests:\n      memory: 128Mi\n      cpu: 25m\n    limits:\n       memory: 768Mi\n       cpu: 600m\n  apps:\n    envQualifier:\n      stable:\n        id: operatormanager1-g\n        colour: G\n        image: pro-registry.pro.caas.caixabank.com/containers/ab3cor/operatormanager1:1.5.0-SNAPSHOT-A\n        version: 1.5.0-SNAPSHOT\n        stable: false\n        new: false\n        replicas: 1\n        requests_memory: 128Mi\n        requests_cpu: 25m\n        limits_memory: 768Mi\n        limits_cpu: 600m\n      old:\n        id: operatormanager1-b\n        colour: B\n        image: pro-registry.pro.caas.caixabank.com/containers/ab3cor/operatormanager1:1.5.0-SNAPSHOT-B\n        version: 1.5.0-SNAPSHOT\n        stable: false\n        new: false\n        replicas: 1\n        requests_memory: 128Mi\n        requests_cpu: 25m\n        limits_memory: 768Mi\n        limits_cpu: 600m\n  services:\n    envQualifier:\n      stable:\n        id: operator-manager-micro-server-1-dev\n        targetColour: G\n      new:\n        id: new-operator-manager-micro-server-1-dev\n        targetColour: G\n"

	String consolidatedLog =
			"absis:\n  app:\n    loggingElkStack: absis30\n    replicas: 1\n    instance: operatormanager1\n    name: operatormanager\n  resources:\n    requests:\n      memory: 128Mi\n      cpu: 25m\n    limits:\n       memory: 768Mi\n       cpu: 600m\n  apps:\n    envQualifier:\n      stable:\n        id: operatormanager1-g\n        colour: G\n        image: pro-registry.pro.caas.caixabank.com/containers/ab3cor/operatormanager1:1.5.0-SNAPSHOT-A\n        version: 1.5.0-SNAPSHOT\n        stable: false\n        new: false\n        replicas: 1\n        requests_memory: 128Mi\n        requests_cpu: 25m\n        limits_memory: 768Mi\n        limits_cpu: 600m\n  services:\n    envQualifier:\n      stable:\n        id: operator-manager-micro-server-1-dev\n        targetColour: G\n      new:\n        id: new-operator-manager-micro-server-1-dev\n        targetColour: G\n"

			
    String canaryLogDump =
            "absis:\n  app:\n    loggingElkStack: absis30\n    replicas: 1\n    instance: operatormanager1\n    name: operatormanager\n  resources:\n    requests:\n      memory: 128Mi\n      cpu: 25m\n    limits:\n       memory: 768Mi\n       cpu: 600m\n  apps:\n    envQualifier:\n      stable:\n        id: operatormanager1-g\n        colour: G\n        image: pro-registry.pro.caas.caixabank.com/containers/ab3cor/operatormanager1:1.5.0-SNAPSHOT-A\n        version: 1.5.0-SNAPSHOT\n        stable: false\n        new: false\n        replicas: 1\n        requests_memory: 128Mi\n        requests_cpu: 25m\n        limits_memory: 768Mi\n        limits_cpu: 600m\n      new:\n        id: operatormanager1-b\n        colour: B\n        image: pro-registry.pro.caas.caixabank.com/containers/ab3cor/operatormanager1:1.5.0-SNAPSHOT-B\n        version: 1.5.0-SNAPSHOT\n        stable: false\n        new: false\n        replicas: 1\n        requests_memory: 128Mi\n        requests_cpu: 25m\n        limits_memory: 768Mi\n        limits_cpu: 600m\n  services:\n    envQualifier:\n      stable:\n        id: operator-manager-micro-server-1-dev\n        targetColour: G\n      new:\n        id: beta-operator-manager-micro-server-1-dev\n        targetColour: B\n"

    String thereIsNothingLogDump = ""

	
	String incidentServiceWithOutPod =
			"absis:\n  app:\n    loggingElkStack: absis30\n    replicas: 1\n    instance: operatormanager1\n    name: operatormanager\n  resources:\n    requests:\n      memory: 128Mi\n      cpu: 25m\n    limits:\n       memory: 768Mi\n       cpu: 600m\n  apps:\n    envQualifier:\n      stable:\n        id: operatormanager1-g\n        colour: K\n        image: pro-registry.pro.caas.caixabank.com/containers/ab3cor/operatormanager1:1.5.0-SNAPSHOT-A\n        version: 1.5.0-SNAPSHOT\n        stable: false\n        new: false\n        replicas: 1\n        requests_memory: 128Mi\n        requests_cpu: 25m\n        limits_memory: 768Mi\n        limits_cpu: 600m\n      new:\n        id: operatormanager1-b\n        colour: B\n        image: pro-registry.pro.caas.caixabank.com/containers/ab3cor/operatormanager1:1.5.0-SNAPSHOT-B\n        version: 1.5.0-SNAPSHOT\n        stable: false\n        new: false\n        replicas: 1\n        requests_memory: 128Mi\n        requests_cpu: 25m\n        limits_memory: 768Mi\n        limits_cpu: 600m\n  services:\n    envQualifier:\n      stable:\n        id: operator-manager-micro-server-1-dev\n        targetColour: G\n      new:\n        id: beta-operator-manager-micro-server-1-dev\n        targetColour: B\n"
		
				

					
    @Test
    void testConsolidatedWithOld() {
		
		
        AppDeploymentStateICP appState = new AppDeploymentStateICP(parseFromYamlToMap(consolidatedLogDumpWithOld),"app-micro-1")
		
        assertEquals(AppDeploymentState.Current.CONSOLIDATED, appState.current)
    }
	
	
	@Test
	void testConsolidated() {		
		
		AppDeploymentStateICP appState = new AppDeploymentStateICP(parseFromYamlToMap(consolidatedLog),"app-micro-1")
		
		assertEquals(AppDeploymentState.Current.CONSOLIDATED, appState.current)
	}
	
	
	@Test
	void testCanary() {
		AppDeploymentStateICP appState = new AppDeploymentStateICP(parseFromYamlToMap(canaryLogDump),"app-micro-1")
		assertEquals(AppDeploymentState.Current.CANARY, appState.current)
	}
	
	@Test
	void testThereIsNothing() {
        AppDeploymentStateICP appState = new AppDeploymentStateICP(parseFromYamlToMap(thereIsNothingLogDump),"app-micro-1")
		assertEquals(AppDeploymentState.Current.NOT_DEPLOYED, appState.current)
	}
	
	@Test
	void testThereIsIncident() {
		AppDeploymentStateICP appState = new AppDeploymentStateICP(parseFromYamlToMap(incidentServiceWithOutPod),"app-micro-1")
		assertEquals(AppDeploymentState.Current.INCIDENT_LIKELY, appState.current)
	}

}
