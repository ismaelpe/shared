package com.caixabank.absis3

import org.junit.Test

public class H2DataSourceGeneratorTest extends GroovyTestCase {

    TTSSResourcesUtility ttssResourcesUtility
    Map configuracionDataSource
    DataSourceGenerator generator


    void initTest(String pathToSystemsYaml = './tst/resources/definitionBackendH2.yml') {

        println "Init of the test"

        ttssResourcesUtility = new TTSSResourcesUtility()
        configuracionDataSource = (Map) ttssResourcesUtility.getConfigResource(pathToSystemsYaml)
        println "Original"
        TTSSResourcesUtility.printYamlFile(configuracionDataSource)

        generator = ttssResourcesUtility.getDataSourceGenerator(TTSSResourcesUtility.H2)

    }

    void printOutput(def resultConfiguracionDataSource) {

        println "The content of the yaml " + resultConfiguracionDataSource
        println "Resultado"
        TTSSResourcesUtility.printYamlFile(resultConfiguracionDataSource)

    }

    @Test
    void testGetHikariDataSourceGeneratorH2() {

        initTest()

        TuplePair touple = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource)
        Map resultConfiguracionDataSource = touple.getFirst()

        printOutput(resultConfiguracionDataSource)
    }

}
