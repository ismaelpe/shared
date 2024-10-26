package com.project.alm

import java.util.ArrayList

class Cloudk8sComponentServiceInfo{

	String name
	
	Cloudk8sComponentServiceInfo() {
	}
	
	String toString() {
		String resultado="{ name: "+name+" } "		
		
		return resultado
	}
	
}

