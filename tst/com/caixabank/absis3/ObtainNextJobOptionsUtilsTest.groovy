package com.caixabank.absis3

import static org.junit.Assert.*

import org.junit.Test

class ObtainNextJobOptionsUtilsTest extends GroovyTestCase {

    @Test
    public void test_recuperar_parametros_de_las_acciones_siguientes() {
        ResultPipelineData result = new MasterResultPipelineData("DSV", "git.url", "arqrun-micro", true);
        NextJobOptions options = ObtainNextJobOptionsUtils.obtainNextJobInformation("actionInTestingPipeline", result.getAcciones(true))
        assertNotNull(options.parameters);
        assertEquals(9, options.parameters.size())
        assertEquals("absis3/services/arch/alm/job-create-RC", options.nextJobName)
    }
}