package com.caixabank.absis3

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotEquals
import static org.junit.Assert.assertNotNull

import org.junit.Test

class UtilitiesTest extends GroovyTestCase {

    @Test
    public void testGetBooleanPropertyOrDefault() {

        assert Utilities.getBooleanPropertyOrDefault(null, false) == false
        assert Utilities.getBooleanPropertyOrDefault(null, true) == true
        assert Utilities.getBooleanPropertyOrDefault("false", true) == false
        assert Utilities.getBooleanPropertyOrDefault("true", false) == true

    }

	@Test
	public void testRegularExpression() {
		String componentName = 'DEMOCONNECT220180123E'
		
		//def matcher = componentName =~ /\S+[[:digit:]]{8}E\b/
		def matcher = componentName =~ /.{0,}\d{8}E\b/
		
		assert matcher.find()
		
		String componentName1 = 'APIGATEWAY1'
		
		def matcher1 = componentName1 =~ /.*\[[:digit:]]{8}E/
		
		assert  matcher1.find() == false
	}
	
	
    @Test
    public void testSplitStringToList() {
        String lines = 'absis-initalizr-1-251-20180926\n' + 'absis-initalizr-1-251-20180927\n' + 'absis-initalizr-1-251-20180928\n'

        String ignoreLine = 'absis-initalizr-1-251-20180927'
        print ignoreLine
        List linesR = Utilities.splitStringToList(lines, ignoreLine)

        print 'The values are ' + linesR
        assertEquals(linesR, ['absis-initalizr-1-251-20180926', 'absis-initalizr-1-251-20180928'])
    }

    @Test
    public void testSplitMappedRoutesToList() {
        String lines = 'sca-micro-server-1.tst.int.srv.caixabank.com, sca-micro-server-1.tst.ext.srv.caixabank.com, sca-micro-server-1.tst1.int.srv.caixabank.com'
        String ignoreLine;
        List linesR = Utilities.splitStringToListWithSplitter(lines, ',')

        print 'The values are ' + linesR
        assertEquals(linesR, ['sca-micro-server-1.tst.int.srv.caixabank.com', ' sca-micro-server-1.tst.ext.srv.caixabank.com', ' sca-micro-server-1.tst1.int.srv.caixabank.com'])
    }


    public void testIterateListCenters() {
        [GlobalVars.BMX_CD1, GlobalVars.BMX_CD2].each {
            print it
        }
    }

    @Test
    void testParseXml() {


        def xml = "<project><groupId>com.caixabank.absis</groupId><artifactId>demo-micro-app</artifactId><version>1.0.0-RELEASE</version><packaging>jar</packaging><name>demo-micro-app</name><description>Microservicio aplicativo</description><parent><groupId>com.caixabank.absis.arch</groupId><artifactId>absis-app-micro-parent</artifactId><version>1.0.0-SNAPSHOT</version><relativePath/> <!-- lookup parent from repository --></parent><properties><contract.package>com.caixabank.absis.demo</contract.package><skip-it>true</skip-it><skip-ut>false</skip-ut></properties></project>"

        def project = new XmlSlurper().parseText(xml)

        assertNotNull(project)
        assertNotNull(project.properties)

        assertEquals("com.caixabank.absis.demo", project.properties.children().find({ it.name() == "contract.package" }).text())
        assertEquals("true", project.properties.children().find({ it.name() == "skip-it" }).text())
        assertEquals("false", project.properties.children().find({ it.name() == "skip-ut" }).text())


    }

    @Test
    void testGetMajorMinorFix() {
        println "Value testGetMajorMinorFix"
        def version = '1.9.3'
        int i = 0
        version.tokenize('.').each { x ->

            println "testGetMajorMinorFix $x $i"
            i++
        }

        def majorMinorFix = Utilities.getMajorMinorFix('1.8.3')

        assertEquals("1", "1")
        assertEquals(1, majorMinorFix[0])
        assertEquals(8, majorMinorFix[1])
        assertEquals(3, majorMinorFix[2])
    }

    @Test
    void testIsLowerThan() {
        def value = Utilities.isLowerThan('1.8.3', '2.0.0')
        println 'isLowerthan $value'

        assertEquals(true, Utilities.isLowerThan('1.8.3', '2.0.0'))

        assertEquals(true, Utilities.isLowerThan('2.0.3', '2.1.0'))
        assertEquals(true, Utilities.isLowerThan('2.0.3', '2.0.5'))
        assertEquals(false, Utilities.isLowerThan('3.0.3', '2.1.0'))

        assertEquals(true, Utilities.isLowerThan('1.0.3', '2.1.0'))
    }
	
	@Test
	void testParseTenants() {
		String currentTenant = null
		String finalTenants = null
		List listOfTenants = Utilities.splitStringToListWithSplitter("01,0P",",")
		List definitiveTenants = new ArrayList<String>()
		listOfTenants.each {
			currentTenant = it
			definitiveTenants.add(currentTenant)
		}
		if(definitiveTenants.size() > 0) {
			finalTenants = Utilities.concatListWithSeparator(definitiveTenants, ",")
		}
		assertEquals("01,0P", finalTenants)
	}
}
