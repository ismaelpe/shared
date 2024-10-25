import com.project.alm.*
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.util.regex.Matcher

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK

/**
 *
 * @param pomXml info del pom.xml
 * @param pipeline info de la pipeline
 * @return String
 */
def call(PomXmlStructure pomXml, PipelineData pipeline) {

    boolean error = false
    String backendFileTTSS = ""

    String repoUri = GitUtils.getConfigSysRepoUrl(pipeline.bmxStructure.environment)

    GitRepositoryHandler git = new GitRepositoryHandler(this, repoUri, [gitProjectRelativePath: 'config-sys'])

    try {
        printOpen("Cloning the config sys Repo", EchoLevel.ALL)

        git.pullOrClone([depth: 1])

        Map dataSourceapp = [:]
        Set vcapsServiceIds = []
        String application = pomXml.getApp(pipeline.garArtifactType)
        String version = pomXml.getArtifactMajorVersion()
        backendFileTTSS = getConfigSysFile(application, version)
        printOpen("The name of the neutral config file is ", EchoLevel.ALL)
        String domain = pipeline.domain;
        GarAppType artifactType = pipeline.garArtifactType;

        Map tenantsApp = [:]

        String disableGetTenants = "${env.ALM_SERVICES_SKIP_TENANT_GENERATION}"
        def whiteListAppsTemants = "${env.ALM_SERVICES_SKIP_TENANT_GENERATION_LIST}".split(";")

        if ("true".equals(disableGetTenants) || Arrays.asList(whiteListAppsTemants).contains(pomXml.artifactName)) {

            printOpen("SKIP Tenants Limitation", EchoLevel.ALL)

        } else {

            def appGar = getGarTenants(pomXml,pipeline)

            if (appGar?.tenants) {

                appGar.tenants.each {

                    tenantsApp += [(it.idTenant): true]

                }

            } else {

                printOpen("Tenants not found in GAR. This can be caused by an incorrect artifactId", EchoLevel.ALL)

            }
        }
        printOpen("Launching processDatasourceFileFromSys([:], [], ${backendFileTTSS}, ${version}, ${domain}, ${application}, ${artifactType.prettyPrint()}, ${tenantsApp})...", EchoLevel.ALL)
        processDatasourceFileFromSys(pomXml, pipeline, dataSourceapp, vcapsServiceIds, backendFileTTSS, version, domain, application, artifactType, tenantsApp)

        if(isDatasourceMultitenant(dataSourceapp)) {
			validateAllTenantsFromGARHaveBeenUsed(tenantsApp, dataSourceapp)
    	}

        pipeline.vcapsServiceIds = vcapsServiceIds
        printOpen("vcapsServiceIds is ", EchoLevel.ALL)

        printOpen("The content of the neutral config file after transformation is ", EchoLevel.ALL)

        def exists = sh(
            script: "ls ./application.yml",
            returnStatus: true
        )

        if (exists == 0) {
            sh(
                script: "rm ./application.yml",
                returnStatus: true
            )
        }

        exists = sh(
            script: "ls ./${GlobalVars.TMP_FILE_GENERATED_DATASOURCE}",
            returnStatus: true
        )

        if (exists == 0) {
            sh(
                script: "rm ./${GlobalVars.TMP_FILE_GENERATED_DATASOURCE}",
                returnStatus: true
            )
        }

        writeYaml file: "./${GlobalVars.TMP_FILE_GENERATED_DATASOURCE}", data: dataSourceapp
        printOpen("The content of the DataSource used in the bluemix environment", EchoLevel.ALL)
        sh "cat './${GlobalVars.TMP_FILE_GENERATED_DATASOURCE}'"

        return backendFileTTSS

    } catch (Exception e) {

        printOpen("Error ocurred generating the datasource yaml", EchoLevel.ERROR)
        throw e

    } finally {

        git.purge()

    }

}

private boolean isDatasourceMultitenant(Map datasourceMap) {
	return (datasourceMap.containsKey('datasource') && datasourceMap.get('datasource').containsKey('enable')) || (datasourceMap.containsKey('readonly-datasource') && datasourceMap.get('readonly-datasource').containsKey('enable')) || (datasourceMap.containsKey('readonly-datasource-list') && datasourceMap.get('readonly-datasource-list').containsKey('enable'));
}

private void processDatasourceFileFromSys(PomXmlStructure pomXml, PipelineData pipeline, Map dataSourceapp, Set vcapsServiceIds, String backendFileTTSS, String version, String domain, String application, GarAppType artifactType, Map tenantsApp) {
	printOpen("Start processDatasourceFileFromSys(${dataSourceapp}, ${vcapsServiceIds}, ${backendFileTTSS}, ${version}, ${domain}, ${application}, ${artifactType.prettyPrint()}, ${tenantsApp})", EchoLevel.ALL)
	TTSSResourcesUtility resourcesUtility = new TTSSResourcesUtility()
	def fileInfoYaml = readYaml file: backendFileTTSS

    String environment = pipeline.bmxStructure.environment.toLowerCase()

	printOpen("The content of the neutral config file is ", EchoLevel.ALL)

	DataSourceGenerator generator = null

	if (fileInfoYaml.containsKey('template')) {
		String template = fileInfoYaml.get('template')
		fileInfoYaml.remove('template')
		processDatasourceFileFromSys(pomXml, pipeline, dataSourceapp, vcapsServiceIds, "./config-sys/"+template, "", domain, application, artifactType, tenantsApp)
	}
	
	if (fileInfoYaml.containsKey('templates')) {
		List<String> templates = fileInfoYaml.get('templates')
		fileInfoYaml.remove('templates')
		templates.each { template -> processDatasourceFileFromSys(pomXml, pipeline, dataSourceapp, vcapsServiceIds, "./config-sys/"+template, "", domain, application, artifactType, tenantsApp) }
	}

    String appName = pomXml?.getArtifactName()?.toLowerCase() - '-micro'

    boolean weAreInPrevis = environment == "eden" || environment == "dev" || environment == "tst"
    boolean isProvisioningH2 = "${fileInfoYaml}" == "[datasource:[enable:true, connections:[${appName}db:[jdbcDriver:org.h2.Driver, tenant:ALL]]]]"

    printOpen("weAreInPrevis: ${weAreInPrevis}", EchoLevel.ALL)
    printOpen("isProvisioningH2: ${isProvisioningH2}", EchoLevel.ALL)

	//if we got the datasource.enable property, then we have the multitenant/readonly version of datasource
	if (fileInfoYaml.containsKey('datasource') || fileInfoYaml.containsKey('readonly-datasource') || fileInfoYaml.containsKey('readonly-datasource-list')) {
        
		if (weAreInPrevis && isProvisioningH2) {

            printOpen("choose H2-based DataSourceGenerator", EchoLevel.ALL)
            generator = resourcesUtility.getDataSourceGenerator(TTSSResourcesUtility.H2, this)
            TuplePair touple = generator.generate(domain, application, artifactType, fileInfoYaml, tenantsApp)
            mergeMaps(dataSourceapp, touple.getFirst())

            pipeline.isDataserviceWithH2InMemoryDatabase = true

        } else if (isDatasourceMultitenant(fileInfoYaml)) {
	printOpen("AntesDEfileInfoYaml ${fileInfoYaml}", EchoLevel.ALL)
            printOpen("choose DataSourceGenerator second version", EchoLevel.ALL)
            generator = resourcesUtility.getDataSourceGenerator(TTSSResourcesUtility.V2, this)
            TuplePair touple = generator.generate(domain, application, artifactType, fileInfoYaml, tenantsApp)
	printOpen("artifactType ${artifactType}", EchoLevel.ALL)
	printOpen("domain ${domain}", EchoLevel.ALL)
	printOpen("application ${application}", EchoLevel.ALL)
	printOpen("fileInfoYaml ${fileInfoYaml}", EchoLevel.ALL)
	printOpen("tennantsApp ${tenantsApp}", EchoLevel.ALL)
	printOpen("dataSourceapp ${dataSourceapp}", EchoLevel.ALL)
	printOpen("touple.getFirst ${touple.getFirst()}", EchoLevel.ALL)
            mergeMaps(dataSourceapp, touple.getFirst())
            vcapsServiceIds.addAll(touple.getSecond())

        } else {

			printOpen("choose DataSourceGenerator original version", EchoLevel.ALL)
			generator = resourcesUtility.getDataSourceGenerator(TTSSResourcesUtility.V1, this)
			mergeMaps(dataSourceapp, generator.generate(domain, application, artifactType, fileInfoYaml.get('datasource')))

		}
	}
	
}

private Map getConfigResource(String resourceType, String pathToSystemsYaml) {
    File data = new File(pathToSystemsYaml)
    printOpen( " Exists the file " + data.exists(), EchoLevel.DEBUG)

    def opts = new DumperOptions()
    opts.setDefaultFlowStyle(BLOCK)
    Yaml yaml = new Yaml(opts)

    Map propertiesFichero = (Map) yaml.load(data.newInputStream())


    return propertiesFichero.get(resourceType)
}

private void mergeMaps(Map mainMap, Map mapToMerge) {
	mapToMerge.each{
		if (mainMap.containsKey(it.key)) {
			if (it.value instanceof Map && mainMap[it.key] instanceof Map) {
				mergeMaps(mainMap[it.key], it.value)
			} else {
				mainMap[it.key] = mapToMerge[it.key]
			}
		} else {
			mainMap[it.key] = mapToMerge[it.key]
		}
	}
}

def getGarTenants(PomXmlStructure pomXml, PipelineData pipeline) {
	//Tenemos que validar los tenants definidos en GAR
	
	//Si es DATAService tiene sentido... en el resto de casos no
	if (pipeline.garArtifactType == GarAppType.DATA_SERVICE) {
		//Solo aplica en este caso
		def appId = pomXml.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name) )
		
		def app = idecuaRoutingUtils.getInfoAppFromTypeAndName(pipeline.garArtifactType.getGarName(), appId)
		
		return app
	}else {
		printOpen("No aplica ya que no es un DataSource", EchoLevel.ALL)
	}
	
}

private Map getConnectionsFromReadOnlyList(Map readOnlyDatasourceList) {
	def readOnlyConnectionsListMap = [:]
	
	if (readOnlyDatasourceList!=null) {
		readOnlyDatasourceList.each  {
			entry-> readOnlyConnectionsListMap.putAll(entry.value.connections)
		}
	}
	
	return readOnlyConnectionsListMap
}

private validateAllTenantsFromGARHaveBeenUsed(Map tenantsFromGAR, Map datasourceApp) {

	Map datasource = datasourceApp?.absis?.containsKey("datasource") ? datasourceApp.absis.datasource : [:]
	Map readOnlyDatasource = datasourceApp?.absis?.containsKey("readonly-datasource") ? datasourceApp.absis['readonly-datasource'] : [:]
	Map readOnlyDatasourceList = datasourceApp?.absis?.containsKey("readonly-datasource-list") ? datasourceApp.absis['readonly-datasource-list'] : [:]
	
	Set expectedTenantsInConfiguration = []

	def connections = datasource?.connections ? datasource.connections : [:]
	def readOnlyConnections = readOnlyDatasource?.connections ? readOnlyDatasource.connections : [:]	
	def readOnlyConnectionsList = getConnectionsFromReadOnlyList(readOnlyDatasourceList)

	tenantsFromGAR.each {
		if (it.value) expectedTenantsInConfiguration += it.key
	}
	expectedTenantsInConfiguration += getTenantsFrom(connections, /IMS.*/)
	expectedTenantsInConfiguration += getTenantsFrom(readOnlyConnections, /IMS.*/)
	expectedTenantsInConfiguration += getTenantsFrom(readOnlyConnectionsList, /IMS.*/)
	
	Set datasourcesTenants = getTenantsFrom(connections)
	Set readonlyDatasourcesTenants = getTenantsFrom(readOnlyConnections)
	Set readonlyDatasourcesListTenants = getTenantsFrom(readOnlyConnectionsList)
	
	expectedTenantsInConfiguration.each {

		boolean datasourceTenantsValidate =
			datasource.get("customTenants", false) ||
				specifiedTenantOrALLTenantIsPresentInDatasource(datasourcesTenants, it)

		boolean readonlyDatasourceTenantsValidate =
			readOnlyDatasource.get("customTenants", false) ||
				specifiedTenantOrALLTenantIsPresentInDatasource(readonlyDatasourcesTenants, it)
				
		boolean readonlyDatasourceListTenantsValidate =
			readOnlyDatasourceList.get("customTenants", false) ||
				specifiedTenantOrALLTenantIsPresentInDatasource(readonlyDatasourcesListTenants, it)

		if ( ! datasourceTenantsValidate && ! readonlyDatasourceTenantsValidate && ! readonlyDatasourceListTenantsValidate) {

			throw new HikariDataSourceTenantValidationException(
				"It seems that one or more datasource tenants configured in GAR for this application are not present in the TS configuration",
				expectedTenantsInConfiguration, datasourcesTenants, readonlyDatasourcesTenants, readonlyDatasourcesListTenants, connections, readOnlyConnections)
		}
	}
}

private specifiedTenantOrALLTenantIsPresentInDatasource(Set datasourceTenants, def tenant) {
	
	boolean datasourceTenantsValidate = false

	datasourceTenants.each {

		if (it == "ALL" || it == tenant) datasourceTenantsValidate = true

	}

	return datasourceTenantsValidate
}

private getTenantsFrom(def connections, def tenantRegex = /.*/) {
	
	Set usedTenants = []

	connections.each {

		boolean hasSingleTenant = it.value.containsKey("tenant")
		boolean hasTenantList = it.value.containsKey("tenant-list")

		if (hasTenantList) {

			it.value['tenant-list'].split(",").each {

				Matcher matches = it =~ tenantRegex
				if (matches.matches()) usedTenants += it

			}

		} else if (hasSingleTenant) {

			Matcher matches = it.value['tenant'] =~ tenantRegex
			if (matches.matches()) usedTenants += it.value['tenant']

		} else {

			//No configured tenant means CBK
			usedTenants += it.value['01']

		}

	}

	return usedTenants
}
