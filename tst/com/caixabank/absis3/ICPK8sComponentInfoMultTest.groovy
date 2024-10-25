package com.caixabank.absis3

import org.junit.Test
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK
import com.caixabank.absis3.AppDeploymentStateICP
import com.caixabank.absis3.AppDeploymentState
import com.caixabank.absis3.ICPk8sComponentDeploymentInfo
import com.caixabank.absis3.ICPk8sComponentInfo
import com.caixabank.absis3.ICPk8sComponentInfoMult
import com.caixabank.absis3.ICPk8sInstancesApp


import java.util.Map

class ICPK8sComponentInfoMultTest extends GroovyTestCase {

	
	Map parseFromYamlToMap(String valuesYaml) {
		Map valuesMap=null
		def opts = new DumperOptions()
		opts.setDefaultFlowStyle(BLOCK)
		Yaml yaml= new Yaml(opts)
		
		valuesMap=(Map)yaml.load( valuesYaml)
		println "The valuesMap is "+valuesMap
		return valuesMap
	}
	
	def getDeploymentInfoInCompleted(def colour = 'g') {
		def infoData = new ICPk8sComponentDeploymentInfo()
		infoData.name='micro-testing-'+colour
		infoData.replicasDesired=2
		infoData.replicasUnavailable=1
		infoData.replicasAvailable=1
		infoData.ready="true"
		return infoData
	}
	
	def getDeploymentInfoCompleted(def colour = 'g') {
		def infoData = new ICPk8sComponentDeploymentInfo()
		infoData.name='micro-testing-'+colour
		infoData.replicasDesired=2
		infoData.replicasUnavailable=0
		infoData.replicasAvailable=2
		infoData.ready="true"
		return infoData 
	}
	
	def getDeploymentClusterIncompleted(){
		ICPk8sComponentInfo infoData = new ICPk8sComponentInfo()
		
		infoData.deployments.add(getDeploymentInfoCompleted('g'))		
		infoData.deployments.add(getDeploymentInfoInCompleted('b'))
		
		return infoData
	}
	
	def getDeploymentClusterCompleted(){
		ICPk8sComponentInfo infoData = new ICPk8sComponentInfo()
		
		infoData.deployments.add(getDeploymentInfoCompleted('g'))		
		infoData.deployments.add(getDeploymentInfoCompleted('b'))
		
		return infoData
    }
	
	ICPk8sComponentInfoMult getObjectIncompleted() {
		def infoData = new ICPk8sComponentInfoMult()
		

		infoData.clusters.add(getDeploymentClusterIncompleted())
		infoData.clusters.add(getDeploymentClusterCompleted())

		
		
		return infoData
	} 
	
	
	ICPk8sComponentInfoMult getObjectIncompleted1() {
		def infoData = new ICPk8sComponentInfoMult()
		


		infoData.clusters.add(getDeploymentClusterCompleted())
		infoData.clusters.add(getDeploymentClusterIncompleted())
		
		
		return infoData
	}
	
	
	ICPk8sComponentInfoMult getObjectCompleted() {
		def infoData = new ICPk8sComponentInfoMult()
		

		infoData.clusters.add(getDeploymentClusterCompleted())
		

		infoData.clusters.add(getDeploymentClusterCompleted())
		
		
		return infoData
	}
	

		
			
    @Test
    void testAreAllTheInstancesAvailableIncompleted1() {
		
		ICPk8sComponentInfoMult icpActualStatusInfo=getObjectIncompleted1()
		
		ICPk8sInstancesApp icpInstances=icpActualStatusInfo.areAllTheInstanciesAvailable('b')
		
		assertNotSame(icpInstances.available , icpInstances.desired)
		
		assertEquals(icpInstances.desired,4)
		
     
    }
	
	@Test
	void testAreAllTheInstancesAvailableIncompleted() {
		
		ICPk8sComponentInfoMult icpActualStatusInfo=getObjectIncompleted()
		
		ICPk8sInstancesApp icpInstances=icpActualStatusInfo.areAllTheInstanciesAvailable('b')
		
		assertNotSame(icpInstances.available , icpInstances.desired)
		
		assertEquals(icpInstances.desired,4)
		
	 
	}
	
	@Test
	void testAreAllTheInstancesAvailableCompleted() {
		
		ICPk8sComponentInfoMult icpActualStatusInfo=getObjectCompleted()
		
		ICPk8sInstancesApp icpInstances=icpActualStatusInfo.areAllTheInstanciesAvailable('b')
		
		assertEquals(icpInstances.available , icpInstances.desired)
		
		assertEquals(icpInstances.desired,4)
	}
	

}
