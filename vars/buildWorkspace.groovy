import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.KpiAlmEvent
import com.caixabank.absis3.KpiAlmEventOperation
import com.caixabank.absis3.KpiAlmEventStage
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.WorkspaceUtils
import com.caixabank.absis3.GlobalVars

def call(PomXmlStructure pomXml, PipelineData pipeline) {
/**
 * [ERROR]     Unresolveable build extension: Plugin org.apache.maven.plugins:maven-javadoc-plugin:3.0.0 or one of its dependencies could not be resolved: Failed to collect dependencies at org.apache.maven.plugins:maven-javadoc-plugin:jar:3.0.0 -> org.apache.httpcomponents:httpclient:jar:4.5.2 -> commons-logging:commons-logging:jar:1.2: Failed to read artifact descriptor for commons-logging:commons-logging:jar:1.2: Could not transfer artifact org.apache.commons:commons-parent:pom:34 from/to nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/): GET request of: org/apache/commons/commons-parent/34/commons-parent-34.pom from nexus-pro-public-group failed: Premature end of Content-Length delimited message body (expected: 55943; received: 6557 -> [Help 2
 */
    def additionalParameters = ''
    pipeline.mvnAdditionalParameters.each { parameter ->
        additionalParameters += parameter && parameter != "null" ? "${parameter} " : ""
    }

    def muleParameters = ''
    pipeline.mvnMuleParameters.config.each { parameter ->
        muleParameters += parameter && parameter != "null" ? "${parameter} " : ""
    }

    printOpen("Executing maven clean verify... ", EchoLevel.INFO)

    /* Cuidado, deben mantenerse los dos parametros "-Dmaven.test.skip=true -DskipTests" para evitar la compilacion de tests pero tambien
       la ejecucion en caso de realizarse con un target ya generado anteriormente */
    //def cmd = "mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Default_Maven_Settings> clean verify -Dskip-generate-static-docs=${pipeline.getExecutionMode().skipJavadoc()} -Dmaven.test.skip=${pipeline.getExecutionMode().skipTest()} -DskipTests=${pipeline.getExecutionMode().skipTest()} ${additionalParameters}"
	def cmd = "mvn  <Default_Maven_Settings> clean verify -Dskip-generate-static-docs=${pipeline.getExecutionMode().skipJavadoc()} -Dmaven.test.skip=${pipeline.getExecutionMode().skipTest()} -DskipTests=${pipeline.getExecutionMode().skipTest()} $GlobalVars.MVN_TEST_LOGGERS_LEVEL $additionalParameters $muleParameters "
	boolean weHaveToGenerateOpenApiClasses =
        WorkspaceUtils.isThereASwaggerContract(this, pomXml) &&
            ! WorkspaceUtils.areSwaggerContractClassesGenerated(this, pomXml)
    if ( ! weHaveToGenerateOpenApiClasses ) cmd += " -Dcodegen.skip=true "

    runMavenGoalWithRetries(pomXml, pipeline, cmd, [
        archiveLogIfMvnDurationExceeds: 40,
        showOnlyMvnErrorsInLog: true,
        kpiAlmEvent: new KpiAlmEvent(
            pomXml, pipeline,
            KpiAlmEventStage.UNDEFINED,
            KpiAlmEventOperation.MVN_BUILD_WORKSPACE)
    ])

    printOpen("Maven clean verify ended successfully.", EchoLevel.INFO)

     // Elimimamos de /tmp el contrato recien generado
    cleanTmp()
}