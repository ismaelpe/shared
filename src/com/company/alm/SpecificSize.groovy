package com.project.alm

class SpecificSize implements Serializable {
	public String cpuSize;
	public String memorySize;
	public String replicaSize;
	
	SpecificSize(){
		
	}
	
	SpecificSize(def contentInJson){
		cpuSize = contentInJson?.cpuSize;
		memorySize = contentInJson?.memorySize;
		replicaSize = contentInJson?.replicaSize;
	}
}