import com.caixabank.absis3.BranchType
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.KpiAlmEvent
import com.caixabank.absis3.KpiAlmEventOperation
import com.caixabank.absis3.KpiAlmEventStage
import com.caixabank.absis3.MavenGoalExecutionException
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.WorkspaceUtils
import com.caixabank.absis3.MavenUtils
import com.caixabank.absis3.GlobalVars

def call(PomXmlStructure pomXml, PipelineData pipeline, boolean isValidatingVersion, boolean isAbsis2) {


    String pathToRevapiPom = pipeline.revapiStructure.revapiPomPath
    String pathToContract = pipeline.revapiStructure.swaggerContractPath

    // Si se ha definido el remote lo usamos
    if (pipeline.revapiStructure.remoteSwaggerContractPath) {
        pathToContract = pipeline.revapiStructure.remoteSwaggerContractPath
    }

    String profile = "checkVersion"
    String goal = "verify"

    if (!isValidatingVersion && pipeline.deployFlag && (pipeline.branchStructure.branchType == BranchType.MASTER || pipeline.branchStructure.branchType == BranchType.RELEASE || pipeline.branchStructure.branchType == BranchType.HOTFIX)) {
        profile = isAbsis2 ? "generateAbsis2Client" : "generateAbsisClient"
        goal = "deploy"
    }

    printOpen("Building client artifact with profile ${profile} and maven goal ${goal}\n Micro's path is ${env.WORKSPACE}@tmp", EchoLevel.DEBUG)

    try {
        def muleParameters = ''
        
        if (pipeline.mvnMuleParameters) {
            def mvnMuleParameters = [:]
            mvnMuleParameters.putAll(pipeline.mvnMuleParameters.config)
            mvnMuleParameters.putAll(pipeline.mvnMuleParameters.contract)
            
            mvnMuleParameters.each { parameter ->
                muleParameters += parameter && parameter != "null" ? "${parameter} " : ""
            }
        }
            
        //def cmd = "mvn -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Default_Maven_Settings> -f ${pathToRevapiPom} -P${profile} -Dcontract.package=${pomXml.contractPackage} -Dopenapi.contract=${pathToContract}  clean ${goal} "
		def cmd = "mvn <Only_Maven_Settings> -U -f $pathToRevapiPom -P$profile -Dcontract.package=$pomXml.contractPackage -Dopenapi.contract=$pathToContract $muleParameters clean $goal "
        boolean weHaveToGenerateOpenApiClasses =
            ( swaggerWorkspaceUtils.isThereASwaggerContract( pomXml) &&
                ! swaggerWorkspaceUtils.areSwaggerContractClassesGeneratedIn("${env.WORKSPACE}@tmp" , pomXml) )
        
		if ( ! weHaveToGenerateOpenApiClasses ) cmd += " -Dcodegen.skip=true "

        def commitLog = runMavenGoalWithRetries(pomXml, pipeline, cmd, [
            archiveLogIfMvnDurationExceeds: 10,
            showOnlyMvnErrorsInLog: true,
            kpiAlmEvent: new KpiAlmEvent(
                pomXml, pipeline,
                KpiAlmEventStage.UNDEFINED,
                KpiAlmEventOperation.MVN_INSTALL_DEPLOY_CLIENT_ARTIFACT)
        ])
		
		return commitLog
		
    } catch (MavenGoalExecutionException mgee) {

        def log = mgee?.mavenError?.errors[0]
        printOpen("Revapi check has failed.\nLog dump:\n\n${mgee?.mavenError?.log}", EchoLevel.ERROR)
        error "REVAPI FAILED!\n\n${log}"

    }

}
