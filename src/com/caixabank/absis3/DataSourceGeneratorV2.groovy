package com.caixabank.absis3


import com.caixabank.absis3.*
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK
import java.util.HashMap
import java.util.ArrayList

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode;

import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.Expression;

public class DataSourceGeneratorV2 {

    private String postfix;

    public DataSourceGeneratorV2(String postfix) {
        this.postfix = postfix;
    }


    private Map getConnectionsMap(Map originalData, String level1, String level2) {
        Map connectionsMap = new HashMap();
        try {
            connectionsMap = ((Map) originalData.get(level1)).get(level2)
        } catch (Exception e) {
            
        }
        return connectionsMap
    }
	
	private Map getConnectionsMapFromList(Map originalData, String level1, String level2, String level3, String level4) {
		Map connectionsMap = new HashMap();
		try {			
			Map connectionsList = originalData.get(level1)			
			if (connectionsList!=null) {				
				if (connectionsList.get(level2)!=null) {
					Map connectionList2=connectionsList.get(level2)
					if (connectionList2.get(level3)!=null) {
						Map connectionList3=connectionList2.get(level3)
						connectionList3.each{
							entry-> Map mapaconnections=(Map)entry.get(level4)
									connectionsMap.putAll(mapaconnections)
						}					
					}
				}
			}
		} catch (Exception e) {
		    printOnConsole("No map found", EchoLevel.ERROR)
		}
		return connectionsMap
	}


    private List<Map> getConnectionsMaps(Map originalData) {
        List<Map> connectionNames = new ArrayList<Map>()

        Map connectionsMap = getConnectionsMap(originalData, "datasource", "connections")
        connectionNames.add(connectionsMap)


        Map connectionsReadOnlyMap = getConnectionsMap(originalData, "readonly-datasource", "connections")

        //verification for not repeat connection names
        for (entry in connectionsReadOnlyMap) {

            if (connectionsMap.get(entry.key) != null) {
                throw new Exception("already existing connection name, must be unique")
            }
        }

        connectionNames.add(connectionsReadOnlyMap)

		Map connectionsReadOnylListMap = getConnectionsMapFromList(originalData, "readonly-datasource-list", "datasources", "connections")
		
		//verification for not repeat connection names
		for (entry in connectionsReadOnylListMap) {

			if (connectionsMap.get(entry.key) != null) {
				throw new Exception("already existing connection name, must be unique")
			}
		}
		
		connectionNames.add(connectionsReadOnylListMap)
				
        return connectionNames
    }


    private Set<String> generateVcapsInfo(String domain, String app, Map originalData) {

        //get list of connections inside map, max 2 maps : datasource and readonly-datasource
        List<Map> connectionMaps = getConnectionsMaps(originalData)

        //vcaps services, one for each connection
        Set<String> vcapsServiceIds = []

        //for each connection insert vcaps
        for (Map connectionMap : connectionMaps) {

            for (entry in connectionMap) {

                String connectionName = (String) entry.key
                Map valueMap = (Map) entry.value

                valueMap.put("url", '${vcap.services.cbk-apps-' + domain.toLowerCase() + '-' + app.toLowerCase() + '-' + connectionName.toLowerCase() + '-database.credentials.uri:uri}')
                valueMap.put("username", '${vcap.services.cbk-apps-' + domain.toLowerCase() + '-' + app.toLowerCase() + '-' + connectionName.toLowerCase() + '-database.credentials.username:user}')
                valueMap.put("password", '${vcap.services.cbk-apps-' + domain.toLowerCase() + '-' + app.toLowerCase() + '-' + connectionName.toLowerCase() + '-database.credentials.password:secret}')

                vcapsServiceIds.add('cbk-apps-' + domain.toLowerCase() + '-' + app.toLowerCase() + '-' + connectionName.toLowerCase() + '-database')
            }
        }

        return vcapsServiceIds
    }

    private void generateJpaInfo(Map originalData) {

        List<Map> connectionMaps = new ArrayList<Map>()
        if (originalData.get("datasource") != null) {
            connectionMaps.add(originalData.get("datasource"))
        }
        if (originalData.get("readonly-datasource") != null) {
            connectionMaps.add(originalData.get("readonly-datasource"))
        }
		if (originalData.get("readonly-datasource-list") != null) {
			Map mapaConnexiones=(Map)originalData.get("readonly-datasource-list")
			if (mapaConnexiones!=null) {
				mapaConnexiones=(Map)mapaConnexiones.get("datasources")
				if (mapaConnexiones!=null) {
					mapaConnexiones.each {
						key,value-> connectionMaps.add(value)
					}
				}
			}
		}


        for (Map connectionMap : connectionMaps) {

            String jdbcDriver = findDriver(connectionMap.get("connections"))

            Tuple2 touple = resolveDatabaseAndDialect(jdbcDriver)

            //remove pre existing jpa info
            connectionMap.remove("jpa")

            //create new jpa info
            Map jpaMap = new HashMap()
            jpaMap.put("database", touple.getFirst())
            jpaMap.put("show-sql", "false")
            jpaMap.put("generate-ddl", "false")
            Map hibernateMap = new HashMap()
            hibernateMap.put("ddl-auto", "create-drop")
            hibernateMap.put("dialect", touple.getSecond())
            Map propertiesMap = new HashMap()
            propertiesMap.put("hibernate", hibernateMap)
            jpaMap.put("properties", propertiesMap)
            connectionMap.put("jpa", jpaMap)

        }

    }

    private Tuple2 resolveDatabaseAndDialect(String jdbcDriver) {

        switch (jdbcDriver) {
            case "com.ibm.db2.jcc.DB2Driver": return new Tuple2("DB2", "org.hibernate.dialect.DB2Dialect")
                break;
            case "org.apache.derby.jdbc.EmbeddedDriver": return new Tuple2("DERBY", "org.hibernate.dialect.DerbyDialect")
                break;
            case "org.h2.Driver": return new Tuple2("H2", "org.hibernate.dialect.H2Dialect")
                break;
            case "com.sap.db.jdbc.Driver": return new Tuple2("HANA", "org.hibernate.dialect.HANAColumnStoreDialect")
                break;
            case "org.hsqldb.jdbc.JDBCDriver": return new Tuple2("HSQL", "org.hibernate.dialect.HSQLDialect")
                break;
            case "com.informix.jdbc.IfxDriver": return new Tuple2("INFORMIX", "org.hibernate.dialect.Informix10Dialect")
                break;
            case "com.mysql.jdbc.Driver": return new Tuple2("MYSQL", "org.hibernate.dialect.MySQLDialect")
                break;
            case "oracle.jdbc.OracleDriver": return new Tuple2("ORACLE", "org.hibernate.dialect.Oracle12cDialect")
                break;
            case "org.postgresql.Driver": return new Tuple2("POSTGRESQL", "org.hibernate.dialect.PostgreSQLDialect")
                break;
            case "com.microsoft.sqlserver.jdbc.SQLServerDriver": return new Tuple2("SQL_SERVER", "org.hibernate.dialect.SQLServerDialect")
                break;
            default: throw new Exception("cannot resolve database and dialect")
        }

    }

    private String findDriver(Map connections) {
        String resultDriver = null
        for (conn in connections) {
            for (entry in conn.value) {
                if ("jdbcDriver".equals(entry.key)) {
                    resultDriver = entry.value
                }
            }
        }
        if (resultDriver == null) {
            throw new Exception("jdbcDriver property not found")
        }

        return resultDriver
    }


    public def generate(String domain, String app, Map originalData) {

        //insert vcaps in original map and return vcaps serviceids list
        Set<String> vcapsServiceIds = generateVcapsInfo(domain, app, originalData)


        //jpa new info
        generateJpaInfo(originalData)


        //map to json
        JsonNode input = TTSSResourcesUtility.getMapToJson(originalData)


        //transformation
        String templateFilePath = "./resources/" + "templateDataSource_" + postfix + ".jslt"
        File transform = new File(templateFilePath)
        Expression jslt = Parser.compile(transform)
        JsonNode output = jslt.apply(input);


        //json to map
        Map outputMap = TTSSResourcesUtility.getJsonToMap(output)

        Tuple2 touple = new Tuple2(outputMap, vcapsServiceIds)
        return touple

    }

}

