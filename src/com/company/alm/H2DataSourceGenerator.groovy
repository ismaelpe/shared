package com.project.alm


class H2DataSourceGenerator extends HikariDataSourceGeneratorV2 {

    H2DataSourceGenerator(def scriptContext){
        super(scriptContext)
    }

    def generate(String domain, String app, GarAppType appType, Map originalData) {
        return generate(domain, app, appType, originalData, [:])
    }

    def generate(String domain, String app, GarAppType appType, Map originalData, Map tenants) {

        TuplePair touple = super.generate(domain, app, appType, originalData, tenants)
        Map resultYaml = touple.getFirst()

        if ( ! resultYaml.spring ) {

            resultYaml.put("spring", [h2: [console: [settings: [:]]]])

        } else if ( ! resultYaml.spring.h2 ) {

            resultYaml.spring.put("h2", [console: [settings: [:]]])

        } else if ( ! resultYaml.spring.h2.console ) {

            resultYaml.spring.h2.put("console", [settings: [:]])

        } else if ( ! resultYaml.spring.h2.console.settings ) {

            resultYaml.spring.h2.console.put("settings", [:])

        }

        Map h2ConfigConsole = resultYaml.spring.h2.console
        h2ConfigConsole.put("enabled", true)
        h2ConfigConsole.put("path", "/h2-console")
        h2ConfigConsole.settings.put("web-allow-others", true)

        return touple
        
    }
	
	Map getConnectionsMap(Map originalData, String level1, String level2, String level3) {
		Map connectionsMap = new HashMap();
		try {
			connectionsMap = (Map) originalData.get(level1).get(level2).get(level3)
		} catch (Exception e) {
			
		}
		return connectionsMap
	}

    @Override
    Set<String> generateVcapsInfo(String domain, String app, GarAppType appType, Map originalData) {

        //get list of connections inside map, max 2 maps : datasource and readonly-datasource
        List<Map> connectionMaps = getConnectionsMaps(originalData)

        //for each connection insert vcaps
        for (Map connectionMap : connectionMaps) {

            for (entry in connectionMap) {

                Map valueMap = (Map) entry.value

                if (!valueMap.containsKey("url")){
                    valueMap.put("url", 'jdbc:h2:mem:testdb;MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false')
                }
                if (!valueMap.containsKey("username")){
                    valueMap.put("username", 'sa')
                }
                if (!valueMap.containsKey("password")){
                    valueMap.put("password", 'sa')
                }
                if (!valueMap.containsKey("data")){
                    valueMap.put("data", 'classpath:data.sql')
                }
            }
        }

        //This is used to create manifest. Does not apply here thus we send an empty one
        return []
    }

}

