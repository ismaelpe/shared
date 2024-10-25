package com.caixabank.absis3

import org.junit.Test

class MultipleDeploymentsStateTest extends GroovyTestCase {

    String logDump =
            "ancient_demoPipelineMicr-micro-1          stopped           0/1         768M     1G\n" +
                    "demoPipelineMicr-micro-1                  started           1/1        1024M     2G     demopipelinemicr-micro-1-beta.pro.ext.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro1.int.srv.caixabank.com, demopipelinemicr-micro-1-beta.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.int.srv.caixabank.com, demoPipelineMicr-micro-1.pro.ext.srv.caixabank.com, demoPipelineMicr-micro-1.pro1.int.srv.caixabank.com\n" +
                    ""

    @Test
    void testInstanceCreation() {
        MultipleDeploymentsState states = new MultipleDeploymentsState(GlobalVars.BMX_CD1, GlobalVars.BMX_TST_ORG_CD1, GlobalVars.BMX_DEV_SPACE, logDump)
        assertEquals(GlobalVars.BMX_CD1, states.center)
        assertEquals(GlobalVars.BMX_TST_ORG_CD1, states.organization)
        assertEquals(GlobalVars.BMX_DEV_SPACE, states.space)
        assertEquals("ancient_demoPipelineMicr-micro-1", states.deployments.get(0).appName)
        assertEquals("stopped", states.deployments.get(0).state)
        assertEquals("0", states.deployments.get(0).instancesRunning)
        assertEquals("1", states.deployments.get(0).maxInstances)
        assertEquals("768M", states.deployments.get(0).assignedMemory)
        assertEquals("1G", states.deployments.get(0).filesystemSize)
        assertEquals([], states.deployments.get(0).routes)
        assertEquals(true, states.deployments.get(0).isAncient())
        assertEquals("demoPipelineMicr-micro-1", states.deployments.get(1).appName)
        assertEquals("started", states.deployments.get(1).state)
        assertEquals("1", states.deployments.get(1).instancesRunning)
        assertEquals("1", states.deployments.get(1).maxInstances)
        assertEquals("1024M", states.deployments.get(1).assignedMemory)
        assertEquals("2G", states.deployments.get(1).filesystemSize)
        assertEquals([
                "demopipelinemicr-micro-1-beta.pro.ext.srv.caixabank.com",
                "demopipelinemicr-micro-1-beta.pro1.int.srv.caixabank.com",
                "demopipelinemicr-micro-1-beta.pro.int.srv.caixabank.com",
                "demoPipelineMicr-micro-1.pro.int.srv.caixabank.com",
                "demoPipelineMicr-micro-1.pro.ext.srv.caixabank.com",
                "demoPipelineMicr-micro-1.pro1.int.srv.caixabank.com"
        ], states.deployments.get(1).routes)
        assertEquals(false, states.deployments.get(1).isAncient())
    }

}
