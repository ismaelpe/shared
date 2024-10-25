package com.caixabank.absis3

import com.caixabank.absis3.*
import static org.junit.Assert.*
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK

import org.junit.Test
import com.caixabank.absis3.TTSSResourcesUtility
import com.caixabank.absis3.DataSourceGenerator
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper

import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.Expression;

class DataSourceGeneratorV2Test extends GroovyTestCase {

    @Test
    void testGetConfigResource() {


        println "Init of the test"

        TTSSResourcesUtility ttssResourcesUtility = new TTSSResourcesUtility()


        Map configuracionDataSource = (Map) ttssResourcesUtility.getConfigResource('datasource', './tst/resources/definitionBackend.yml')

        println "The content of the yaml " + configuracionDataSource
        println "End of the test"

        //Escribe en salida

        //File dataOutput = new File('./target/output.yml')
        //yaml.dump  (configuracionDataSource, dataOutput.newWriter())


        println "OutputGenerated"

    }


    void testGetDataSourceGenerator() {


        println "Init of the test"

        TTSSResourcesUtility ttssResourcesUtility = new TTSSResourcesUtility()


        Map configuracionDataSource = (Map) ttssResourcesUtility.getConfigResource('datasource', './tst/resources/definitionBackend.yml')
		println "Init of the tes1t "+configuracionDataSource
		
        DataSourceGeneratorV2 generator = new DataSourceGeneratorV2("hikari")
		
        println "The content of the yaml " + generator.generate("demo", "myapp", configuracionDataSource)
        println "End of the test"

        //Escribe en salida

        //File dataOutput = new File('./target/output.yml')
        //yaml.dump  (configuracionDataSource, dataOutput.newWriter())


        println "OutputGenerated"

    }

    void testGetDataSourceGeneratorV2() {


        println "Init of the test"
        TTSSResourcesUtility ttssResourcesUtility = new TTSSResourcesUtility()

        //map from yaml
        Map configuracionDataSource = (Map) ttssResourcesUtility.getConfigResource('./tst/resources/definitionBackendV2.yml')
        println "Original YML FILE"
        TTSSResourcesUtility.printYamlFile(configuracionDataSource)

        DataSourceGeneratorV2 generator = new DataSourceGeneratorV2("hikari")

        Tuple2 touple = generator.generate("demo", "myapp", configuracionDataSource)


        println "Transformed YML FILE"
        Map result = touple.getFirst()
        TTSSResourcesUtility.printYamlFile(result)

        println "Vcaps services  Ids "+touple.getSecond().getClass()
        def vcapsServicesIds = touple.getSecond()
        println vcapsServicesIds

        println "End of the test"

    }
	
	void testGetDataSourceGeneratorV2ReadOnlyList() {


		println "Init of the test"
		TTSSResourcesUtility ttssResourcesUtility = new TTSSResourcesUtility()

		//map from yaml
		Map configuracionDataSource = (Map) ttssResourcesUtility.getConfigResource('./tst/resources/definitionBackendV2WithReadOnly.yml')		
	    println "Original YML FILE"
		TTSSResourcesUtility.printYamlFile(configuracionDataSource)

		DataSourceGeneratorV2 generator = new DataSourceGeneratorV2("hikari")

		Tuple2 touple = generator.generate("demo", "myapp", configuracionDataSource)


		println "Transformed YML FILE"
		Map result = touple.getFirst()
		TTSSResourcesUtility.printYamlFile(result)

		println "Vcaps services  Ids "+touple.getSecond().getClass()
		def vcapsServicesIds = touple.getSecond()
		println vcapsServicesIds

		println "End of the test"

	}


    void testYmlToJson() {

        println "Init of the test"
        TTSSResourcesUtility ttssResourcesUtility = new TTSSResourcesUtility()


        println "Original YML FILE"
        //map from yaml
        Map configuracionDataSource = (Map) ttssResourcesUtility.getConfigResource('./tst/resources/newDataSource.yml')
        //print
        TTSSResourcesUtility.printYamlFile(configuracionDataSource)

        println "Modified YML to JSON"
        //json from map
        JsonNode json = TTSSResourcesUtility.getMapToJson(configuracionDataSource)
        //print
        TTSSResourcesUtility.prettyPrintJson(json)


        println "End of the test"

    }


    void testSimpleJstlTransformation() {

        ObjectMapper mapper = new ObjectMapper();
        File from = new File('./tst/resources/input.json');
        JsonNode input = mapper.readTree(from);

        File transform = new File('./tst/resources/input.jslt')
        Expression jslt = Parser.compile(transform)
        JsonNode output = jslt.apply(input);
        String niceOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);

        println "transformation result:\n" + niceOutput

        from = new File('./tst/resources/output.json');
        JsonNode expectedOutput = mapper.readTree(from);
        String niceExpectedOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedOutput);

        assertToString(niceExpectedOutput, niceOutput)
    }


}
