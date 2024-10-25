package com.caixabank.absis3

import org.junit.Test

class GsaUtilitiesTest extends GroovyTestCase {

	@Test
	public void testGetMostRecentVersionInProWithCandidate(){
		
		String versionsResponse = '[{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"SNAPSHOT","major":"1","minor":"6","fix":"0","build":"20190927.132622-1","entorno":"DEV","tipoInstalacion":"I","fecha":"2019-09-27 15:09:54","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"SNAPSHOT","major":"2","minor":"7","fix":"0","build":"20201201.113959-77","entorno":"DEV","tipoInstalacion":"I","fecha":"2020-12-01 12:12:21","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"RC","major":"1","minor":"4","fix":"0","build":"RC3","entorno":"TST","tipoInstalacion":"I","fecha":"2019-08-27 14:08:11","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"RC","major":"2","minor":"7","fix":"0","build":"RC4","entorno":"TST","tipoInstalacion":"I","fecha":"2020-12-06 21:12:55","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"RELEASE","major":"2","minor":"7","fix":"0","build":"2.7.0","entorno":"PRE","tipoInstalacion":"I","fecha":"2020-12-06 23:12:24","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"RELEASE","major":"2","minor":"7","fix":"0","build":"2.7.0","entorno":"PRO","tipoInstalacion":"I","fecha":"2020-12-09 23:12:45","application":"arqrun"}]';
		String majorVersion = "2"
		def resultado = GsaUtilities.getMostRecentVersionInEnvironment(majorVersion, versionsResponse, "pro")
		
		println resultado
		
		assertEquals(resultado, "2.7.0")
	}
	
	@Test
	public void testGetMostRecentVersionInPreWithCandidate(){
		
		String versionsResponse = '[{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"SNAPSHOT","major":"1","minor":"6","fix":"0","build":"20190927.132622-1","entorno":"DEV","tipoInstalacion":"I","fecha":"2019-09-27 15:09:54","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"SNAPSHOT","major":"2","minor":"7","fix":"0","build":"20201201.113959-77","entorno":"DEV","tipoInstalacion":"I","fecha":"2020-12-01 12:12:21","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"RC","major":"1","minor":"4","fix":"0","build":"RC3","entorno":"TST","tipoInstalacion":"I","fecha":"2019-08-27 14:08:11","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"RC","major":"2","minor":"7","fix":"0","build":"RC4","entorno":"TST","tipoInstalacion":"I","fecha":"2020-12-06 21:12:55","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"RELEASE","major":"2","minor":"7","fix":"0","build":"2.7.0","entorno":"PRE","tipoInstalacion":"I","fecha":"2020-12-06 23:12:24","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"RELEASE","major":"2","minor":"7","fix":"0","build":"2.7.0","entorno":"PRO","tipoInstalacion":"I","fecha":"2020-12-09 23:12:45","application":"arqrun"}]';
		String majorVersion = "2"
		def resultado = GsaUtilities.getMostRecentVersionInEnvironment(majorVersion, versionsResponse, "pre")
		
		println resultado
		
		assertEquals(resultado, "2.7.0")
	}
	
	@Test
	public void testGetMostRecentVersionInProWithNoCandidate(){
		
		String versionsResponse = '[{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"SNAPSHOT","major":"1","minor":"6","fix":"0","build":"20190927.132622-1","entorno":"DEV","tipoInstalacion":"I","fecha":"2019-09-27 15:09:54","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"SNAPSHOT","major":"2","minor":"7","fix":"0","build":"20201201.113959-77","entorno":"DEV","tipoInstalacion":"I","fecha":"2020-12-01 12:12:21","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"RC","major":"1","minor":"4","fix":"0","build":"RC3","entorno":"TST","tipoInstalacion":"I","fecha":"2019-08-27 14:08:11","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"RC","major":"2","minor":"7","fix":"0","build":"RC4","entorno":"TST","tipoInstalacion":"I","fecha":"2020-12-06 21:12:55","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"RELEASE","major":"2","minor":"7","fix":"0","build":"2.7.0","entorno":"PRE","tipoInstalacion":"I","fecha":"2020-12-06 23:12:24","application":"arqrun"},{"nombreComponente":"arqrun-micro","tipo":"SRV.DS","tipoVersion":"RELEASE","major":"2","minor":"7","fix":"0","build":"2.7.0","entorno":"PRO","tipoInstalacion":"I","fecha":"2020-12-09 23:12:45","application":"arqrun"}]';
		String majorVersion = "1"
		def resultado = GsaUtilities.getMostRecentVersionInEnvironment(majorVersion, versionsResponse,"pro")
		assertNull(resultado)
	}
	
	@Test
	public void testGetMostRecentVersionWhenReleaseCandidate(){
		
		String versionsResponse = '[{"nombreComponente":"demoarqalm-micro","tipo":"SRV.MS","tipoVersion":"SNAPSHOT","major":"1","minor":"121","fix":"0","build":"20201214.115229-2","entorno":"DEV","tipoInstalacion":"I","fecha":"2020-12-14 12:12:01","application":"demoarqalm"},{"nombreComponente":"demoarqalm-micro","tipo":"SRV.MS","tipoVersion":"SNAPSHOT","major":"2","minor":"0","fix":"0","build":"20201128.211809-1","entorno":"DEV","tipoInstalacion":"I","fecha":"2020-11-28 21:11:29","application":"demoarqalm"},{"nombreComponente":"demoarqalm-micro","tipo":"SRV.MS","tipoVersion":"RC","major":"1","minor":"121","fix":"0","build":"RC1","entorno":"TST","tipoInstalacion":"I","fecha":"2020-12-14 12:12:14","application":"demoarqalm"},{"nombreComponente":"demoarqalm-micro","tipo":"SRV.MS","tipoVersion":"RC","major":"2","minor":"1","fix":"0","build":"RC3","entorno":"TST","tipoInstalacion":"I","fecha":"2020-11-28 23:11:46","application":"demoarqalm"},{"nombreComponente":"demoarqalm-micro","tipo":"SRV.MS","tipoVersion":"RC","major":"1","minor":"121","fix":"0","build":"RC1","entorno":"PRE","tipoInstalacion":"I","fecha":"2020-12-14 14:12:14","application":"demoarqalm"},{"nombreComponente":"demoarqalm-micro","tipo":"SRV.MS","tipoVersion":"RELEASE","major":"2","minor":"1","fix":"0","build":"2.1.0","entorno":"PRE","tipoInstalacion":"I","fecha":"2020-11-28 23:11:33","application":"demoarqalm"},{"nombreComponente":"demoarqalm-micro","tipo":"SRV.MS","tipoVersion":"RELEASE","major":"1","minor":"118","fix":"0","build":"1.118.0","entorno":"PRO","tipoInstalacion":"I","fecha":"2020-12-09 06:12:23","application":"demoarqalm"},{"nombreComponente":"demoarqalm-micro","tipo":"SRV.MS","tipoVersion":"RELEASE","major":"2","minor":"1","fix":"0","build":"2.1.0","entorno":"PRO","tipoInstalacion":"I","fecha":"2020-11-28 23:11:12","application":"demoarqalm"}]';
		String majorVersion = "1"
		def resultado = GsaUtilities.getMostRecentVersionInEnvironment(majorVersion, versionsResponse,"pre")
		
		println resultado
		
		assertEquals(resultado, "1.121.0-RC1")
	}
	

    @Test
    public void testGetDependeciesNamesByType() {

        String dependeciesJson = '[{"application":"arqrun","tipo":"SRV.DS","componente":"arqrun-micro","version":"2.0.0"},{"application":"servicemanager","tipo":"ARQ.LIB","componente":"service-manager-spring-boot-starter","version":"1.11.0-SNAPSHOT"}]'

        def resultado = GsaUtilities.getDependeciesNamesByType(dependeciesJson, 'SRV.DS')

        println resultado

        assertEquals(resultado.size, 1)
    }


    @Test
    public void testGetDependeciesJsonFilePath() {

        PomXmlStructure pomXml = new PomXmlStructure()
        pomXml.artifactType = ArtifactType.AGREGADOR
        def resultado = GsaUtilities.getDependeciesJsonFilePath(pomXml)

        assertEquals(resultado, "target/classes/META-INF/maven/dependencies.json")
        println resultado


    }



}
