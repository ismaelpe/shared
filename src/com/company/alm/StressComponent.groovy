package com.caixabank.absis3

class StressComponent implements Serializable {
	public String icpName;
	public String garName;
	public String garType;
	public String version;
	public SpecificSize specificSize;
	
	StressComponent(){
		
	}
	
	StressComponent(def contentInJson){
		icpName = contentInJson.icpName
		garName = contentInJson.garName
		garType = contentInJson.garType
		version = icpName-garName
		if(contentInJson.specificSize) {
			specificSize = new SpecificSize(contentInJson.specificSize)
		}
	}
}