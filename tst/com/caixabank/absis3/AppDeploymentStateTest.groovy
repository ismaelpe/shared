package com.caixabank.absis3

import org.junit.Test

class AppDeploymentStateTest extends GroovyTestCase {

    String consolidatedLogDump =
            "ancient_demoPipelineMicr-micro-1          stopped           0/1         768M     1G\n" +
                    "demoPipelineMicr-micro-1                  started           1/1        1024M     2G     demopipelinemicr-micro-1-beta.pro.ext.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro1.int.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.ext.srv.caixabank.com, demoPipelineMicr-micro-1.pro1.int.srv.caixabank.com\n" +
                    ""

    String canaryLogDump =
            "ancient_demoPipelineMicr-micro-1          started           1/1         768M     1G     demoPipelineMicr-micro-1.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.ext.srv.caixabank.com, demoPipelineMicr-micro-1.pro1.int.srv.caixabank.com\n" +
                    "demoPipelineMicr-micro-1                  started           1/1        1024M     2G     demopipelinemicr-micro-1-beta.pro.ext.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro1.int.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro.int.srv.caixabank.com\n" +
                    ""

    String incidentBothRunningLessThan6RoutesLogDump =
            "ancient_demoPipelineMicr-micro-1          started           1/1         768M     1G     demoPipelineMicr-micro-1.pro.ext.srv.caixabank.com, demoPipelineMicr-micro-1.pro1.int.srv.caixabank.com\n" +
                    "demoPipelineMicr-micro-1                  started           1/1        1024M     2G     demopipelinemicr-micro-1-beta.pro.ext.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro1.int.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro.int.srv.caixabank.com\n" +
                    ""

    String incidentOnlyAncientRunsLessThan6RoutesLogDump =
            "ancient_demoPipelineMicr-micro-1          started           1/1         768M     1G     demopipelinemicr-micro-1-beta.pro1.int.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.ext.srv.caixabank.com, demoPipelineMicr-micro-1.pro1.int.srv.caixabank.com\n" +
                    "demoPipelineMicr-micro-1                  stopped           0/1        1024M     2G\n" +
                    ""

    String incidentOnlyLiveRunsLessThan6RoutesLogDump =
            "ancient_demoPipelineMicr-micro-1          stopped           0/1         768M     1G\n" +
                    "demoPipelineMicr-micro-1                  started           1/1        1024M     2G     demopipelinemicr-micro-1-beta.pro1.int.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.ext.srv.caixabank.com, demoPipelineMicr-micro-1.pro1.int.srv.caixabank.com\n" +
                    ""

    String inconsistentRoutesLogDump =
            "ancient_demoPipelineMicr-micro-1          started           1/1         768M     1G     demopipelinemicr-micro-1-beta.pro.ext.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro1.int.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.ext.srv.caixabank.com, demoPipelineMicr-micro-1.pro1.int.srv.caixabank.com\n" +
                    "demoPipelineMicr-micro-1                  started           1/1        1024M     2G     demopipelinemicr-micro-1-beta.pro.ext.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro1.int.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.ext.srv.caixabank.com, demoPipelineMicr-micro-1.pro1.int.srv.caixabank.com\n" +
                    ""

    String thereIsNoAncientLogDump =
            "demoPipelineMicr-micro-1                  started           1/1        1024M     2G     demopipelinemicr-micro-1-beta.pro.ext.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro1.int.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.ext.srv.caixabank.com, demoPipelineMicr-micro-1.pro1.int.srv.caixabank.com\n" +
                    ""

    String thereIsNoLiveLogDump =
            "ancient_demoPipelineMicr-micro-1          started           1/1         768M     1G     demopipelinemicr-micro-1-beta.pro.ext.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro1.int.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.ext.srv.caixabank.com, demoPipelineMicr-micro-1.pro1.int.srv.caixabank.com\n" +
                    ""

    String thereIsNothingLogDump = ""

    @Test
    void testConsolidated() {
        MultipleDeploymentsState states = new MultipleDeploymentsState(GlobalVars.BMX_CD1, GlobalVars.BMX_TST_ORG_CD1, GlobalVars.BMX_DEV_SPACE, consolidatedLogDump)
        AppDeploymentState appState = states.getAppState("demoPipelineMicr-micro-1")
        assertEquals(AppDeploymentState.Current.CONSOLIDATED, appState.current)
    }

    @Test
    void testCanary() {
        MultipleDeploymentsState states = new MultipleDeploymentsState(GlobalVars.BMX_CD1, GlobalVars.BMX_TST_ORG_CD1, GlobalVars.BMX_DEV_SPACE, canaryLogDump)
        AppDeploymentState appState = states.getAppState("demoPipelineMicr-micro-1")
        assertEquals(AppDeploymentState.Current.CANARY, appState.current)
    }

    @Test
    void testIncidentBothRunningLessThan6Routes() {
        MultipleDeploymentsState states = new MultipleDeploymentsState(GlobalVars.BMX_CD1, GlobalVars.BMX_TST_ORG_CD1, GlobalVars.BMX_DEV_SPACE, incidentBothRunningLessThan6RoutesLogDump)
        AppDeploymentState appState = states.getAppState("demoPipelineMicr-micro-1")
        assertEquals(AppDeploymentState.Current.INCIDENT_LIKELY, appState.current)
    }

    @Test
    void testIncidentOnlyAncientRunsLessThan6Routes() {
        MultipleDeploymentsState states = new MultipleDeploymentsState(GlobalVars.BMX_CD1, GlobalVars.BMX_TST_ORG_CD1, GlobalVars.BMX_DEV_SPACE, incidentOnlyAncientRunsLessThan6RoutesLogDump)
        AppDeploymentState appState = states.getAppState("demoPipelineMicr-micro-1")
        assertEquals(AppDeploymentState.Current.INCIDENT_LIKELY, appState.current)
    }

    @Test
    void testIncidentOnlyLiveRunsLessThan6Routes() {
        MultipleDeploymentsState states = new MultipleDeploymentsState(GlobalVars.BMX_CD1, GlobalVars.BMX_TST_ORG_CD1, GlobalVars.BMX_DEV_SPACE, incidentOnlyLiveRunsLessThan6RoutesLogDump)
        AppDeploymentState appState = states.getAppState("demoPipelineMicr-micro-1")
        assertEquals(AppDeploymentState.Current.INCIDENT_LIKELY, appState.current)
    }

    @Test
    void testInconsistentRoutes() {
        MultipleDeploymentsState states = new MultipleDeploymentsState(GlobalVars.BMX_CD1, GlobalVars.BMX_TST_ORG_CD1, GlobalVars.BMX_DEV_SPACE, inconsistentRoutesLogDump)
        AppDeploymentState appState = states.getAppState("demoPipelineMicr-micro-1")
        assertEquals(AppDeploymentState.Current.INCONSISTENT_ROUTES, appState.current)
    }

    @Test
    void testThereIsNoAncient() {
        MultipleDeploymentsState states = new MultipleDeploymentsState(GlobalVars.BMX_CD1, GlobalVars.BMX_TST_ORG_CD1, GlobalVars.BMX_DEV_SPACE, thereIsNoAncientLogDump)
        AppDeploymentState appState = states.getAppState("demoPipelineMicr-micro-1")
        assertEquals(true, appState.isDeployed)
        assertEquals(true, appState.onlyLiveIsRunning)
        assertEquals(false, appState.onlyAncientIsRunning)
        assertEquals(false, appState.bothAreRunning)
    }

    @Test
    void testThereIsNoLive() {
        MultipleDeploymentsState states = new MultipleDeploymentsState(GlobalVars.BMX_CD1, GlobalVars.BMX_TST_ORG_CD1, GlobalVars.BMX_DEV_SPACE, thereIsNoLiveLogDump)
        AppDeploymentState appState = states.getAppState("demoPipelineMicr-micro-1")
        assertEquals(true, appState.isDeployed)
        assertEquals(false, appState.onlyLiveIsRunning)
        assertEquals(true, appState.onlyAncientIsRunning)
        assertEquals(false, appState.bothAreRunning)
    }

    @Test
    void testThereIsNothing() {
        MultipleDeploymentsState states = new MultipleDeploymentsState(GlobalVars.BMX_CD1, GlobalVars.BMX_TST_ORG_CD1, GlobalVars.BMX_DEV_SPACE, thereIsNothingLogDump)
        AppDeploymentState appState = states.getAppState("demoPipelineMicr-micro-1")
        assertEquals(false, appState.isDeployed)
        assertEquals(false, appState.onlyLiveIsRunning)
        assertEquals(false, appState.onlyAncientIsRunning)
        assertEquals(false, appState.bothAreRunning)
    }

}
