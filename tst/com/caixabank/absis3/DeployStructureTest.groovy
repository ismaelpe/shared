package com.caixabank.absis3

import java.time.LocalDate
import java.time.ZoneId

class DeployStructureTest extends GroovyTestCase {


    void testGetVipaPerCenterEqualsCenter1() {

        BmxStructure bmxStruct = new PreBmxStructure();

        DeployStructure dStruct = bmxStruct.getDeployStructure(GlobalVars.BMX_CD1)

        assertEquals(dStruct.getVipaPerCenter(), "pre1.int.srv.caixabank.com")
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
}
