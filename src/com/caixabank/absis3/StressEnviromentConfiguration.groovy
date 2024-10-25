package com.caixabank.absis3

class StressEnviromentConfiguration implements Serializable {
	
	Map<String,StressComponent> archComponents
	Map<String, StressComponent> appComponents
	
	Map valuesArchDeployed = [:]
	Map valuesAppDeployed = [:]
	
	StressEnviromentConfiguration() {
		archComponents = new HashMap<String, StressComponent>();
		appComponents = new HashMap<String, StressComponent>();
	}
	
	StressEnviromentConfiguration(def contentInJson){
		archComponents = new HashMap<String, StressComponent>();
		appComponents = new HashMap<String, StressComponent>();
		StressComponent component;
		contentInJson?.archComponents?.each { it ->
			component = new StressComponent(it)
			archComponents.put(component.icpName, component);
		}
		contentInJson?.appComponents?.each { it ->
			component = new StressComponent(it)
			appComponents.put(component.icpName, component);
		}
	}
	
	List getArchComponentNameList() {
		List archComponentName = new ArrayList<String>()
		archComponents?.each { it ->
			archComponentName.add(it.value.icpName)
		}
		return archComponentName;
	}
	
	List getAppComponentNameList() {
		List appComponentsName = new ArrayList<String>()
		appComponents?.each { it ->
			appComponentsName.add(it.value.icpName)
		}
		return appComponentsName;
	}
	
	StressComponent getArchStressComponentByIcpName(String icpName) {
		return archComponents.get(icpName)
	}
	
	StressComponent getAppStressComponentByIcpName(String icpName) {
		return appComponents.get(icpName)
	}
}





