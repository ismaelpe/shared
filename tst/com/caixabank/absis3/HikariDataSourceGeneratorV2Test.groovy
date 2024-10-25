package com.caixabank.absis3

import org.junit.Test
import org.junit.jupiter.api.Disabled

public class HikariDataSourceGeneratorV2Test extends GroovyTestCase {

    TTSSResourcesUtility ttssResourcesUtility
    Map configuracionDataSource
    DataSourceGenerator generator


    void initTest(String pathToSystemsYaml = './tst/resources/definitionBackendV2.yml') {

        println "Init of the test"

        ttssResourcesUtility = new TTSSResourcesUtility()
        configuracionDataSource = (Map) ttssResourcesUtility.getConfigResource(pathToSystemsYaml)
        println "Original"
        TTSSResourcesUtility.printYamlFile(configuracionDataSource)

        generator = ttssResourcesUtility.getDataSourceGenerator(TTSSResourcesUtility.V2)

    }

    void printOutput(def resultConfiguracionDataSource) {

        println "The content of the yaml " + resultConfiguracionDataSource
        println "Resultado"
        TTSSResourcesUtility.printYamlFile(resultConfiguracionDataSource)

    }
	
	@Test
	void testGetHikariDataSourceGeneratorV2_readOnly() {

		initTest('./tst/resources/definitionBackendV2WithReadOnly.yml')

		TuplePair touple = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource)
		Map resultConfiguracionDataSource = touple.getFirst()
		printOutput(resultConfiguracionDataSource)
	}

    @Test
    void testGetHikariDataSourceGeneratorV2() {

        initTest()

        TuplePair touple = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource)
        Map resultConfiguracionDataSource = touple.getFirst()

        printOutput(resultConfiguracionDataSource)
    }

    @Test
    void test_given_NoTenantsGivenByGARAndOnlyCBKInYaml_ValidationGoesOK() {

        initTest()

        def connections = configuracionDataSource?.datasource?.connections
        def readOnlyConnections = configuracionDataSource['readonly-datasource']?.connections

        // File is multitenant by default, we'll remove connections so it's CBK-only
        connections.remove("maria")
        connections.remove("jesus")
        readOnlyConnections.remove("pepito")
        readOnlyConnections.remove("pepitogrillo")

        TuplePair touple = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource, [:])
        Map resultConfiguracionDataSource = touple.getFirst()

        printOutput(resultConfiguracionDataSource)

        connections = resultConfiguracionDataSource?.absis?.datasource?.connections
        readOnlyConnections = resultConfiguracionDataSource?.absis['readonly-datasource']?.connections

        assert connections?.size() == 1, 'We did not get the expected connections'
        assert connections.jose?.tenant == '01', 'We did not get the expected tenant'

        assert readOnlyConnections?.size() == 1, 'We did not get the expected readOnlyConnections'
        assert readOnlyConnections.grillo?.tenant == '01', 'We did not get the expected tenant'
    }

    @Test
    void test_given_NoTenantsGivenByGARAndNoTenantsInYaml_ValidationGoesOKAsWeAssumeCBKIsUsed() {

        initTest()

        def connections = configuracionDataSource?.datasource?.connections
        def readOnlyConnections = configuracionDataSource['readonly-datasource']?.connections

        // File is multitenant by default, we'll remove connections so it's CBK-only
        connections.remove("maria")
        connections.remove("jesus")
        readOnlyConnections.remove("pepito")
        readOnlyConnections.remove("pepitogrillo")

        // We'll remove also the tenant property in the TS file. We assume CBK is used
        connections.jose.remove("tenant")
        readOnlyConnections.grillo.remove("tenant")

        TuplePair touple = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource, [:])
        Map resultConfiguracionDataSource = touple.getFirst()

        printOutput(resultConfiguracionDataSource)

        connections = resultConfiguracionDataSource?.absis?.datasource?.connections
        readOnlyConnections = resultConfiguracionDataSource?.absis['readonly-datasource']?.connections

        assert connections?.size() == 1, 'We did not get the expected connections'
        assert connections.jose?.tenant == '01', 'We did not get the expected tenant'

        assert readOnlyConnections?.size() == 1, 'We did not get the expected readOnlyConnections'
        assert readOnlyConnections.grillo?.tenant == '01', 'We did not get the expected tenant'
    }
/*
    @Test
    void test_given_AllTenantsGivenByGARButMissingOneInYaml_GenerationFailsAsWeExpectTSToConfigureAll() {

        initTest()

        def connections = configuracionDataSource?.datasource?.connections
        def readOnlyConnections = configuracionDataSource['readonly-datasource']?.connections

        // File is multitenant by default, we'll remove 0P to trigger the error
        connections.remove("maria")
        connections.remove("jesus")
        readOnlyConnections.remove("pepito")
        readOnlyConnections.remove("pepitogrillo")

        def thrownException

        try {

            generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource, ['0P': true, '01': true])

        } catch(Exception e) {

            thrownException = e

        }

        assert thrownException instanceof HikariDataSourceTenantValidationException, 'We did not get the expected type of exception'
        HikariDataSourceTenantValidationException validationException = ((HikariDataSourceTenantValidationException) thrownException)
        assert validationException.expectedTenantList == ['0P','01'] as Set, 'We did not get the expected garTenantList'
        assert validationException.datasourcesTenants == ['01'] as Set, 'We did not get the expected datasourcesTenants'
        assert validationException.readonlyDatasourcesTenants == ['01'] as Set, 'We did not get the expected readonlyDatasourcesTenants'
        assert validationException.datasourceConnections == [jose:[password:'PasswordEncriptado', 'driver-class-name':'oracle.jdbc.OracleDriver', tenant:'01', url:'jdbc:oracle:thin:@(DESCRIPTION_LIST=(LOAD_BALANCE=OFF)(FAILOVER=ON)(DESCRIPTION=(CONNECT_TIMEOUT=10)(TRANSPORT_CONNECT_TIMEOUT=3)(RETRY_COUNT=3)(LOAD_BALANCE=on)(FAILOVER=on)(ADDRESS=(PROTOCOL=tcp)(HOST=bkoradbt09-vip.lacaixa.es)(PORT=1528))(ADDRESS=(PROTOCOL=tcp)(HOST=bkoradbt11-vip.lacaixa.es)(PORT=1528))(CONNECT_DATA=(SERVICE_NAME=CGNOFI1T)(FAILOVER_MODE=(TYPE=select)(METHOD=basic))))(DESCRIPTION=(CONNECT_TIMEOUT=10)(TRANSPORT_CONNECT_TIMEOUT=3)(RETRY_COUNT=3)(ADDRESS=(PROTOCOL=TCP)(HOST=conordbt04.lacaixa.es)(PORT=1554))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=CGNOFI1T))))', hikari:[initializationFailTimeout:3000, validationTimeout:180000, maxLifetime:180000, minimumIdle:2, idleTimeout:5000, connectionTestQuery:'SELECT 1 FROM DUAL', dataSourceProperties:[user:'XXXPQT01', clientProgramName:'IMSEPPA', currentSchema:'PI97121'], driverClassName:'oracle.jdbc.OracleDriver', maximumPoolSize:4, autoCommit:false, connectionTimeout:30000, prepStmtCacheSize:10, poolName:'cgnofi']]], 'We did not get the expected datasourceConnections'
        assert validationException.readOnlyDatasourceConnections == [grillo:[password:'asd', 'driver-class-name':'oracle.jdbc.OracleDriver', tenant:'01', url:'jdbc:oracle', hikari:[driverClassName:'oracle.jdbc.OracleDriver', maximumPoolSize:7]]], 'We did not get the expected readOnlyDatasourceConnections'
        assert validationException.message ==
            "It seems that one or more datasource tenants configured in GAR for this application are not present in the TS configuration",
            'We did not get the expected Message'

    }*/

    @Test
    void test_given_AllTenantsGivenByGARAndATenantListHasBoth_GenerationGoesOK() {

        initTest()

        def connections = configuracionDataSource?.datasource?.connections
        def readOnlyConnections = configuracionDataSource['readonly-datasource']?.connections

        // We'll remove 0P from single tenant but not the tenantList
        connections.remove("maria")
        readOnlyConnections.remove("pepito")

        TuplePair touple = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource, ['0P': true, '01': true])
        Map resultConfiguracionDataSource = touple.getFirst()

        printOutput(resultConfiguracionDataSource)

        connections = resultConfiguracionDataSource?.absis?.datasource?.connections
        readOnlyConnections = resultConfiguracionDataSource?.absis['readonly-datasource']?.connections

        assert connections?.size() == 2, 'We did not get the expected connections'
        assert connections.jesus['tenant-list'] == '0P,01', 'We did not get the expected tenant-list'
        assert connections.jose?.tenant == '01', 'We did not get the expected tenant'

        assert readOnlyConnections?.size() == 2, 'We did not get the expected readOnlyConnections'
        assert readOnlyConnections.pepitogrillo['tenant-list'] == '01,0P', 'We did not get the expected tenant-list'
        assert readOnlyConnections.grillo?.tenant == '01', 'We did not get the expected tenant'

    }

    @Test
    void test_given_AllTenantsGivenByGARAndBothArePresentAsSingleDatasources_GenerationGoesOK() {

        initTest()

        def connections = configuracionDataSource?.datasource?.connections
        def readOnlyConnections = configuracionDataSource['readonly-datasource']?.connections

        // We'll remove both tenantList but not the single containing 0P
        connections.remove("jesus")
        readOnlyConnections.remove("pepitogrillo")

        TuplePair touple = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource, ['0P': true, '01': true])
        Map resultConfiguracionDataSource = touple.getFirst()

        printOutput(resultConfiguracionDataSource)

        connections = resultConfiguracionDataSource?.absis?.datasource?.connections
        readOnlyConnections = resultConfiguracionDataSource?.absis['readonly-datasource']?.connections

        assert connections?.size() == 2, 'We did not get the expected connections'
        assert connections.jose?.tenant == '01', 'We did not get the expected tenant'
        assert connections.maria?.tenant == '0P', 'We did not get the expected tenant'

        assert readOnlyConnections?.size() == 2, 'We did not get the expected readOnlyConnections'
        assert readOnlyConnections.pepito?.tenant == '0P', 'We did not get the expected tenant'
        assert readOnlyConnections.grillo?.tenant == '01', 'We did not get the expected tenant'

    }

    @Test
    void test_given_AllTenantsGivenByGARAndInYaml_GenerationGoesOKAndIncludesBoth() {

        initTest()

        TuplePair touple = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource, ['0P': true, '01': true])
        Map resultConfiguracionDataSource = touple.getFirst()

        printOutput(resultConfiguracionDataSource)

        def connections = resultConfiguracionDataSource?.absis?.datasource?.connections
        def readOnlyConnections = resultConfiguracionDataSource?.absis['readonly-datasource']?.connections

        assert connections?.size() == 3, 'We did not get the expected connections'
        assert connections.jesus['tenant-list'] == '0P,01', 'We did not get the expected tenant-list'
        assert connections.jose?.tenant == '01', 'We did not get the expected tenant'
        assert connections.maria?.tenant == '0P', 'We did not get the expected tenant'

        assert readOnlyConnections?.size() == 3, 'We did not get the expected readOnlyConnections'
        assert readOnlyConnections.pepitogrillo['tenant-list'] == '01,0P', 'We did not get the expected tenant-list'
        assert readOnlyConnections.pepito?.tenant == '0P', 'We did not get the expected tenant'
        assert readOnlyConnections.grillo?.tenant == '01', 'We did not get the expected tenant'
    }

    @Test
    void test_given_AllTenantsGivenByGARAndInYamlButSplitInRWAndRODatasources_GenerationGoesOKAndIncludesBoth() {

        initTest('./tst/resources/definitionBackendV2WithDifferentTenantsInRWAndRO.yml')

        TuplePair touple = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource, ['0P': true, '01': true])
        Map resultConfiguracionDataSource = touple.getFirst()

        printOutput(resultConfiguracionDataSource)

        def connections = resultConfiguracionDataSource?.absis?.datasource?.connections
        def readOnlyConnections = resultConfiguracionDataSource?.absis['readonly-datasource']?.connections

        assert connections?.size() == 1, 'We did not get the expected connections'
        assert connections.jose?.tenant == '01', 'We did not get the expected tenant'

        assert readOnlyConnections?.size() == 1, 'We did not get the expected readOnlyConnections'
        assert readOnlyConnections.pepito?.tenant == '0P', 'We did not get the expected tenant'
    }

    @Test
    void test_given_YamlHasTenantsNotPresentInGARAndTenantsThatArePresentInGAR_GenerationGoesOKButTenantsNotInGARAreNotIncluded() {

        initTest()

        TuplePair touple = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource, ['0P': true])
        Map resultConfiguracionDataSource = touple.getFirst()

        printOutput(resultConfiguracionDataSource)

        def connections = resultConfiguracionDataSource?.absis?.datasource?.connections
        def readOnlyConnections = resultConfiguracionDataSource?.absis['readonly-datasource']?.connections

        assert connections?.size() == 2, 'We did not get the expected connections'
        assert connections.maria?.tenant == '0P', 'We did not get the expected tenant'
        assert connections.jesus['tenant-list'] == '0P', 'We did not get the expected tenant-list'

        assert readOnlyConnections?.size() == 2, 'We did not get the expected readOnlyConnections'
        assert readOnlyConnections.pepito?.tenant == '0P', 'We did not get the expected tenant'
        assert readOnlyConnections.pepitogrillo['tenant-list'] == '0P', 'We did not get the expected tenant-list'

    }


    //@Test
	@Disabled("Esto no puede funcionar esta clase no lanza este tipo de excepciones")
   /*
	void test_given_NoTenantInYamlMatchesWithGAR_GenerationFails() {

        initTest()

        def thrownException

        try {

            generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource, ['02': true])

        } catch(Exception e) {

            thrownException = e

        }
		println "Sacanos la excepciotn " +thrownException

        assert thrownException instanceof HikariDataSourceTenantValidationException, 'We did not get the expected type of exception'
        HikariDataSourceTenantValidationException validationException = ((HikariDataSourceTenantValidationException) thrownException)
		println "El error es de "+validationException
        assert validationException.expectedTenantList == ['02'] as Set, 'We did not get the expected garTenantList'
        assert validationException.datasourcesTenants == [] as Set, 'We did not get the expected datasourcesTenants'
        assert validationException.readonlyDatasourcesTenants == [] as Set, 'We did not get the expected readonlyDatasourcesTenants'
        assert validationException.datasourceConnections == [:], 'We did not get the expected datasourceConnections'
        assert validationException.readOnlyDatasourceConnections == [:], 'We did not get the expected readOnlyDatasourceConnections'
        assert validationException.message ==
            "It seems that one or more datasource tenants configured in GAR for this application are not present in the TS configuration",
            'We did not get the expected Message'

    }
*/
    @Test
    void test_given_NoTenantInYamlMatchesWithGARButCustomTenantsIsEnabled_GenerationGoesOK() {

        initTest('./tst/resources/definitionBackendV2WithCustomTenants.yml')

        TuplePair touple = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource, ['02': true])
        Map resultConfiguracionDataSource = touple.getFirst()

        printOutput(resultConfiguracionDataSource)

        def connections = resultConfiguracionDataSource?.absis?.datasource?.connections
        def readOnlyConnections = resultConfiguracionDataSource?.absis['readonly-datasource']?.connections

        assert connections?.size() == 2, 'We did not get the expected connections'
        assert connections['uk-as400']?.tenant == 'uk', 'We did not get the expected tenant'
        assert connections['cre-as400']?.tenant == 'cre', 'We did not get the expected tenant'

        assert readOnlyConnections?.size() == 2, 'We did not get the expected readOnlyConnections'
        assert readOnlyConnections['ma-as400']?.tenant == 'ma', 'We did not get the expected tenant'
        assert readOnlyConnections['pl-as400']?.tenant == 'pl', 'We did not get the expected tenant'

    }
/*
    @Test
    void test_given_NoMatchesWithGARButIMSArePresent_GenerationFailsAsGARTenantsHaveToBePresentAsWell() {

        initTest('./tst/resources/definitionBackendV2WithIMSAndALLTenants.yml')

        def connections = configuracionDataSource?.datasource?.connections
        def readOnlyConnections = configuracionDataSource['readonly-datasource']?.connections

        // We'll remove ALL tenants to trigger error
        connections.remove("oracledb")
        readOnlyConnections.remove("rooracledb")

        def thrownException

        try {

            generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource, ['02': true])

        } catch(Exception e) {

            thrownException = e

        }

        assert thrownException instanceof HikariDataSourceTenantValidationException, 'We did not get the expected type of exception'
        HikariDataSourceTenantValidationException validationException = ((HikariDataSourceTenantValidationException) thrownException)
        assert validationException.expectedTenantList == ['02','IMSEPPC5','IMSEPPB'] as Set, 'We did not get the expected garTenantList'
        assert validationException.datasourcesTenants == ['IMSEPPC5'] as Set, 'We did not get the expected datasourcesTenants'
        assert validationException.readonlyDatasourcesTenants == ['IMSEPPB'] as Set, 'We did not get the expected readonlyDatasourcesTenants'
        assert validationException.datasourceConnections == [IMSEPPC5:[password:'${vcap.services.cbk-apps-channelspecific-lifein-imseppc5-database.credentials.password:secret}', 'driver-class-name':'com.ibm.db2.jcc.DB2Driver', tenant:'IMSEPPC5', url:'${vcap.services.cbk-apps-channelspecific-lifein-imseppc5-database.credentials.uri:uri}', hikari:[driverClassName:'com.ibm.db2.jcc.DB2Driver'], username:'${vcap.services.cbk-apps-channelspecific-lifein-imseppc5-database.credentials.username:user}']], 'We did not get the expected datasourceConnections'
        assert validationException.readOnlyDatasourceConnections == [IMSEPPB:[password:'${vcap.services.cbk-apps-db2-database.credentials.password}', 'driver-class-name':'com.ibm.db2.jcc.DB2Driver', tenant:'IMSEPPB', url:'jdbc:db2://sysj.lacaixa.es:5106/DBPB', hikari:[driverClassName:'com.ibm.db2.jcc.DB2Driver'], username:'${vcap.services.cbk-apps-db2-database.credentials.username}']], 'We did not get the expected readOnlyDatasourceConnections'
        assert validationException.message ==
            "It seems that one or more datasource tenants configured in GAR for this application are not present in the TS configuration",
            'We did not get the expected Message'

    }*/

    @Test
    void test_given_WeHaveOneMatchWithGARPlusIMSTenants_GenerationGoesOKAsIMSAreNotValidatedAgainstGAR() {

        initTest('./tst/resources/definitionBackendV2WithIMSAndALLTenants.yml')

        def connections = configuracionDataSource?.datasource?.connections
        def readOnlyConnections = configuracionDataSource['readonly-datasource']?.connections

        // We'll remove ALL tenants to check against IMS and GAR tenants
        connections.remove("oracledb")
        readOnlyConnections.remove("rooracledb")

        TuplePair touple = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource, ['01': true])
        Map resultConfiguracionDataSource = touple.getFirst()

        printOutput(resultConfiguracionDataSource)

        connections = resultConfiguracionDataSource?.absis?.datasource?.connections
        readOnlyConnections = resultConfiguracionDataSource?.absis['readonly-datasource']?.connections

        assert connections?.size() == 2, 'We did not get the expected connections'
        assert connections['IMSEPPC5']?.tenant == 'IMSEPPC5', 'We did not get the expected tenant'
        assert connections['default']?.tenant == '01', 'We did not get the expected tenant'

        assert readOnlyConnections?.size() == 2, 'We did not get the expected readOnlyConnections'
        assert readOnlyConnections['IMSEPPB']?.tenant == 'IMSEPPB', 'We did not get the expected tenant'
        assert readOnlyConnections['rodefault']?.tenant == '01', 'We did not get the expected tenant'

    }

    @Test
    void test_given_NoMatchesWithGARButALLTenantIsPresent_GenerationGoesOKAsALLOverridesGARValidation() {

        initTest('./tst/resources/definitionBackendV2WithIMSAndALLTenants.yml')

        TuplePair touple = generator.generate("demo", "myapp", GarAppType.DATA_SERVICE, configuracionDataSource, ['02': true])
        Map resultConfiguracionDataSource = touple.getFirst()

        printOutput(resultConfiguracionDataSource)

        def connections = resultConfiguracionDataSource?.absis?.datasource?.connections
        def readOnlyConnections = resultConfiguracionDataSource?.absis['readonly-datasource']?.connections

        assert connections?.size() == 2, 'We did not get the expected connections'
        assert connections['oracledb']?.tenant == 'ALL', 'We did not get the expected tenant'
        assert connections['IMSEPPC5']?.tenant == 'IMSEPPC5', 'We did not get the expected tenant'

        assert readOnlyConnections?.size() == 2, 'We did not get the expected readOnlyConnections'
        assert readOnlyConnections['rooracledb']?.tenant == 'ALL', 'We did not get the expected tenant'
        assert readOnlyConnections['IMSEPPB']?.tenant == 'IMSEPPB', 'We did not get the expected tenant'

    }

}
