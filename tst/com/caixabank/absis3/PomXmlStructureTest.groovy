package com.caixabank.absis3

class PomXmlStructureTest extends GroovyTestCase {


    void testSantizeArtifactName() {
        PomXmlStructure obj = new PomXmlStructure()
        obj.applicationName = "cics-client"
        obj.artifactName = 'demo-micro'

        assertToString(obj.getApp(GarAppType.ARCH_LIBRARY), 'cicsclient')

        obj.applicationName = null
        obj.artifactName = 'demo-micro'

        assertToString(obj.getApp(GarAppType.MICRO_SERVICE), 'demo')

        obj.artifactName = 'absis-micro-common-starter'
        assertToString(obj.getApp(GarAppType.ARCH_PLUGIN), 'absismicrocommon')

        obj.artifactName = 'demo-pipeline-micro'
        assertToString(obj.getApp(GarAppType.ARCH_MICRO), 'demopipeline')

        obj.artifactName = 'arch-common-lib'
        assertToString(obj.getApp(GarAppType.ARCH_LIBRARY), 'archcommon')

    }


    void testInitStructureAgregadorStarter() {
        PomXmlStructure obj = new PomXmlStructure()
        def project = new XmlSlurper().parseText(new File("./tst/resources/agregadorStarter.xml").getText())
        println "Contingut " + (new File("./tst/resources/agregadorStarter.xml").getText())
        obj.initXmlStructure("AGREGADOR", "STARTER", project)
        println "El value es de " + obj.toString()
        assertToString(obj.artifactLib, '')
        assertToString(obj.artifactMicro, '')
        assertToString(obj.artifactSampleApp, 'absis-micro-common-sample-app')
    }

    void testIncRCSnapshot() {
        PomXmlStructure obj = new PomXmlStructure()
        def project = new XmlSlurper().parseText(new File("./tst/resources/agregadorStarter.xml").getText())
        println "Contingut " + (new File("./tst/resources/agregadorStarter.xml").getText())
        obj.initXmlStructure("AGREGADOR", "STARTER", project)
        println "El value es de " + obj.toString()
        try {
            obj.incRC()
            fail("ERROR")
        } catch (Exception e) {
            //Esto es un SNAPSHOT no es una RELEASE es correcto

        }
    }

    void testIncRCIncorrect() {
        PomXmlStructure obj = new PomXmlStructure()
        def project = new XmlSlurper().parseText(new File("./tst/resources/agregadorStarterRCIncorrect.xml").getText())

        obj.initXmlStructure("AGREGADOR", "STARTER", project)

        try {
            obj.incRC()
            println "El value es de testIncRCIncorrect " + obj.artifactVersion
            assertToString(obj.artifactVersion, '1.0.0-RC1')
        } catch (Exception e) {
            //Esto es un SNAPSHOT no es una RELEASE es correcto
            println "El error es de " + e.getMessage()
            e.printStackTrace()
            assert ('OK')
        }
    }

    void testIncRCCorrect() {
        PomXmlStructure obj = new PomXmlStructure()
        def project = new XmlSlurper().parseText(new File("./tst/resources/agregadorStarterRCCorrect.xml").getText())

        obj.initXmlStructure("AGREGADOR", "STARTER", project)


        obj.incRC()
        println "El value es de testIncRCCorrect " + obj.artifactVersion
        assertToString(obj.artifactVersion, '1.0.0-RC3')
    }

    void testIncMinor() {
        PomXmlStructure obj = new PomXmlStructure()
        def project = new XmlSlurper().parseText(new File("./tst/resources/pomApp.xml").getText())

        obj.initXmlStructure("AGREGADOR", "STARTER", project)


        obj.incMinor()
        println "El value es de testIncMinor " + obj.artifactVersion
        assertToString(obj.artifactVersion, '3.3.0-SNAPSHOT')
    }

    void testVersionWithoutQualifier() {
        PomXmlStructure obj = new PomXmlStructure()
        def project = new XmlSlurper().parseText(new File("./tst/resources/agregadorStarter.xml").getText())
        println "Contingut " + (new File("./tst/resources/agregadorStarter.xml").getText())
        obj.initXmlStructure("AGREGADOR", "STARTER", project)
        assertToString(obj.getArtifactVersionWithoutQualifier(), "1.8.3")
    }

    void testGetMajorMinorFix() {
        PomXmlStructure obj = new PomXmlStructure()
        def project = new XmlSlurper().parseText(new File("./tst/resources/agregadorStarter.xml").getText())
        println "Contingut " + (new File("./tst/resources/agregadorStarter.xml").getText())
        obj.initXmlStructure("AGREGADOR", "STARTER", project)
        println "testGetMajorMinorFix El value es de " + obj.toString()
        assertToString(obj.getArtifactMajorVersion(), "1")
        assertToString(obj.getArtifactMinorVersion(), "8")
        assertToString(obj.getArtifactFixVersion(), "3")

    }

    /*
    void testInitStructureGetRouteToManifest() {
        PomXmlStructure obj=new PomXmlStructure()	
        def project = new XmlSlurper().parseText(new File("./tst/resources/agregadorStarter.xml").getText())
        println "Contingut " + (new File("./tst/resources/agregadorStarter.xml").getText())
        obj.initXmlStructure("AGREGADOR","STARTER","./tst/resources",project)
        println "La ruta del jar es de " + obj.getRouteToArtifactJar()
        println "La ruta del manifest es de " + obj.getRouteToManifest()
        
        assertToString(obj.artifactLib,'')
        assertToString(obj.artifactMicro,'')
        assertToString(obj.artifactSampleApp,'absis-micro-common-sample-app')
    }*/

    void testVersionQualifier() {
        PomXmlStructure obj = new PomXmlStructure()
        def project = new XmlSlurper().parseText(new File("./tst/resources/agregadorStarter.xml").getText())
        println "Contingut " + (new File("./tst/resources/agregadorStarter.xml").getText())
        obj.initXmlStructure("AGREGADOR", "STARTER", project)
        assertToString(obj.getArtifactVersionQualifier(), "SNAPSHOT")
    }

}
