import com.project.alm.*

def call(PomXmlStructure pomXml, PipelineData pipeline) {
	if (pipeline.garArtifactType != GarAppType.BFF_SERVICE && pipeline.garArtifactType != GarAppType.DATA_SERVICE) {
	    printOpen("There are no dependency restrictions for ${pipeline.garArtifactType}", EchoLevel.INFO)
		return
	}
    def bffRestrictions = [
        /com\.project\.absis\.ads\.transaction:ads-.*-lib/,
        /com\.project\.absis\.arch\.backend\.ads:adsconnector-common-lib/,
        /com\.project\.absis\.arch\.backend\.ads:adsconnector-lib-starter/,
        /com\.project\.absis\.arch\.backend\.ase:se-.*-spring-boot-starter/,
        /com\.project\.absis\.arch\.backend\.absis2:a2connector-spring-boot-starter/,
        /org\.hibernate:hibernate-core/,
        /com\.oracle:ojdbc6/,
        /org\.springframework:spring-jdbc/,
        /jakarta\.persistence:jakarta\.persistence-api/,
        /javax\.persistence-api:javax\.persistence/
    ]
    def dataserviceRestrictions = [
        /com\.project\.absis\.ads\.transaction:ads-.*-lib/,
        /com\.project\.absis\.arch\.backend\.ads:adsconnector-common-lib/,
        /com\.project\.absis\.arch\.backend\.ads:adsconnector-lib-starter/
    ]

    def bffWhitelist = GlobalVars.ALM_SERVICES_DEPENDENCY_WHITELIST_BFF
    def dataserviceWhitelist = GlobalVars.ALM_SERVICES_DEPENDENCY_WHITELIST_DATASERVICE

    def forbiddenDependencies = [:]

    try {
        def fullArtifactName = "${pomXml.groupId}:${pomXml.artifactName}:${pomXml.artifactVersion}"
        printOpen("Validating dependency restrictions for ${fullArtifactName} as ${pipeline.garArtifactType} ...", EchoLevel.INFO)

        sh "rm -f dependency-tree.txt"

        //def cmd = "mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Default_Maven_Settings> dependency:tree -DoutputFile=dependency-tree.txt -DappendOutput=true -Dtokens=whitespace"
		def cmd = "mvn  <Default_Maven_Settings> dependency:tree -DoutputFile=dependency-tree.txt -DappendOutput=true -Dtokens=whitespace"
        def dependenciesRawLog = runMavenGoalWithRetries(pomXml, pipeline, cmd, [
            archiveLogIfMvnDurationExceeds: 5,
            kpiAlmEvent: new KpiAlmEvent(
                pomXml, pipeline,
                KpiAlmEventStage.UNDEFINED,
                KpiAlmEventOperation.MVN_VALIDATE_FORBIDDEN_DEPENDENCY_RESTRICTIONS)
        ])
		
		def dependencies = sh(returnStdout: true, script: "cat dependency-tree.txt");
		
        if (pipeline.garArtifactType == GarAppType.BFF_SERVICE) {
            printOpen("Validating BFF restrictions", EchoLevel.DEBUG)
            def bffValidator = new PomForbiddenDependenciesValidator(bffRestrictions, bffWhitelist)
            forbiddenDependencies = bffValidator.validate(fullArtifactName, dependencies)

        } else if (pipeline.garArtifactType == GarAppType.DATA_SERVICE) {
            printOpen("Validating DS restrictions", EchoLevel.DEBUG)
            def dataserviceValidator = new PomForbiddenDependenciesValidator(dataserviceRestrictions, dataserviceWhitelist)
            forbiddenDependencies = dataserviceValidator.validate(fullArtifactName, dependencies)

        }

        if (forbiddenDependencies) {
            String errorMessage = "This component type does not allow the following dependencies to be used:" +
                "\n\n${forbiddenDependencies}\n\n" +
                "Please remove them from the pom and try again. In case they are not in your pom you're probably getting them via transitive dependencies"
            printOpen("${errorMessage}", EchoLevel.ERROR)
            throw new RuntimeException("${errorMessage}")

        } else {
            printOpen("The dependency restrictions have been validated.", EchoLevel.INFO)
        }

    } catch (err) {

        printOpen(Utilities.prettyException(err), EchoLevel.ERROR) 
        throw err

    }
}
