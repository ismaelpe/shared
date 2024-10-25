package com.caixabank.absis3

import java.util.ArrayList
import com.caixabank.absis3.ICPk8sInstancesApp


import java.lang.Integer


class ICPk8sComponentPodInfo{


		
	String name
	String controlledByName
	int restarts
	String creationTimestamp

	
	
	ICPk8sComponentPodInfo() {
	}

	String toString() {
		String resultado=""
		
		resultado=resultado+"[name:"+name+",controlledByName:"+controlledByName+",restarts:"+restarts+",creationTimestamp:" +creationTimestamp+ "]"
		
		return resultado
	}
	
}

