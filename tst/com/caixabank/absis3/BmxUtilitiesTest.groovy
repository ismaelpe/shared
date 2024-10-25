package com.caixabank.absis3

import java.time.ZonedDateTime
import java.time.LocalDate
import java.time.ZoneId

class BmxUtilitiesTest extends GroovyTestCase {


    void testSampleAppsToDelete() {
        def bmxUtilities = new BmxUtilities()
        LocalDate today = LocalDate.now()
        LocalDate twoDaysAgo = today.minusDays(2)
        LocalDate before = today.minusDays(4)
		
        def todaytext = bmxUtilities.SDF.format(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()))
        def twoDaysAgoText = bmxUtilities.SDF.format(Date.from(twoDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant()))
        def beforetext = bmxUtilities.SDF.format(Date.from(before.atStartOfDay(ZoneId.systemDefault()).toInstant()))

        String apps = "demo-alm-plugin-sample-app-1-20190110\n"


        def appsToDelete = bmxUtilities.getAppsStartedBefore(apps, 3)
        assertEquals(appsToDelete, Arrays.asList("demo-alm-plugin-sample-app-1-20190110"))
    }

    void testEdenAppsToDelete() {
        def bmxUtilities = new BmxUtilities()

        LocalDate today = LocalDate.now()
        LocalDate twoDaysAgo = today.minusDays(2)
        LocalDate before = today.minusDays(4)
		
        def todaytext = bmxUtilities.SDF.format(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()))
        def twoDaysAgoText = bmxUtilities.SDF.format(Date.from(twoDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant()))
        def beforetext = bmxUtilities.SDF.format(Date.from(before.atStartOfDay(ZoneId.systemDefault()).toInstant()))

        String apps = "cics-micro-server-1-400-" + todaytext + "\n" +
                "dec-micro-server-1-515-" + beforetext + "\n" +
                "demo-pipeline-micro-3-375-" + twoDaysAgoText + "\n" +
                "absis-micro-common-sample-app-1-495-" + twoDaysAgoText + "\n" +
                "dec-plugin-sample-app-1-513" + "\n" +
                "dec-plugin-sample-app-1-513-" + beforetext + "\n"


        def appsToDelete = bmxUtilities.getAppsStartedBefore(apps, 2)


        assertNotNull(appsToDelete)
        assertFalse(appsToDelete.isEmpty())
        assertEquals(2, appsToDelete.size())
        assertEquals(appsToDelete, Arrays.asList("dec-micro-server-1-515-" + beforetext, "dec-plugin-sample-app-1-513-" + beforetext))
        println appsToDelete
    }

    void testMatcher() {
        def appName = "absis2-flow-dev-1-223-20181212"
        def findDate = (appName =~ /$GlobalVars.APP_STARTED_REGEX/)
        println findDate.size()
        println findDate[0]
    }

    void testSample() {

        def sample = "EDEN"

        println SampleAppCleanMode.valueOf(sample)
    }

    void testFilterWhiteList() {


        List<SyntheticTestStructure> syntheticTestList = new ArrayList<SyntheticTestStructure>()
        SyntheticTestStructure item1 = new SyntheticTestStructure()
        item1.appName = "app1"
        SyntheticTestStructure item2 = new SyntheticTestStructure()
        item2.appName = "app2"
        SyntheticTestStructure item3 = new SyntheticTestStructure()
        item3.appName = "demoarqcbk-micro-2-dev"

        syntheticTestList.add(item1)
        syntheticTestList.add(item2)
        syntheticTestList.add(item3)


        def String[] whiteListApps = new String[3]
        whiteListApps[0] = "arqrun"
        whiteListApps[1] = "demoarqcbk-micro"
        whiteListApps[2] = "decmicro"


        println "BEFORE FILTER LIST"
        for (SyntheticTestStructure item : syntheticTestList) {
            println item.appName
        }
        def bmxUtilities = new BmxUtilities()

        List<SyntheticTestStructure> filteredSyntheticTestList = bmxUtilities.filterWhiteList(syntheticTestList, whiteListApps)

        println "AFTER FILTER LIST"
        for (SyntheticTestStructure item : filteredSyntheticTestList) {
            println item.appName
        }

        assertEquals(1, filteredSyntheticTestList.size())
    }
}
