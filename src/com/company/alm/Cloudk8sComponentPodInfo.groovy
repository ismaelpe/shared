package com.project.alm

import java.util.ArrayList
import com.project.alm.Cloudk8sInstancesApp


import java.lang.Integer


class Cloudk8sComponentPodInfo{


		
	String name
	String controlledByName
	int restarts
	String creationTimestamp

	
	
	Cloudk8sComponentPodInfo() {
	}

	String toString() {
		String resultado=""
		
		resultado=resultado+"[name:"+name+",controlledByName:"+controlledByName+",restarts:"+restarts+",creationTimestamp:" +creationTimestamp+ "]"
		
		return resultado
	}
	
}

