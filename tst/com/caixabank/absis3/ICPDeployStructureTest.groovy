package com.caixabank.absis3

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotEquals
import static org.junit.Assert.assertNotNull

import com.caixabank.absis3.DeployStructure
import com.caixabank.absis3.ICPDeployStructure

import org.junit.Test

class ICPDeployStructureTest extends GroovyTestCase {

	@Test
	public void testInstantiaton() {
		ICPDeployStructure deployStructure=new ICPDeployStructure('cxb-ab3cor','cxb-ab3app',"tst")
		
		assert deployStructure.env == 'tst'
    }

}
