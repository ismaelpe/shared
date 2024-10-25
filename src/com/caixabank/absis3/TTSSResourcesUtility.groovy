package com.caixabank.absis3

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode;

public class TTSSResourcesUtility {


    /**
     * Permite recuperar un bloque de configuracion del yaml propio de sistemas
     * @param resourceType Identificador del bloque a recuperar
     * @param pathToSystemsYaml path hacia el yaml de sistemas
     * @return Map con la informacion
     */
    public Map getConfigResource(String resourceType, String pathToSystemsYaml) {
        File data = new File(pathToSystemsYaml)        

        def opts = new DumperOptions()
        opts.setDefaultFlowStyle(BLOCK)
        Yaml yaml = new Yaml(opts)

        Map propertiesFichero = (Map) yaml.load(data.newInputStream())

        //para ser llamado por DataSourceGenerator no pasamos el resourceType, leemos fichero tal como esta
        if (resourceType != null) {
            return propertiesFichero.get(resourceType)
        } else {
            return propertiesFichero
        }
    }

    public Map getConfigResource(String pathToSystemsYaml) {
        return getConfigResource(null, pathToSystemsYaml)
    }


    public static JsonNode getMapToJson(Map map) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.valueToTree(map);
        return jsonNode;
    }

    public static Map getJsonToMap(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = mapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {});
        return result;
    }

    public static printYamlFile(Map data) {

        def opts = new DumperOptions()
        opts.setDefaultFlowStyle(BLOCK)
        Yaml yaml = new Yaml(opts);

        String path = "./testResultFile.yml"

        FileWriter writer = new FileWriter(path);
        yaml.dump(data, writer);

        String fileContents = new File(path).getText('UTF-8')

    }


    public static void prettyPrintJson(JsonNode node) {
        ObjectMapper mapper = new ObjectMapper();        
    }

    public static final String V1 = "V1"
    public static final String V2 = "V2"
    public static final String H2 = "H2"


    public DataSourceGenerator getDataSourceGenerator(String version, def scriptContext = null) {
        switch (version) {
            case V1:
                return new HikariDataSourceGenerator()
                break
            case V2:
                return new HikariDataSourceGeneratorV2(scriptContext)
                break
            case H2:
                return new H2DataSourceGenerator(scriptContext)
                break
            default:
                return null;


        }

    }


}
