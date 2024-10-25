package com.caixabank.absis3

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotEquals
import static org.junit.Assert.assertNotNull

import com.caixabank.absis3.DeployStructure
import com.caixabank.absis3.ICPDeployStructure

import org.junit.Test

class SqlGeneratorTest extends GroovyTestCase {

	@Test
	public void testHashCodeEquals() {
		String origin = "CREATE TABLE A1( ID NUMBER, NAME VARCHAR2(10));"	
		String origin1 = "CREATE TABLE A1( ID NUMBER, NAME VARCHAR2(10));"
		def hashCodeOrigin = origin.hashCode()
		def hashCodeDestiny = origin1.hashCode()
		print "Origin ${origin} ${hashCodeOrigin}"
		print "Destiny ${origin1} ${hashCodeDestiny}"
		assert hashCodeOrigin == hashCodeDestiny 
    }
	
	
	@Test
	public void testHashCodeNotEquals() {
		String origin = "CREATE TABLE A1( ID NUMBER, NAME VARCHAR2(10));"
		String origin1 = "CREATE TABLE B1( ID NUMBER, NAME VARCHAR2(10));"
		def hashCodeOrigin = origin.hashCode()
		def hashCodeDestiny = origin1.hashCode()
		print "Origin ${origin} ${hashCodeOrigin}"
		print "Destiny ${origin1} ${hashCodeDestiny}"
		assert hashCodeOrigin != hashCodeDestiny
	}

}
