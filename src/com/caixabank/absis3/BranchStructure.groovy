package com.caixabank.absis3

import com.caixabank.absis3.BranchType
import com.caixabank.absis3.GlobalVars

import com.cloudbees.groovy.cps.NonCPS

class BranchStructure {

    String branchName
    String featureNumber
    String releaseNumber
    BranchType branchType

    def getFeatureRelease() {
        if (branchType == BranchType.FEATURE) {
            return featureNumber
        } else if (branchType == BranchType.RELEASE) {
            return releaseNumber
        } else if (branchType == BranchType.CONFIGFIX) {
            return releaseNumber
        }
        return ""
    }

    def init() {
		if (branchName.indexOf(GlobalVars.PROTOTYPE_BRANCH) != -1) branchType = BranchType.PROTOTYPE
        else if (branchName.indexOf(GlobalVars.FEATURE_BRANCH) != -1) branchType = BranchType.FEATURE
        else if (branchName.indexOf(GlobalVars.RELEASE_BRANCH) != -1) branchType = BranchType.RELEASE
        else if (branchName.indexOf(GlobalVars.MASTER_BRANCH) != -1) branchType = BranchType.MASTER
        else if (branchName.indexOf(GlobalVars.HOTFIX_BRANCH) != -1) branchType = BranchType.HOTFIX
		else if (branchName.indexOf(GlobalVars.CONFIGFIX_BRANCH) != -1) branchType = BranchType.CONFIGFIX
        else branchType = BranchType.UNKNOWN

        initFeatureFromBranchName()

        if (branchName.startsWith("origin")) branchName = branchName.replace("origin/", "")
    }


    void initFeatureFromBranchName() {

        if (branchType == BranchType.FEATURE) {
            def index = branchName.indexOf(GlobalVars.FEATURE_NUM_SEPARATOR)
            def index1 = branchName.indexOf(GlobalVars.FEATURE_NUM_SEPARATOR1)

            if (index1 == -1 && index == -1) featureNumber = ""
            else {
                int iniciFeatureNumber = 0
                int fiFeatureNumber = 0

                if (index1 > index) {
                    iniciFeatureNumber = index1 + 1
                    featureNumber = branchName.substring(index1 + 1)
                } else {
                    iniciFeatureNumber = index + 1
                    featureNumber = branchName.substring(index + 1)
                }
                fiFeatureNumber = branchName.indexOf(GlobalVars.FEATURE_DESC_SEPARATOR)

                if (fiFeatureNumber == -1) fiFeatureNumber = branchName.length()
                featureNumber = branchName.substring(iniciFeatureNumber, fiFeatureNumber)
            }

        } else {
            featureNumber = ""
            if (branchType == BranchType.RELEASE || branchType == BranchType.HOTFIX || branchType == BranchType.CONFIGFIX) {
                def index = branchName.indexOf(GlobalVars.FEATURE_NUM_SEPARATOR1)
                String releaseNumberTemp = branchName.substring(index + 1)
                releaseNumber = releaseNumberTemp.replaceAll('\'', '')
            }
        }
		if (featureNumber.indexOf('BBDD')==-1 && featureNumber.length() > 16){ //Truncamos a 16 maximo , si contiene BBDD no es necesario truncar
			featureNumber = featureNumber.substring(0, 16)
            // Nos aseguramos que el ultimo caracter sea alfanumerico
            while(featureNumber ==~ GlobalVars.LAST_CHARACTER_IS_NOT_ALPHANUMERIC_REGEX){				
				featureNumber = featureNumber.substring(0, featureNumber.length() - 1)		
			}
            if (featureNumber ==~ GlobalVars.ONE_CHARACTER_IS_NOT_ALPHANUMERIC_REGEX){
                featureNumber = GlobalVars.DEFAULT_FEATURE_BRACH_NAME
            }
        }

    }

	@NonCPS
    String toString() {
        return "BranchStructure:\n" +
                "\tbranchName: $branchName\n" +
                "\tfeatureNumber: $featureNumber\n" +
                "\treleaseNumber: $releaseNumber\n" +
                "\tbranchType: $branchType"
    }
}
