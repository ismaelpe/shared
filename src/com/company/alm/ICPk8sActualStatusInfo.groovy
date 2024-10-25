package com.project.alm

import java.util.ArrayList

class ICPk8sActualStatusInfo{

	
	def currentColour
	def currentImage
	def currentVersion
	def deployId
	def readinessProbePath
	def livenessProbePath
	def envVars
    def replicas
	
	ICPk8sActualStatusInfo() {
		currentColour=null
		currentImage=null
		currentVersion=null
		deployId=null
		readinessProbePath=null
		livenessProbePath=null
		envVars=null
        replicas=null
	}

	String toString() {
		String returnValue=""
		
		returnValue="[currentColour:"+currentColour+",currentImage:"+currentImage+",currentVersion:"+currentVersion+",deployId:"+deployId+",readinessProbePath:"+readinessProbePath+",livenessProbePath:"+livenessProbePath+",envVars:"+envVars+",replicas:"+replicas+"]"
		
		return returnValue
	}
	
	String getNextColour() {
		if (currentColour==null) return "B"
		else { 
			if (currentColour=="B") return "G"
			else return "B"
		}			
	}
	
	String getNextImage() {
		if (currentImage==null || currentImage=="") return "A"
		else {
			if (currentImage.contains("SNAPSHOT")) {
				String actualColour=currentImage.substring(currentImage.length()-1)
				
				if (actualColour=="A") return "B"
				if (actualColour=="B") return "C"
				if (actualColour=="C") return "A"
				return "A"			
			}else return ""
		}
	}
}

