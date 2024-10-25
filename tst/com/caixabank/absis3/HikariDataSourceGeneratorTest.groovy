package com.caixabank.absis3

import com.caixabank.absis3.*
import static org.junit.Assert.*
import org.yaml.snakeyaml.Yaml

import org.junit.Test
import com.caixabank.absis3.TTSSResourcesUtility
import com.caixabank.absis3.DataSourceGenerator
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper

import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.Expression;
import groovy.util.GroovyTestCase;

public class HikariDataSourceGeneratorTest extends GroovyTestCase {


    @Test
    void testGetHikariDataSourceGeneratorV1() {


        println "Init of the test"

        TTSSResourcesUtility ttssResourcesUtility = new TTSSResourcesUtility()


        Map configuracionDataSource = (Map) ttssResourcesUtility.getConfigResource('datasource', './tst/resources/definitionBackend.yml')

        println "Original"
        TTSSResourcesUtility.printYamlFile(configuracionDataSource)

        DataSourceGenerator generator = ttssResourcesUtility.getDataSourceGenerator(TTSSResourcesUtility.V1)

        Map resultConfiguracionDataSource = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource)
        println "The content of the yaml " + resultConfiguracionDataSource

        println "Resultado"
        TTSSResourcesUtility.printYamlFile(resultConfiguracionDataSource)


        assert (resultConfiguracionDataSource.get("spring").get("datasource").size() == 15)


        println "End of the test"

        println "OutputGenerated"
    }


}
