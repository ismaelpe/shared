import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.BmxUtilities
import com.project.alm.SampleAppCleanMode

def call(SampleAppCleanMode sampleAppCleanMode, int days) {

    def regex

    switch (sampleAppCleanMode) {
        case SampleAppCleanMode.EDEN:
            regex = GlobalVars.EDEN_SAMPLE_APPS_REGEX
            break
        case SampleAppCleanMode.DEV:
            regex = GlobalVars.DEV_SAMPLE_APPS_REGEX
            break
        case SampleAppCleanMode.TST:
            regex = GlobalVars.TST_SAMPLE_APPS_REGEX
            break
        case SampleAppCleanMode.PRE:
            regex = GlobalVars.PRE_SAMPLE_APPS_REGEX
            break
        default:
            regex = GlobalVars.ALL_SAMPLE_APPS_REGEX
            break
    }

    printOpen("Finding sample apps for ${sampleAppCleanMode} using regex ${regex}", EchoLevel.DEBUG)

    def sampleApps = sh(returnStdout: true, script: "cf apps | grep -E -o '${regex}' || true")

    printOpen("Sample apps for ${sampleApps} using regex ${regex} days ${days}", EchoLevel.DEBUG)

    if (sampleApps != null) {
        def bmxUtilities = new BmxUtilities(script: this)

        def sampleAppsArray = bmxUtilities.getAppsStartedBefore(sampleApps, days)

        //def sampleAppsArray = sampleApps.split("\n")

        printOpen("Deleting sample apps in ${sampleAppsArray}", EchoLevel.DEBUG)

        if (sampleAppsArray != null) {
            sampleAppsArray.each { app ->
                printOpen("Deleting ${app}", EchoLevel.DEBUG)
                sh("cf delete -r -f ${app}")
            }
        }

    } else {
        printOpen("No sample apps found to delete", EchoLevel.INFO)
    }


}
