package com.caixabank.absis3


class HikariDataSourceGeneratorV2 extends DataSourceGenerator {

	public final def scriptContext
	
	HikariDataSourceGeneratorV2(def scriptContext){
		this.scriptContext = scriptContext;
	}

    def generateConnection(Map originalData) {

        Map resultYamlDataSourceProperties = new HashMap()

        if (mapContainsTheValue(originalData, DataSourceGenerator.initialConnections)) resultYamlDataSourceProperties.put("minimumIdle", originalData.get(DataSourceGenerator.initialConnections))
        if (mapContainsTheValue(originalData, DataSourceGenerator.maxConnections)) resultYamlDataSourceProperties.put("maximumPoolSize", originalData.get(DataSourceGenerator.maxConnections))
        if (mapContainsTheValue(originalData, DataSourceGenerator.idleTimeout)) resultYamlDataSourceProperties.put("idleTimeout", originalData.get(DataSourceGenerator.idleTimeout))
        if (mapContainsTheValue(originalData, DataSourceGenerator.initializationFailTimeout)) resultYamlDataSourceProperties.put("initializationFailTimeout", originalData.get(DataSourceGenerator.initializationFailTimeout))
        if (mapContainsTheValue(originalData, DataSourceGenerator.maxWait)) resultYamlDataSourceProperties.put("connectionTimeout", originalData.get(DataSourceGenerator.maxWait))
        if (mapContainsTheValue(originalData, DataSourceGenerator.validationTimeout)) resultYamlDataSourceProperties.put("validationTimeout", originalData.get(DataSourceGenerator.validationTimeout))
        if (mapContainsTheValue(originalData, DataSourceGenerator.inactiveConnectionTimeout)) resultYamlDataSourceProperties.put("maxLifetime", originalData.get(DataSourceGenerator.inactiveConnectionTimeout))
        if (mapContainsTheValue(originalData, DataSourceGenerator.validationQuery)) resultYamlDataSourceProperties.put("connectionTestQuery", originalData.get(DataSourceGenerator.validationQuery))
        if (mapContainsTheValue(originalData, DataSourceGenerator.statementCacheSize)) resultYamlDataSourceProperties.put("prepStmtCacheSize", originalData.get(DataSourceGenerator.statementCacheSize))
        if (mapContainsTheValue(originalData, DataSourceGenerator.autocommit)) resultYamlDataSourceProperties.put("autoCommit", originalData.get(DataSourceGenerator.autocommit))
        if (mapContainsTheValue(originalData, DataSourceGenerator.jdbcDriver)) resultYamlDataSourceProperties.put("driverClassName", originalData.get(DataSourceGenerator.jdbcDriver))
        if (mapContainsTheValue(originalData, DataSourceGenerator.poolName)) resultYamlDataSourceProperties.put("poolName", originalData.get(DataSourceGenerator.poolName))
        if (mapContainsTheValue(originalData, DataSourceGenerator.connectionInitSql)) resultYamlDataSourceProperties.put("connectionInitSql", originalData.get(DataSourceGenerator.connectionInitSql))
        if (mapContainsTheValue(originalData, DataSourceGenerator.schema)) resultYamlDataSourceProperties.put("schema", originalData.get(DataSourceGenerator.schema))
        if (mapContainsTheValue(originalData, DataSourceGenerator.connectionProperties)) resultYamlDataSourceProperties.put("dataSourceProperties", originalData.get(DataSourceGenerator.connectionProperties))
		if (mapContainsTheValue(originalData, DataSourceGenerator.frequencyTestIdle)) resultYamlDataSourceProperties.put("keepAliveTime", originalData.get(DataSourceGenerator.frequencyTestIdle))
		if (mapContainsTheValue(originalData, DataSourceGenerator.activeConnectionTimeout)) resultYamlDataSourceProperties.put("leakDetectionTreshold", originalData.get(DataSourceGenerator.activeConnectionTimeout))

        return resultYamlDataSourceProperties
    }

    public boolean mapContainsTheValue(Map originalData, String key) {
        if (originalData.containsKey(key) && originalData.get(key) != null && !''.equals(originalData.get(key)) && !'null'.equals(originalData.get(key))) return true
        return false
    }

	def generateTargetConnections(Map datasourcesConns, Map authorizedTenants, boolean hasCustomTenants) {
		Map connectionsDestino = new HashMap()

		if (datasourcesConns != null && datasourcesConns.size() > 0) {

			for (datasourceConn in datasourcesConns) {
				//New model can use tenantList option
				def tenantList = datasourceConn.value.get("tenantList")
                // If no tenant is present we assume CBK
                def currentTenant = datasourceConn.value.containsKey("tenant") ? datasourceConn.value.get("tenant") : '01'

				if(tenantList) {

                    def definitiveTenants = generateTargetConnectionsWithTenantList(tenantList, authorizedTenants, hasCustomTenants)
                    if(definitiveTenants.size() > 0) {
                        addTenantListInTargetConnections(Utilities.concatListWithSeparator(definitiveTenants, ","), connectionsDestino, datasourceConn);
                    }
					
				} else {

                    def tenantToAdd = generateTargetConnectionsWithSingleTenant(currentTenant, authorizedTenants, hasCustomTenants)
                    if (tenantToAdd) addTenantInTargetConnections(tenantToAdd, connectionsDestino, datasourceConn);

				}
			}
		}
        return connectionsDestino
	}

    def generateTargetConnectionsWithTenantList(def tenantList, Map authorizedTenants, boolean hasCustomTenants) {

        printOnConsole("There is tenantList defined: $tenantList", EchoLevel.DEBUG)
        List listOfTenants = Utilities.splitStringToListWithSplitter(tenantList,",")
        List definitiveTenants = new ArrayList<String>()

        listOfTenants.each {

            def currentTenant = it
            printOnConsole("Parsing tenant due to list $currentTenant", EchoLevel.DEBUG)
			printOnConsole("Parsing tenant due to list $authorizedTenants customTags $hasCustomTenants", EchoLevel.DEBUG)
            if(!hasCustomTenants && authorizedTenants) {
                if (!(!currentTenant.startsWith("IMS") && authorizedTenants.get(currentTenant)==null )) {

                    printOnConsole("Adding tenant $currentTenant", EchoLevel.DEBUG)
                    definitiveTenants.add(currentTenant)

                }
            }else {

                printOnConsole("Adding tenant $currentTenant", EchoLevel.DEBUG)
                definitiveTenants.add(currentTenant)

            }
        }

        printOnConsole("Definitive tenants: $definitiveTenants", EchoLevel.DEBUG)

        return definitiveTenants
    }

    def generateTargetConnectionsWithSingleTenant(def currentTenant, Map authorizedTenants, boolean hasCustomTenants) {

        if (authorizedTenants && currentTenant!=null && currentTenant != GlobalVars.TENANT_WILDCARD && !hasCustomTenants) {
            if (!currentTenant.startsWith("IMS") && authorizedTenants.get(currentTenant)==null ) {
                return null
            }
        }

        printOnConsole("There is a tenant defined: $currentTenant", EchoLevel.DEBUG)

        return currentTenant
    }

	void addTenantListInTargetConnections(String currentTenantList, Map connectionsDestino, def datasourceConn) {

        Map datasourceConnValue = new HashMap()
        datasourceConnValue.put("hikari", generateConnection(datasourceConn.value))
        datasourceConnValue.put("tenant-list", currentTenantList)
        datasourceConnValue.put("driver-class-name", datasourceConn.value.get("jdbcDriver"))

        if(datasourceConn.value.get("dialect") != null) {
            datasourceConnValue.put("dialect", datasourceConn.value.get("dialect"))
        }

        if(mapContainsTheValue(datasourceConn.value,DataSourceGenerator.url)) datasourceConnValue.put("url", datasourceConn.value.get(DataSourceGenerator.url))
        if(mapContainsTheValue(datasourceConn.value,DataSourceGenerator.username)) datasourceConnValue.put("username", datasourceConn.value.get(DataSourceGenerator.username))
        if(mapContainsTheValue(datasourceConn.value,DataSourceGenerator.password)) datasourceConnValue.put("password", datasourceConn.value.get(DataSourceGenerator.password))
        if(mapContainsTheValue(datasourceConn.value,DataSourceGenerator.optional)) datasourceConnValue.put("optional", datasourceConn.value.get(DataSourceGenerator.optional))

        connectionsDestino.put(datasourceConn.key, datasourceConnValue)

	}
	
	void addTenantInTargetConnections(String currentTenant, Map connectionsDestino, def datasourceConn) {

        Map datasourceConnValue = new HashMap()
        datasourceConnValue.put("hikari", generateConnection(datasourceConn.value))

        if(currentTenant != null) {
            datasourceConnValue.put("tenant", currentTenant)
        }

        datasourceConnValue.put("driver-class-name", datasourceConn.value.get("jdbcDriver"))

        if(datasourceConn.value.get("dialect") != null) {
            datasourceConnValue.put("dialect", datasourceConn.value.get("dialect"))
        }

        if(mapContainsTheValue(datasourceConn.value,DataSourceGenerator.url)) datasourceConnValue.put("url", datasourceConn.value.get(DataSourceGenerator.url))
        if(mapContainsTheValue(datasourceConn.value,DataSourceGenerator.username)) datasourceConnValue.put("username", datasourceConn.value.get(DataSourceGenerator.username))
        if(mapContainsTheValue(datasourceConn.value,DataSourceGenerator.password)) datasourceConnValue.put("password", datasourceConn.value.get(DataSourceGenerator.password))
        if(mapContainsTheValue(datasourceConn.value,DataSourceGenerator.optional)) datasourceConnValue.put("optional", datasourceConn.value.get(DataSourceGenerator.optional))

        connectionsDestino.put(datasourceConn.key, datasourceConnValue)

	}

    Map getConnectionsMap(Map originalData, String level1, String level2, String level3) {
        Map connectionsMap = new HashMap();
        try {
            connectionsMap = (Map) originalData.get(level1).get(level2).get(level3)
        } catch (Exception e) {
            
        }
        return connectionsMap
    }
	

	protected Map getConnectionsMapFromList(Map originalData, String level1, String level2, String level3, String level4) {
		Map connectionsMap = new HashMap();
		try {			
			Map connectionsList = originalData.get(level1)			
			if (connectionsList!=null) {				
				if (connectionsList.get(level2)!=null) {
					Map connectionsList1=connectionsList.get(level2)
					if (connectionsList1.get(level3)!=null) {
						Map connectionsList2=connectionsList1.get(level3)
						connectionsList2.each{
							entry-> Map mapaconnections=(Map)entry.value.get(level4)
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
	
    List<Map> getConnectionsMaps(Map originalData) {
        List<Map> connectionNames = new ArrayList<Map>()

        Map connectionsMap = getConnectionsMap(originalData, "absis", "datasource", "connections")
        connectionNames.add(connectionsMap)

        Map connectionsMapReplica = getConnectionsMap(originalData, "absis", "datasource", "readonly-replica-connections")
        connectionNames.add(connectionsMapReplica)


        Map connectionsReadOnlyMap = getConnectionsMap(originalData, "absis", "readonly-datasource", "connections")

        //verification for not repeat connection names
        for (entry in connectionsReadOnlyMap) {

            if (connectionsMap.get(entry.key) != null) {
                throw new Exception("already existing connection name, must be unique")
            }
        }

        connectionNames.add(connectionsReadOnlyMap)
		
		Map connectionsReadOnlyListMap = getConnectionsMapFromList(originalData, "absis", "readonly-datasource-list","datasources","connections")
		
		//verification for not repeat connection names
		for (entry in connectionsReadOnlyListMap) {

			if (connectionsMap.get(entry.key) != null) {
				throw new Exception("already existing connection name, must be unique")
			}
		}

		connectionNames.add(connectionsReadOnlyListMap)

        return connectionNames
    }

    Set<String> generateVcapsInfo(String domain, String app, GarAppType appType, Map originalData) {
        //get list of connections inside map, max 2 maps : datasource and readonly-datasource and readonly-datasource-list 
        List<Map> connectionMaps = getConnectionsMaps(originalData)
        //vcaps services, one for each connection
        Set<String> vcapsServiceIds = []

        //for each connection insert vcaps
        for (Map connectionMap : connectionMaps) {

            for (entry in connectionMap) {

				String connectionName = (String) entry.key
				Map valueMap = (Map) entry.value

				boolean needsSecretsGeneration = false;
				String newSecretName = getVcapName(domain, app, appType, connectionName)
				if (!valueMap.containsKey("url")){
					valueMap.put("url", '\${vcap.services.' + newSecretName  +'.credentials.uri}')
					if (appType == GarAppType.DATA_SERVICE) vcapsServiceIds.add(newSecretName)
				} else {
					String urlValue = valueMap.get("url");
					if (urlValue.startsWith('\${vcap.services.') && urlValue.endsWith('.credentials.uri}')) {
						String secretName = urlValue.substring('\${vcap.services.'.length(), urlValue.length() - '.credentials.uri}'.length())
						vcapsServiceIds.add(secretName)
					}
				}
				if (!valueMap.containsKey("username")){
					valueMap.put("username", '\${vcap.services.' + newSecretName  +'.credentials.username}')
					if (appType == GarAppType.DATA_SERVICE) vcapsServiceIds.add(newSecretName)
				} else {
					String usernameValue = valueMap.get("username");
					if (usernameValue.startsWith('\${vcap.services.') && usernameValue.endsWith('.credentials.username}')) {
						String secretName = usernameValue.substring('\${vcap.services.'.length(), usernameValue.length() - '.credentials.username}'.length())
						vcapsServiceIds.add(secretName)
					}
				}
				if (!valueMap.containsKey("password")){
					valueMap.put("password", '\${vcap.services.' + newSecretName  +'.credentials.password}')
					if (appType == GarAppType.DATA_SERVICE) vcapsServiceIds.add(newSecretName)
				} else {
					String passwordValue = valueMap.get("password");
					if (passwordValue.startsWith('\${vcap.services.') && passwordValue.endsWith('.credentials.password}')) {
						String secretName = passwordValue.substring('\${vcap.services.'.length(), passwordValue.length() - '.credentials.password}'.length())
						vcapsServiceIds.add(secretName)
					}
				}
            }
        }

        return vcapsServiceIds
    }
	
	public String getVcapName(String domain, String app, GarAppType appType, String connectionName) {
		if (appType == GarAppType.DATA_SERVICE) {
			return 'cbk-apps-' + domain.toLowerCase() + '-' + app.toLowerCase() + '-' + connectionName.toLowerCase() +'-database';
		} else if (appType == GarAppType.ARCH_MICRO) {
			return 'arch-' + domain.toLowerCase() + '-' + app.toLowerCase() + '-' + connectionName.toLowerCase() + '-database';
		} else {
			throw new Exception("App's type is not Dataservice or ArchMicro")
		}
	}

    public TuplePair resolveDatabaseAndDialect(String jdbcDriver,String dialect) {

		TuplePair result=null
		
		if (jdbcDriver == null) {
			throw new Exception("jdbcDriver property cannot be null")
		}
		
        switch (jdbcDriver) {
            case "com.ibm.db2.jcc.DB2Driver": result= new TuplePair("DB2", "org.hibernate.dialect.DB2Dialect")
                break;
			case "com.ibm.as400.access.AS400JDBCDriver": result= new TuplePair("DB2", "org.hibernate.dialect.DB2400Dialect")
				break;
            case "org.apache.derby.jdbc.EmbeddedDriver": result= new TuplePair("DERBY", "org.hibernate.dialect.DerbyDialect")
                break;
            case "org.h2.Driver": result= new TuplePair("H2", "org.hibernate.dialect.H2Dialect")
                break;
            case "com.sap.db.jdbc.Driver": result= new TuplePair("HANA", "org.hibernate.dialect.HANAColumnStoreDialect")
                break;
            case "org.hsqldb.jdbc.JDBCDriver": result= new TuplePair("HSQL", "org.hibernate.dialect.HSQLDialect")
                break;
            case "com.informix.jdbc.IfxDriver": result= new TuplePair("INFORMIX", "org.hibernate.dialect.Informix10Dialect")
                break;
            case "com.mysql.jdbc.Driver": result= new TuplePair("MYSQL", "org.hibernate.dialect.MySQLDialect")
                break;
            case "oracle.jdbc.OracleDriver": result= new TuplePair("ORACLE", "org.hibernate.dialect.Oracle12cDialect")
                break;
            case "org.postgresql.Driver": result= new TuplePair("POSTGRESQL", "org.hibernate.dialect.PostgreSQLDialect")
                break;
            case "com.microsoft.sqlserver.jdbc.SQLServerDriver": result= new TuplePair("SQL_SERVER", "org.hibernate.dialect.SQLServerDialect")
                break;
            default: throw new Exception("cannot resolve database")
        }
		
		if (dialect!=null) {
			result.setSecond(dialect)
		}
		
		return result;

    }

    public String findProperty(String propertyName,Map connections) {

		//we find one value, but we iterate over connections to find the first one
        for (conn in connections) {
            for (entry in conn.value) {
                if (propertyName.equals(entry.key)) {
					return entry.value
                }
            }
        }
		
		//if not found return null
        return null
    }
	
	void generateJpaInfoFromConnectionsListMap(List<Map> connectionMaps ) {
		//connectionMaps.size() = 2 only read-write and read-only
		for (Map connectionMap : connectionMaps) {

			String jdbcDriver = findProperty("driver-class-name",connectionMap.get("connections"))
			String dialect = findProperty("dialect",connectionMap.get("connections"))

			TuplePair touple = resolveDatabaseAndDialect(jdbcDriver,dialect)

			//remove pre existing jpa info
			connectionMap.remove("jpa")

			//create new jpa info
			Map jpaMap = new HashMap()
			jpaMap.put("database", touple.getFirst())

			if ("H2" == touple.getFirst()) {
				jpaMap.put("show-sql", "true")
				jpaMap.put("generate-ddl", "true")
			} else {
				jpaMap.put("show-sql", "false")
				jpaMap.put("generate-ddl", "false")
			}

			Map hibernateMap = new HashMap()

			if ("H2" == touple.getFirst()) {
				hibernateMap.put("ddl-auto", "create-drop")
			}
			if(touple.getSecond() != null) {
				hibernateMap.put("dialect", touple.getSecond())
			}
			Map propertiesMap = new HashMap()
			propertiesMap.put("hibernate", hibernateMap)
			jpaMap.put("properties", propertiesMap)
			connectionMap.put("jpa", jpaMap)

		}
	}

    void generateJpaInfo(Map originalData) {
        List<Map> connectionMaps = new ArrayList<Map>()
        if (originalData.get("absis").get("datasource") != null) {
            connectionMaps.add(originalData.get("absis").get("datasource"))
        }
        if (originalData.get("absis").get("readonly-datasource") != null) {
            connectionMaps.add(originalData.get("absis").get("readonly-datasource"))
        }
		
	   generateJpaInfoFromConnectionsListMap(connectionMaps)

    }
	
	public def generate(String domain, String app, GarAppType appType, Map originalData) {
		return generate(domain, app, appType, originalData, [:])
	}

	public def generate(String domain, String app, GarAppType appType, Map originalData, Map tenants) {
		Map resultYaml = new HashMap();
		resultYaml.put("absis", new HashMap(originalData))

		//for read-write datasources
		try {
			if(originalData.containsKey("datasource")) {
				Map datasourcesConns = originalData.get("datasource").get("connections")
				Map datasourceReplica = originalData.get("datasource").get("readonly-replica-connections")
				Map datasourcesDestino = resultYaml.get("absis").get("datasource")
				boolean hasCustomTenants = originalData.get('datasource').containsKey('customTenants') ? originalData.get('datasource').get('customTenants') : false
                datasourcesDestino.put("connections", generateTargetConnections(datasourcesConns, tenants, hasCustomTenants))
				if (datasourceReplica!=null){
					datasourcesDestino.put("readonly-replica-connections", generateTargetConnections(datasourceReplica,tenants, hasCustomTenants))
				}
			}
		} catch (Exception e) {
			
		}
		//for read-only datasources
		try {
			if(originalData.containsKey("readonly-datasource")) {
				Map datasourcesConns = originalData.get("readonly-datasource").get("connections")
				Map datasourcesDestino = resultYaml.get("absis").get("readonly-datasource")
				boolean hasCustomTenants = originalData.get('readonly-datasource').containsKey('customTenants') ? originalData.get('readonly-datasource').get('customTenants') : false
                datasourcesDestino.put("connections", generateTargetConnections(datasourcesConns, tenants, hasCustomTenants))
			}
		} catch (Exception e) {
			
		}
		
		//for read-only-list datasources
		try {
			if(originalData.containsKey("readonly-datasource-list")) {
				Map datasourcesConnsList = originalData.get("readonly-datasource-list")	
				if (datasourcesConnsList) {
					datasourcesConnsList=(Map)datasourcesConnsList.get("datasources")
				}
				if (datasourcesConnsList!=null) {	
					Map listadoConexionesValues=[:]
									
					for (String nombreConnexion in datasourcesConnsList.keySet()) {
	
						Map dataSourceReadOnly=datasourcesConnsList.get(nombreConnexion)
						Map datasourcesConns = dataSourceReadOnly.get("connections")
	
						Map datasourcesDestino = resultYaml.get("absis").get("readonly-datasource-list").get("datasources").get(nombreConnexion)						
						//resultYaml.get("absis").get("readonly-datasource-list").get("datasources").remove(nombreConnexion)
						
						boolean hasCustomTenants = originalData.get("readonly-datasource-list").get("datasources").get(nombreConnexion).containsKey('customTenants') ? originalData.get("readonly-datasource-list").get("datasources").get(nombreConnexion).get('customTenants') : false
						printOnConsole("Generating target connections for datasource $nombreConnexion", EchoLevel.DEBUG)
						datasourcesDestino.put("connections", generateTargetConnections(datasourcesConns, tenants, hasCustomTenants))
						List<Map> connectionMaps = new ArrayList<Map>()
						connectionMaps.add(dataSourceReadOnly)
						generateJpaInfoFromConnectionsListMap(connectionMaps )
						listadoConexionesValues.put(nombreConnexion, datasourcesDestino)				
					}
					resultYaml.get("absis").get("readonly-datasource-list").putAt("datasources", listadoConexionesValues)
				}
			}
		} catch (Exception e) {
		    printOnConsole(e.getMessage(), EchoLevel.ERROR)
		}

		//insert vcaps in original map and return vcaps serviceids list
		Set<String> vcapsServiceIds = generateVcapsInfo(domain, app, appType, resultYaml)

		//jpa new info
		generateJpaInfo(resultYaml)

		TuplePair touple = new TuplePair(resultYaml, vcapsServiceIds)

		return touple
	}

    public void printOnConsole(def message, EchoLevel echoLevel = EchoLevel.ALL) {

        if (scriptContext) {

            scriptContext.printOpen("${message}", echoLevel)

        }

    }

}

