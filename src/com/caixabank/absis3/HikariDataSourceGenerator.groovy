package com.caixabank.absis3

import com.caixabank.absis3.DataSourceGenerator


class HikariDataSourceGenerator extends DataSourceGenerator {

    private void generateInfo(Map resultYamlDataSourceProperties, Map originalData) {
        if (mapContainsTheValue(originalData, DataSourceGenerator.initialConnections)) resultYamlDataSourceProperties.put("minimumIdle", originalData.get(DataSourceGenerator.initialConnections))
        if (mapContainsTheValue(originalData, DataSourceGenerator.maxConnections)) resultYamlDataSourceProperties.put("maximumPoolSize", originalData.get(DataSourceGenerator.maxConnections))
        if (mapContainsTheValue(originalData, DataSourceGenerator.idleTimeout)) resultYamlDataSourceProperties.put("idleTimeout", originalData.get(DataSourceGenerator.idleTimeout))
        if (mapContainsTheValue(originalData, DataSourceGenerator.initializationFailTimeout)) resultYamlDataSourceProperties.put("initializationFailTimeout", originalData.get(DataSourceGenerator.initializationFailTimeout))
        if (mapContainsTheValue(originalData, DataSourceGenerator.maxWait)) resultYamlDataSourceProperties.put("connectionTimeout", originalData.get(DataSourceGenerator.maxWait))
        if (mapContainsTheValue(originalData, DataSourceGenerator.validationTimeout)) resultYamlDataSourceProperties.put("validationTimeout", originalData.get(DataSourceGenerator.frequencyTestIdle))
        if (mapContainsTheValue(originalData, DataSourceGenerator.inactiveConnectionTimeout)) resultYamlDataSourceProperties.put("maxLifetime", originalData.get(DataSourceGenerator.inactiveConnectionTimeout))
        if (mapContainsTheValue(originalData, DataSourceGenerator.validationQuery)) resultYamlDataSourceProperties.put("connectionTestQuery", originalData.get(DataSourceGenerator.validationQuery))
        if (mapContainsTheValue(originalData, DataSourceGenerator.statementCacheSize)) resultYamlDataSourceProperties.put("prepStmtCacheSize", originalData.get(DataSourceGenerator.statementCacheSize))
        if (mapContainsTheValue(originalData, DataSourceGenerator.autocommit)) resultYamlDataSourceProperties.put("autoCommit", originalData.get(DataSourceGenerator.autocommit))
        if (mapContainsTheValue(originalData, DataSourceGenerator.jdbcDriver)) resultYamlDataSourceProperties.put("driverClassName", originalData.get(DataSourceGenerator.jdbcDriver))
        if (mapContainsTheValue(originalData, DataSourceGenerator.poolName)) resultYamlDataSourceProperties.put("poolName", originalData.get(DataSourceGenerator.poolName))
        if (mapContainsTheValue(originalData, DataSourceGenerator.connectionInitSql)) resultYamlDataSourceProperties.put("connectionInitSql", originalData.get(DataSourceGenerator.connectionInitSql))
        if (mapContainsTheValue(originalData, DataSourceGenerator.schema)) resultYamlDataSourceProperties.put("schema", originalData.get(DataSourceGenerator.schema))
		if (mapContainsTheValue(originalData, DataSourceGenerator.connectionProperties)) resultYamlDataSourceProperties.put("dataSourceProperties", originalData.get(DataSourceGenerator.connectionProperties))
		if (mapContainsTheValue(originalData, DataSourceGenerator.url)) resultYamlDataSourceProperties.put("url", originalData.get(DataSourceGenerator.url))
		if (mapContainsTheValue(originalData, DataSourceGenerator.username)) resultYamlDataSourceProperties.put("username", originalData.get(DataSourceGenerator.username))
		if (mapContainsTheValue(originalData, DataSourceGenerator.password)) resultYamlDataSourceProperties.put("password", originalData.get(DataSourceGenerator.password))
		if (mapContainsTheValue(originalData, DataSourceGenerator.frequencyTestIdle)) resultYamlDataSourceProperties.put("keepAliveTime", originalData.get(DataSourceGenerator.validationInterval))
		if (mapContainsTheValue(originalData, DataSourceGenerator.activeConnectionTimeout)) resultYamlDataSourceProperties.put("leakDetectionTreshold", originalData.get(DataSourceGenerator.activeConnectionTimeout))
    }

    private void generateVcapsInfo(String domain, String app, GarAppType appType, Map resultYamlDataSourceProperties) {
        if (!resultYamlDataSourceProperties.containsKey("url")) resultYamlDataSourceProperties.put("url", '${vcap.services.' + getVcapName(domain, app, appType) +'.credentials.uri}')
        if (!resultYamlDataSourceProperties.containsKey("username")) resultYamlDataSourceProperties.put("username", '${vcap.services.' + getVcapName(domain, app, appType) + '.credentials.username}')
        if (!resultYamlDataSourceProperties.containsKey("password")) resultYamlDataSourceProperties.put("password", '${vcap.services.' + getVcapName(domain, app, appType) + '.credentials.password}')
    }
	
	private String getVcapName(String domain, String app, GarAppType appType) {
		if (appType == GarAppType.DATA_SERVICE) {
			return 'cbk-apps-' + domain.toLowerCase() + '-' + app.toLowerCase() + '-database';
		} else if (appType == GarAppType.ARCH_MICRO) {
			return 'arch-' + domain.toLowerCase() + '-' + app.toLowerCase() + '-database';
		} else {
			throw new Exception("App's type is not Dataservice or ArchMicro")
		}
	}

    private boolean mapContainsTheValue(Map originalData, String key) {
        if (originalData.containsKey(key) && originalData.get(key) != null && !''.equals(originalData.get(key)) && !'null'.equals(originalData.get(key))) return true
        return false
    }

	public def generate(String domain, String app, GarAppType appType, Map originalData, Map tenants) {
	   return generate(domain, app, appType, originalData)
	}
	
    public def generate(String domain, String app, GarAppType appType, Map originalData) {


        Map resultYaml = new HashMap();
        Map resultYamlDataSource = new HashMap();
        Map resultYamlDataSourceProperties = new HashMap();

        resultYaml.put("spring", resultYamlDataSource)
        resultYamlDataSource.put("datasource", resultYamlDataSourceProperties)

        generateVcapsInfo(domain, app, appType, resultYamlDataSourceProperties)
        generateInfo(resultYamlDataSourceProperties, originalData)

        return resultYaml
    }
}

