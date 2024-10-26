package com.project.alm

import java.util.ArrayList
import com.project.alm.Cloudk8sInstancesApp


import java.lang.Integer


class Cloudk8sComponentDeploymentInfo{
	
	String name
	String replicasAvailable
	String replicasDesired
	String replicasUnavailable
	boolean ready
	String creationTimestamp
	def errors
	
	
	Cloudk8sComponentDeploymentInfo() {
		errors=null
	}
	
	def areAllTheInstanciesAvailable(String colour) {
		if (name.toLowerCase().endsWith(colour.toLowerCase())) return areAllTheInstanciesAvailable()
		else return null
	}
	
	def areAllTheInstanciesAvailable() {
		
		if ((replicasDesired==null || "null".equals(replicasDesired)) && (replicasAvailable==null || "null".equals(replicasAvailable))	
			&&  (replicasUnavailable==null ||  "null".equals(replicasUnavailable))) throw new Exception("DEPLOY FALLIDO Faltan instancias")
		
		int replicasDesiredInt=((replicasDesired==null || "null".equals(replicasDesired))?0:Integer.parseInt(replicasDesired))
		int replicasAvailableInt=((replicasAvailable==null || "null".equals(replicasAvailable))?0:Integer.parseInt(replicasAvailable))
		int replicasUnavailableInt=((replicasUnavailable==null || "null".equals(replicasUnavailable))?0:Integer.parseInt(replicasUnavailable))
		
		Cloudk8sInstancesApp instancesApp=new Cloudk8sInstancesApp()
		instancesApp.available=replicasAvailableInt
		instancesApp.unavailable=replicasUnavailableInt
		instancesApp.desired=replicasDesiredInt

		return instancesApp
	}
	
	
	def isTheDeploymentReady() {	
		
		
		return ready		
	}
	

	String toString() {
		String resultado=""
		
		resultado=resultado+"[errors:"+errors+",name:"+name+",ready:"+ready+",replicasAvailable:"+replicasAvailable+",replicasDesired:"+replicasDesired+",replicasUnavailable:" +replicasUnavailable+ "]"
		
		return resultado
	}
	
}

