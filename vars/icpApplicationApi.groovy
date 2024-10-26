import com.project.alm.EchoLevel
import groovy.json.JsonOutput
import hudson.Functions

def updateChartAndDockerVersionsOrShowEligibleForUpdate(def apps, String namespace, Integer chartVersion,
                                                        Integer dockerVersion, boolean updateVersion = false) {

    apps.each {

        try {

            boolean chartRequiresUpdate = (it.chart.id as Integer) < chartVersion
            boolean dockerRequiresUpdate = (it.version.id as Integer) < dockerVersion

            if (chartRequiresUpdate && updateVersion) { it.chart.id = chartVersion }
            if (dockerRequiresUpdate && updateVersion) { it.version.id = dockerVersion }
			
			printOpen("Update? ${chartRequiresUpdate} ${dockerRequiresUpdate} ${updateVersion}", EchoLevel.ALL)
			printOpen("The new docker images ${chartVersion} ${dockerVersion} ${updateVersion}", EchoLevel.ALL)

            if (chartRequiresUpdate || dockerRequiresUpdate) {

                String appName = it.name
                it.technology = null    // This is required as it's related to the chart and docker combination
                String url = getCloudManagementPath(namespace, appName)
                String body = JsonOutput.prettyPrint(JsonOutput.toJson(it))

                if (updateVersion) {
                    sendRequestToCloudApi(url, it, "PUT", namespace, "", false, false)

                } else {

                    printOpen("The following entry is elligible for updating:\n\n${body}", EchoLevel.ALL)

                }

            }

        } catch (Exception e) {

            printOpen(Functions.printThrowable(e), EchoLevel.ERROR)
            return false

        }

    }

}

def dumpAppsMetadataToLog(def apps, String name = '') {

    printOpen("Dumping ${name ? name : 'apps'} info to log:", EchoLevel.ALL)
    apps.each {
        printOpen("${JsonOutput.prettyPrint(JsonOutput.toJson(it))}", EchoLevel.ALL)
    }

}

def getCloudAppInfo(String namespace) {

    String url = getCloudManagementPath(namespace)
    return sendRequestToCloudApi(url, null, "GET", namespace,"", false, false)

}

def filterApps(def apps, def whiteList, def dockerVersionBlacklist) {

    def filteredApps = []

    apps.each {
        if (((whiteList.length == 1 && whiteList[0] == '') || whiteList.contains(it.name)) && ! (dockerVersionBlacklist.contains(it.version.id)) ) {

            filteredApps.add(it)

        }

    }

    return filteredApps
}

def getCloudManagementPath(String namespace, String appName = '') {

    return "v1/api/application/PCLD/${namespace}/component${appName ? '/'+appName : ''}"

}
