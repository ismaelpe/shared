package com.caixabank.absis3

import static org.junit.Assert.*
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK

import org.junit.Test

class GenerateYamlDataSourceTest extends GroovyTestCase {

    @Test
    void test() {
        println "Init of the test"
        File data = new File('./tst/resources/definitionBackend.yml')
        println " Exists the file " + data.exists()
        def opts = new DumperOptions()
        opts.setDefaultFlowStyle(BLOCK)
        Yaml yaml = new Yaml(opts)

        Map propertiesFichero = (Map) yaml.load(data.newInputStream())
        Map configuracionDataSource = (Map) propertiesFichero.get("datasource")

        println "The content of the yaml " + configuracionDataSource
        println "End of the test"

        //Escribe en salida

        File dataOutput = new File('./target/output.yml')
        yaml.dump(configuracionDataSource, dataOutput.newWriter())


        println "OutputGenerated"

    }

}
