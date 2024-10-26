package com.project.alm




class Cloudk8sInstancesApp{

	int available=0
	int unavailable=0
	int desired=0 
	
	
	Cloudk8sInstancesApp() {
		available=0
		unavailable=0
		desired=0
	}

	def increment(Cloudk8sInstancesApp other) {
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

