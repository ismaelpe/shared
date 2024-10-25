package com.project.alm


import java.time.LocalDate
import java.time.ZoneId

import com.project.alm.Utilities
import com.project.alm.BmxStructure
import com.project.alm.EdenBmxStructure
import com.project.alm.DevBmxStructure
import com.project.alm.TstBmxStructure
import com.project.alm.PreBmxStructure
import com.project.alm.ProBmxStructure
import com.project.alm.JavaVersionType
import com.project.alm.GlobalVars
import com.project.alm.Java11Buildpack
import com.project.alm.Java8Buildpack
import org.apache.commons.lang.RandomStringUtils

class BmxUtilities {

    Script script;

    static String calculateRoute(PomXmlStructure pomXml, BranchStructure branchStructure) {
        String newRoute = "";

        if (branchStructure.branchType == BranchType.FEATURE) {
            newRoute = pomXml.getBmxAppId() + "-" + branchStructure.featureNumber
        } else if (branchStructure.branchType == BranchType.MASTER)
            newRoute = pomXml.getBmxAppId() + "-dev"
        else if (branchStructure.branchType == BranchType.RELEASE || branchStructure.branchType == BranchType.HOTFIX)
            newRoute = pomXml.getBmxAppId()


        return newRoute.toLowerCase()
    }

    static String calculatePathToMicro(PomXmlStructure pomXml, BranchStructure branchStructure,ICPStateUtility icpStateUtility) {

        String pathToMicro=""
		
		if (branchStructure.branchType == BranchType.FEATURE) {
			//si es feature
			pathToMicro=icpStateUtility.pathFeature
		}else if (icpStateUtility.sampleAppFlag) {
			//si es sample app
			pathToMicro= calculateArtifactId(pomXml,branchStructure,true).toLowerCase()
		}else {
			//si es micro
			pathToMicro = calculateArtifactId(pomXml, branchStructure,false).toLowerCase()
		}
		
		if (pomXml.isArchProject()) {
			pathToMicro="arch-service/"+pathToMicro
		}

		
		return pathToMicro
    }

    static String calculateFeatureAppName(PomXmlStructure pomXml, PipelineData pipelineData, scriptContext = null) {
        String featureId = ""
        if (pipelineData.branchStructure.featureNumber != null) {
            if (pipelineData.branchStructure.featureNumber.isInteger()){
                featureId = pipelineData.branchStructure.featureNumber
            } else {
                if ((pipelineData.branchStructure.featureNumber.toLowerCase().startsWith("us") ||
                        pipelineData.branchStructure.featureNumber.toLowerCase().startsWith("de") ||
                        pipelineData.branchStructure.featureNumber.toLowerCase().startsWith("ta")) && pipelineData.branchStructure.featureNumber.length()>2) {
                    //Validar que es numerico... sino no sirve
                    String value = pipelineData.branchStructure.featureNumber.substring(2)
                    
                    if (value.isInteger()) {
                         featureId=pipelineData.branchStructure.featureNumber.toLowerCase()
                    }
                }
            }
        }
        
        if (""==featureId) {
            //Tenemos que generar un random
            Random generateRandom = new Random()
            featureId = "" + generateRandom.nextInt(999999)
        }
        
        String featureAppName = (pomXml.getBmxAppId() - '-micro') + '-' + featureId;
        
        if(featureAppName.length() > GlobalVars.LIMIT_LENGTH_FOR_SERVICE_WITHOUT_K8_SUFFIX) {
            String nameAppWithoutMicro=(pomXml.getBmxAppId()-'-micro')
        
            String charset = "abcdefghijklmnopqrstuvwxyz0123456789"
            Integer length = GlobalVars.LIMIT_LENGTH_FOR_SERVICE_WITHOUT_K8_SUFFIX-nameAppWithoutMicro.length()-1
            String suffixForEden = RandomStringUtils.random(length, charset.toCharArray())
        
            String singleDigitUs = "" + suffixForEden
            featureAppName = nameAppWithoutMicro + '-' + singleDigitUs;
        }
        
        return featureAppName
    }
	
	static String calculateArtifactId(PomXmlStructure pomXml, BranchStructure branchStructure) {
		
				return calculateArtifactId(pomXml, branchStructure, false)
	}

    static String calculateArtifactId(PomXmlStructure pomXml, BranchStructure branchStructure, boolean edenTheLast) {
        //Si es una sample app, vamos a poner la fecha sea cual sea el entorno de instalacion
        String dateArtifact = ''

        //if (pomXml.itContainsSampleApp() && edenTheLast)
        if (pomXml.itContainsSampleApp() && !edenTheLast)
            dateArtifact = '-' + Utilities.getActualDate("yyyyMMdd")


        if (branchStructure.branchType == BranchType.FEATURE)
            return pomXml.getBmxAppId() + "-" + branchStructure.featureNumber +  ((edenTheLast) ? "" : "-"+Utilities.getActualDate("yyyyMMdd"))

        else if (branchStructure.branchType == BranchType.MASTER)
            return pomXml.getBmxAppId() + "-dev" + dateArtifact

        else if (branchStructure.branchType == BranchType.RELEASE || branchStructure.branchType == BranchType.HOTFIX)
            return pomXml.getBmxAppId() + dateArtifact
    }

    List<SyntheticTestStructure> getAppsFromString(def apps) {
        List appsArray = Utilities.splitStringToList(apps, "NonExisting")

        List<SyntheticTestStructure> syntheticTestList = new ArrayList<SyntheticTestStructure>()

        for (String item : appsArray) {
            SyntheticTestStructure syntheticTestStructure = new SyntheticTestStructure()
            //appName es del resultado 'cf apps' , sirve para formar el url endpoint de la app
            syntheticTestStructure.appName = item
            syntheticTestList.add(syntheticTestStructure)
        }

        return syntheticTestList

    }

    List<SyntheticTestStructure> cloneFrom(List<SyntheticTestStructure> syntheticTestList) {


        List<SyntheticTestStructure> auxList = new ArrayList<SyntheticTestStructure>()

        for (SyntheticTestStructure item : syntheticTestList) {

            auxList.add(item)
        }

        return auxList

    }
	
	List<SyntheticTestStructure> addStringList(List<SyntheticTestStructure> syntheticTestList,def whiteListArch,String enviromentParam,String dataCenterParam,boolean isArchMicro) {
		for (String item : whiteListArch) {
			SyntheticTestStructure syntheticTestStructure = new SyntheticTestStructure()
			syntheticTestStructure.appName = item
			if (isArchMicro) {
				syntheticTestStructure.urlIcp = "https://k8sgateway.${enviromentParam.toLowerCase()}.icp-${dataCenterParam}.absis.cloud.lacaixa.es/arch-service/${syntheticTestStructure.appName}"
                syntheticTestStructure.isArchMicro = true
			}else {
				syntheticTestStructure.urlIcp = "https://k8sgateway.${enviromentParam.toLowerCase()}.icp-${dataCenterParam}.absis.cloud.lacaixa.es/${syntheticTestStructure.appName}"
                syntheticTestStructure.isArchMicro = false
			}
			syntheticTestStructure.resultOK = true
			syntheticTestList.add(syntheticTestStructure)
		}
		return syntheticTestList
	}

	
	List<SyntheticTestStructure> cloneOnlyErrors(List<SyntheticTestStructure> syntheticTestList) {
		
		
		List<SyntheticTestStructure> auxList = new ArrayList<SyntheticTestStructure>()
		
		for (SyntheticTestStructure item : syntheticTestList) {
			if (!item.resultOK) {
					auxList.add(item)
			}
		}
		
		return auxList
		
	}

    List<SyntheticTestStructure> filterEnviroment(List<SyntheticTestStructure> syntheticTestList, String enviroment) {


        List<SyntheticTestStructure> syntheticTestListResult = null

        if (enviroment == 'DEV') {
            syntheticTestListResult = syntheticTestList.findAll { (it.appName.length() >= 3) && (it.appName.substring(it.appName.length() - 3, it.appName.length()).equals('dev')) }
        } else {
            syntheticTestListResult = syntheticTestList.findAll { (it.appName.length() >= 3) && !(it.appName.substring(it.appName.length() - 3, it.appName.length()).equals('dev')) }
        }

        return syntheticTestListResult

    }

    List<SyntheticTestStructure> filterAncient(List<SyntheticTestStructure> syntheticTestList) {


        syntheticTestList = syntheticTestList.findAll { !(it.appName.startsWith('ancient')) }

        return syntheticTestList

    }


    List<SyntheticTestStructure> filterWhiteList(List<SyntheticTestStructure> syntheticTestList, String[] whiteListApps) {

        List<String> whiteList = Arrays.asList(whiteListApps);

        List<SyntheticTestStructure> filteredSyntheticTestList = new ArrayList<SyntheticTestStructure>()

        for (String whiteItem : whiteList) {
            for (SyntheticTestStructure allItem : syntheticTestList) {
                if (allItem.appName.startsWith(whiteItem)) {
                    filteredSyntheticTestList.add(allItem)
                }
            }
        }

        return filteredSyntheticTestList;

    }


    List<SyntheticTestStructure> filterAppName(List<SyntheticTestStructure> syntheticTestList, String appname) {

        //if appname is no blank
        if (appname?.trim()) {
            syntheticTestList = syntheticTestList.findAll { it.appName.contains(appname) }
        }
        return syntheticTestList

    }

    List<SyntheticTestStructure> filterFirst(List<SyntheticTestStructure> syntheticTestList) {


        List<SyntheticTestStructure> newSyntheticTestList = new ArrayList<SyntheticTestStructure>()
        newSyntheticTestList.add(syntheticTestList.get(0))

        return newSyntheticTestList

    }


    List<SyntheticTestStructure> filterResultOk(List<SyntheticTestStructure> syntheticTestList) {

        syntheticTestList = syntheticTestList.findAll { (it.resultOK) }

        return syntheticTestList

    }

    List<SyntheticTestStructure> filterResultKo(List<SyntheticTestStructure> syntheticTestList) {

        syntheticTestList = syntheticTestList.findAll { (!it.resultOK) }

        return syntheticTestList

    }

    List<SyntheticTestStructure> filterAlive(List<SyntheticTestStructure> syntheticTestList) {


        for (SyntheticTestStructure item : syntheticTestList) {            
            def url = "https://${item.appName}.tst.int.srv.caixabank.com/actuator/info"

            def response = null

            def errorMessage = null
            try {
                response = httpRequest url
            } catch (Exception e) {                
                errorMessage = e.getMessage()
            }
            if (errorMessage != null) continue


            def json = new groovy.json.JsonSlurper().parseText(response.content)

            item.pomVersion = "${json.build.version}"
            item.pomArtifactId = "${json.build.artifact}"
            item.pomGroup = "${json.build.group}"


        }

        return syntheticTestList

    }


    List<String> getAppsStartedBefore(def apps, int days) {        
        List appsArray = Utilities.splitStringToList(apps, "NonExisting")


		LocalDate today = LocalDate.now()
		LocalDate priorDate = today.minusDays(days)

        if (script != null) script.printOpen("The apps is  " + apps + " appsArray ", EchoLevel.ALL)

        return appsArray.findAll({ app -> obtainDateFromApp(app.trim()).before(Date.from(priorDate.atStartOfDay(ZoneId.systemDefault()).toInstant())) })
    }

    private static Date obtainDateFromApp(String appName) {
        def matcher = GlobalVars.APP_STARTED_REGEX
        def findDate = (appName =~ /$matcher/)
        if (findDate.size() > 0) {
            String dateText = findDate[0]
            return Utilities.parseDate("yyyyMMdd", dateText)
        } else return new Date()

    }


    static BmxStructure buildStructure(String environment) {
        if (environment == GlobalVars.EDEN_ENVIRONMENT) return new EdenBmxStructure()
        else if (environment == GlobalVars.DEV_ENVIRONMENT) return new DevBmxStructure()
        else if (environment == GlobalVars.TST_ENVIRONMENT) return new TstBmxStructure()
        else if (environment == GlobalVars.PRE_ENVIRONMENT) return new PreBmxStructure()
        else if (environment == GlobalVars.PRO_ENVIRONMENT) return new ProBmxStructure()
    }   

}
