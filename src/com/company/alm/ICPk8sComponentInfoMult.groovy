package com.project.alm

import java.util.ArrayList
import com.project.alm.Cloudk8sInstancesApp

class Cloudk8sComponentInfoMult{

	
	def clusters
	
	Cloudk8sComponentInfoMult() {
		clusters=new ArrayList()		
	}
	
	def getDeployId() {
		if (clusters.size()>1) {
			return clusters.get(0).getDeployId()
		}
		return ""
	}
	
	def deploymentError() {
		def result=null
		
		clusters.each {
			
			if (it.deploymentError()!=null) {
				result=it.deploymentError()
			}
		}
		
		return result
	}

	def isTheDeploymentReadyForColour(String colour) {
		boolean result=true
		
		clusters.each {  
			if (!it.isTheDeploymentReadyForColor(colour)) {
				result=it.isTheDeploymentReadyForColor(colour) 				
			} 
		}
		
		return result
	}
	
	def isTheDeploymentReady() {
		boolean result=true
		
		
		clusters.each {  
			if (!it.isTheDeploymentReady()) {
				result=it.isTheDeploymentReady() 				
			} 
		}
		
		return result
	}
	
	def wereSomeInstancesRestarted(def initialPodList) {
		boolean result= false
		clusters.each {
			if (it.wereSomeInstancesRestarted(initialPodList))
				result = true
		}
		return result
	}
	
	
	def wereSomeInstancesRestarted(String colour,def initialPodList) {
		boolean result= false
		clusters.each {
			if (it.wereSomeInstancesRestarted(colour,initialPodList))
				result =  true
		}
		return result
	}
	
	def getPodList() {
		ArrayList pods=new ArrayList()
		clusters.each {
			pods.addAll(it.pods)
		}
		return pods
	}
	
	def areAllTheInstanciesAvailable() {
		
		Cloudk8sInstancesApp totalInstances=new Cloudk8sInstancesApp()
		
		clusters.each {
			Cloudk8sInstancesApp partialInstances=it.areAllTheInstanciesAvailable()
			if (partialInstances!=null)	totalInstances.increment(partialInstances)
			
		}
		
		return totalInstances
		
	}
	
	def areAllTheInstanciesAvailable(String colour) {
		
		Cloudk8sInstancesApp totalInstances=new Cloudk8sInstancesApp()
		
		clusters.each {
			Cloudk8sInstancesApp partialInstances=it.areAllTheInstanciesAvailable(colour)
			if (partialInstances!=null)	totalInstances.increment(partialInstances)
			
		}
		
		return totalInstances
		
	}
	
	String toString() {
		String result=""
		
		clusters.each{
			result=result+" [M "+it.toString()+" M] ,"
		}
		
		return result
	}
	
}

