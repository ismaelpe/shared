package com.project.alm

import com.project.alm.GarAppType


class CloudAppResourcesCatalog extends CloudAppResources{

	String requestsMemory = "300Mi"
	String requestsCpu = "200m"
	String limitsMemory = ""
	String limitsCpu = "800m"
	String numInstances = "1"
	String memSize = "M"
	String cpuSize = "M"
	String replicasSize="M"
	
	String getNumInstances(String environment, boolean resizing) {
		return numInstances
	}
	
	String getNumInstances(String environment) {
		return getNumInstances(environment, false)
	}
	
	String getRequestsMemory(String environment) {
		return requestsMemory+"Mi"
	}
	
	String getRequestCPUPersonalized() {
		return requestsCpu+"m"
	}
	
	String getLimitsCPUPersonalized() {
		return limitsCpu+"m"
	}
	
	String getRequestMemoryPersonalized() {
		return requestsMemory+"Mi"
	}
	
	String getLimitsMemoryPersonalized() {		
		return limitsMemory+"Mi"
	}
	
	String getLimitsMemory(String environment) {
	    return getLimitsMemoryPersonalized()
	}
	
	String getRequestsCPU(String environment) {
		return getRequestCPUPersonalized()
	}
	
	String getLimitsCPU(String environment) {
		return getLimitsCPUPersonalized()
	}
	
	String getChartValues(String environment) {
		return "  resources:\n"+
		       "    requests:\n"+
			   "      memory: "+getRequestsMemory(environment)+"\n"+
			   "      cpu: "+getRequestsCPU(environment)+"\n"+
			   "    limits:\n"+
			   "       memory: "+getLimitsMemory(environment)+"\n"+
			   "       cpu: "+getLimitsCPU(environment)+"\n"		
	}
	
	
	String getChartAppValues(String environment) {
		return 	   "        requests_memory: "+getRequestsMemory(environment)+"\n"+
				   "        requests_cpu: "+getRequestsCPU(environment)+"\n"+
				   "        limits_memory: "+getLimitsMemory(environment)+"\n"+
				   "        limits_cpu: "+getLimitsCPU(environment)+"\n"

	}
	
	String toString() {
		return "MemSize: "+memSize+" CpuSize: "+cpuSize+" ReplicasSize: "+replicasSize+" numInstances: "+numInstances+" limitsMemory: "+getLimitsMemoryPersonalized()+" limitsCpu: "+getLimitsCPUPersonalized()
	}
	
	String getValue() {
		return "MemSize: "+memSize+" CpuSize: "+cpuSize+" ReplicasSize: "+replicasSize+" numInstances: "+numInstances+" limitsMemory: "+getLimitsMemoryPersonalized()+" limitsCpu: "+getLimitsCPUPersonalized()
	}
		
	
}
