package com.caixabank.absis3

import java.util.ArrayList

class ICPk8sComponentServiceInfo{

	String name
	
	ICPk8sComponentServiceInfo() {
	}
	
	String toString() {
		String resultado="{ name: "+name+" } "		
		
		return resultado
	}
	
}
