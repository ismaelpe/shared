package com.caixabank.absis3

import java.util.ArrayList

import com.caixabank.absis3.ICPk8sInstancesApp


class ICPk8sComponentInfo{

	
	def deployments
	def services
	def pods
	
	ICPk8sComponentInfo() {
		deployments=new ArrayList()
		services=new ArrayList()
		pods=new ArrayList()
	}

	def getDeployId() {
		def deployId=""
		deployments.each { 
			def lastSuffix=it.name.substring(it.name.lastIndexOf("-")+1)

	        lastSuffix=lastSuffix.toUpperCase()

			if (lastSuffix!='G' && lastSuffix!='B') {
				deployId=lastSuffix
				return deployId
			}
			
		}	
		return deployId
	}
	
	def wereSomeInstancesRestarted(def initialPodList) {
		boolean result= false
		pods.each {
			if ( it.restarts >= 1 &&  notInsideInitialPodList(it.name,initialPodList) )
				result = true
		}
		return result
	}
	
	def wereSomeInstancesRestarted(String colour ,def initialPodList) {
		boolean result = false
		String deployName = null
		pods.each {
			deployName = it.controlledByName
			if (deployName.toLowerCase().endsWith(colour.toLowerCase())) {
				if (it.restarts >= 1 &&  notInsideInitialPodList(it.name,initialPodList) ) {
					result = true
				}
			}

		}
		return result
	}
	
	def notInsideInitialPodList(String name ,def initialPodList) {
		boolean result= true
		if (initialPodList!=null) {
			initialPodList.each {
				if ( it.name == name)
					result = false
			}
		}
		return result
	}
	
	def areAllTheInstanciesAvailable() {
		
		ICPk8sInstancesApp totalInstances=new ICPk8sInstancesApp()
		
		deployments.each {
			ICPk8sInstancesApp partialInstances=it.areAllTheInstanciesAvailable()			
			if (partialInstances!=null) totalInstances.increment(partialInstances)			
		}
		
		return totalInstances
		
	}
	
	def areAllTheInstanciesAvailable(String colour) {
		
		ICPk8sInstancesApp totalInstances=new ICPk8sInstancesApp()
		
		deployments.each {
			ICPk8sInstancesApp partialInstances=it.areAllTheInstanciesAvailable(colour)
			if (partialInstances!=null) totalInstances.increment(partialInstances)
		}
		
		return totalInstances
		
	}
	
	def deploymentError() {
		def result=null
		
		deployments.each {
			
			if (it.errors!=null) {
				result = it.errors
			}
		}
				
		return result
	}

	def isTheDeploymentReadyForColor(String colour) {
		boolean result=true

		deployments.each { 
			if (!it.isTheDeploymentReady() && it.name.toLowerCase().endsWith(colour.toLowerCase())) {
				result=it.isTheDeploymentReady()				
			}
		}
		
		return result
	}
	
	def isTheDeploymentReady() {
		boolean result=true

		deployments.each { 
			if (!it.isTheDeploymentReady()) {
				result=it.isTheDeploymentReady()				
			}
		}
		
		return result
	}
	
	String toString() {
		String resultado=""
		
		deployments.each{
			resultado=resultado+" [a "+it.toString()+" a],"
		}
		pods.each{
			resultado=resultado+" [pod "+it.toString()+" pod],"
		}
		
		return resultado
	}
}

