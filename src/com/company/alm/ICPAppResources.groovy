package com.project.alm

import com.project.alm.GarAppType


class CloudAppResources{

	/**
	 * Datos del excel de capacity  
APPS		
	ConsumoCPU	LimitConsCPU
PRO	0,2	0,6
PREVIS	0,05	0,45
	ConsumeMem	LimitsMem
PRO	128	768
PREVIS	128	512
ARCH		
	ConsumoCPU	LimitConsCPU
PRO	0,2	1
PREVIS	0,1	0,75
	ConsumeMem	LimitsMem
PRO	128	1024
PREVIS	128	768

	 */
	
	//By default es micro aplicativo
	GarAppType garArtifactType = GarAppType.MICRO_SERVICE
	String memSize = "M"
	String cpuSize = "M"
	String replicasSize = "M"
	
	boolean allAppsMinimum = false 
	
	String requestsMemoryPre = "200Mi"
	String requestsCpuPre = "25m"
	String limitsMemoryPre = limitsMemory
	String limitsCpuPre = "800m"
	
	String requestsMemoryPro = "300Mi"
	String requestsCpuPro = "200m"
	String limitsMemoryPro = limitsMemory
	String limitsCpuPro = "800m"
	String limitsMemory = ""
	
	String jvmArgs
	
	String environment = ""
	boolean isArchProject = false
	String numInstances = 1
	
	String getNumInstances(String environment, boolean resizing) {
		if (allAppsMinimum && "PRO".equalsIgnoreCase(environment))  return "1"
		else if ("PRO".equalsIgnoreCase(environment) && "S".equals(replicasSize)) return "1"
	//Si el tamanyo es L se tiene que aplicar el resizing
		else if (("PRO".equalsIgnoreCase(environment) || resizing) && "L".equals(replicasSize)) return "3"
	    else if (("PRO".equalsIgnoreCase(environment) || resizing) && "XL".equals(replicasSize)) return "4"
	    else if (("PRO".equalsIgnoreCase(environment) || resizing) && "XXL".equals(replicasSize)) return "5"
	    else if (("PRO".equalsIgnoreCase(environment) || resizing) && "XXXL".equals(replicasSize)) return "6"
		else if ("PRO".equalsIgnoreCase(environment)) return "2"
		else return "1"
	}
	
	String getNumInstances(String environment) {
		return getNumInstances(environment, false)
	}
	
	
	String getRequestsMemory(String environment) {
		if ("PRO".equalsIgnoreCase(environment)) return getRequestMemoryPersonalized()
		if (garArtifactType == GarAppType.DATA_SERVICE) return "500Mi"
		return "450Mi"
	}
	
	String getRequestCPUPersonalized() {
		if (cpuSize.equalsIgnoreCase("L")) return "300m"
		else if (cpuSize.equalsIgnoreCase("XL")) return "600m"
		else if (cpuSize.equalsIgnoreCase("XXL")) return "1000m"
		else if (cpuSize.equalsIgnoreCase("XXXL")) return "1500m"
		else if (cpuSize.equalsIgnoreCase("S")) return "50m"
		return "100m"
	}
	
	String getLimitsCPUPersonalized() {
		if (cpuSize.equalsIgnoreCase("L")) return "700m"
		else if (cpuSize.equalsIgnoreCase("XL")) return "1500m"
		else if (cpuSize.equalsIgnoreCase("XXL")) return "2000m"
		else if (cpuSize.equalsIgnoreCase("XXXL")) return "3000m"
		else if (cpuSize.equalsIgnoreCase("S")) return "450m"
		return "550m"
	}
	
	String getRequestMemoryPersonalized() {
		if (garArtifactType ==  GarAppType.MICRO_SERVICE) {
			if (memSize.equalsIgnoreCase("L")) return "700Mi"
			else if (memSize.equalsIgnoreCase("XL")) return "1000Mi"
			else if (memSize.equalsIgnoreCase("XXL")) return "1500Mi"
			else if (memSize.equalsIgnoreCase("XXXL")) return "2000Mi"
			else if (memSize.equalsIgnoreCase("S")) return "450Mi"
			return "550Mi"
		} else if (garArtifactType ==  GarAppType.DATA_SERVICE) {
			if (memSize.equalsIgnoreCase("L")) return "700Mi"
			else if (memSize.equalsIgnoreCase("XL")) return "1000Mi"
			else if (memSize.equalsIgnoreCase("XXL")) return "1500Mi"
			else if (memSize.equalsIgnoreCase("XXXL")) return "2000Mi"
			else if (memSize.equalsIgnoreCase("S")) return "500Mi"
			return "600Mi"
		} else {
			if (memSize.equalsIgnoreCase("L")) return "700Mi"
			else if (memSize.equalsIgnoreCase("XL")) return "1000Mi"
			else if (memSize.equalsIgnoreCase("XXL")) return "1500Mi"
			else if (memSize.equalsIgnoreCase("XXXL")) return "2000Mi"
			else if (memSize.equalsIgnoreCase("S")) return "450Mi"
            return "550Mi"
		}
	}
	
	String getLimitsMemoryPersonalized() {		
		if (garArtifactType ==  GarAppType.MICRO_SERVICE) {
			if (memSize.equalsIgnoreCase("L")) return "700Mi"
			else if (memSize.equalsIgnoreCase("XL")) return "1000Mi"
			else if (memSize.equalsIgnoreCase("XXL")) return "1500Mi"
			else if (memSize.equalsIgnoreCase("XXXL")) return "2000Mi"
			else if (memSize.equalsIgnoreCase("S")) return "450Mi"
            return "550Mi"
		} else if (garArtifactType ==  GarAppType.DATA_SERVICE) {
			if (memSize.equalsIgnoreCase("L")) return "700Mi"
			else if (memSize.equalsIgnoreCase("XL")) return "1000Mi"
			else if (memSize.equalsIgnoreCase("XXL")) return "1500Mi"
			else if (memSize.equalsIgnoreCase("XXXL")) return "2000Mi"
			else if (memSize.equalsIgnoreCase("S")) return "500Mi"
			return "600Mi"
		} else {
			if (memSize.equalsIgnoreCase("L")) return "700Mi"
			else if (memSize.equalsIgnoreCase("XL")) return "1000Mi"
			else if (memSize.equalsIgnoreCase("XXL")) return "1500Mi"
			else if (memSize.equalsIgnoreCase("XXXL")) return "2000Mi"
			else if (memSize.equalsIgnoreCase("S")) return "450Mi"
			return "550Mi"
		}
	}
	
	String getLimitsMemory(String environment) {
	   if (!"PRO".equalsIgnoreCase(environment) && memSize!=null && memSize.contains("XL")) {
			memSize="L" //Si no es PRO no podemos tener tamaños X
	   }
	   return getLimitsMemoryPersonalized()
	}
	
	String getRequestsCPU(String environment) {
		if (isArchProject) {
			if ("PRO".equalsIgnoreCase(environment)) return getRequestCPUPersonalized()
			else return "25m"
		}else {
			if ("PRO".equalsIgnoreCase(environment)) return getRequestCPUPersonalized()
			else return "25m"
		}
	}
	
	String getLimitsCPU(String environment) {
		if (!"PRO".equalsIgnoreCase(environment) && cpuSize!=null && cpuSize.contains("XL")) {
			cpuSize="L" //Si no es PRO no podemos tener tamaños X
	    }
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
	
	String getJvmConfig(String jvmConfig) {
		if (jvmArgs != null) {
			if (jvmConfig==null || "".equals(jvmConfig)) {
				return "${jvmArgs}"
			}else {
				return "${jvmConfig} ${jvmArgs}"
			}
			
		} else {
			return jvmConfig
		}		
	}
	
	String toString() {
		return "MemSize: "+memSize+" CpuSize: "+cpuSize+" ReplicasSize: "+replicasSize+" numInstances: "+numInstances+" limitsMemory: "+getLimitsMemoryPersonalized()+" limitsCpu: "+getLimitsCPUPersonalized()
	}
	
}
