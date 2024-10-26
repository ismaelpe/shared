package com.project.alm

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
			archComponents.put(component.cloudName, component);
		}
		contentInJson?.appComponents?.each { it ->
			component = new StressComponent(it)
			appComponents.put(component.cloudName, component);
		}
	}
	
	List getArchComponentNameList() {
		List archComponentName = new ArrayList<String>()
		archComponents?.each { it ->
			archComponentName.add(it.value.cloudName)
		}
		return archComponentName;
	}
	
	List getAppComponentNameList() {
		List appComponentsName = new ArrayList<String>()
		appComponents?.each { it ->
			appComponentsName.add(it.value.cloudName)
		}
		return appComponentsName;
	}
	
	StressComponent getArchStressComponentByCloudName(String cloudName) {
		return archComponents.get(cloudName)
	}
	
	StressComponent getAppStressComponentByCloudName(String cloudName) {
		return appComponents.get(cloudName)
	}
}





