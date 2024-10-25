package com.caixabank.absis3




class ICPk8sInstancesApp{

	int available=0
	int unavailable=0
	int desired=0 
	
	
	ICPk8sInstancesApp() {
		available=0
		unavailable=0
		desired=0
	}

	def increment(ICPk8sInstancesApp other) {
		available=available+other.available
		unavailable=unavailable+other.unavailable
		desired=desired+other.desired
	}
	
	def allTheInstancesAreOk() {
		

		if (unavailable>0) return false
		else if (desired==available) return true
		else return false
	}
	
	String toString() {
		return "[Instances available:" + available+ ",unavailable: "+ unavailable+ ",desired: " + desired+ "]"
	}
}

