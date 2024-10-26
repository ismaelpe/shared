package com.project.alm

class StressComponent implements Serializable {
	public String cloudName;
	public String garName;
	public String garType;
	public String version;
	public SpecificSize specificSize;
	
	StressComponent(){
		
	}
	
	StressComponent(def contentInJson){
		cloudName = contentInJson.cloudName
		garName = contentInJson.garName
		garType = contentInJson.garType
		version = cloudName-garName
		if(contentInJson.specificSize) {
			specificSize = new SpecificSize(contentInJson.specificSize)
		}
	}
}