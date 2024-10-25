import com.caixabank.absis3.BranchType
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.PomXmlStructure
import com.caixabank.absis3.PipelineData

def call(Map pipelineParams, PomXmlStructure pomXml, PipelineData pipeline, Map params) {

    call(pipelineParams, pomXml, pipeline)
    
    printOpen("************************************************************************************", EchoLevel.DEBUG)
    printOpen("***                                  JOB PARAMS                                  ***", EchoLevel.DEBUG)
    printOpen("************************************************************************************", EchoLevel.DEBUG)
    printOpen('"${params}"?.replace("[", "[\n").replace(", ", ",\n").replace("]", "\n]', EchoLevel.DEBUG)
    printOpen('************************************************************************************', EchoLevel.DEBUG)

}

def call(Map pipelineParams, PomXmlStructure pomXml, PipelineData pipeline) {

    if (env.JENKINS_SHOW_ENNVARS?.toBoolean()) {
        printOpen('************************************************************************************', EchoLevel.DEBUG)
        printOpen('***                                           ENVIRONMENT                        ***', EchoLevel.DEBUG)
        printOpen('************************************************************************************', EchoLevel.DEBUG)
        sh 'printenv'
    }

    printOpen('************************************************************************************', EchoLevel.DEBUG)
    printOpen('***                                  PIPELINE PARAMS                             ***', EchoLevel.DEBUG)
    printOpen('************************************************************************************', EchoLevel.DEBUG)
    printOpen("${pipelineParams}", EchoLevel.DEBUG)
    printOpen('************************************************************************************', EchoLevel.DEBUG)
    printOpen("ARTIFACT TYPE: ${pomXml?.artifactType}", EchoLevel.DEBUG)
    printOpen("ARTIFACT SUBTYPE: ${pomXml?.artifactSubType}", EchoLevel.DEBUG)
    printOpen("ARTIFACT MICRO: ${pomXml?.artifactMicro}", EchoLevel.DEBUG)
    printOpen("ARTIFACT LIB: ${pomXml?.artifactLib}", EchoLevel.DEBUG)
    printOpen("ARTIFACT SAMPLE APP: ${pomXml?.artifactSampleApp}", EchoLevel.DEBUG)
    printOpen("GIT URL: ${pipeline?.gitUrl}", EchoLevel.DEBUG)
    printOpen("GIT ACTION: ${pipeline?.gitAction}", EchoLevel.DEBUG)
    printOpen("COMMIT: ${pipeline?.commitLog}", EchoLevel.DEBUG)
    printOpen("DEPLOY BMX: ${pipeline?.deployFlag}", EchoLevel.DEBUG)
    printOpen("PIPELINE TYPE (isMultiBranch?): ${pipeline?.isMultiBranchPipeline}", EchoLevel.DEBUG)
    printOpen("BRANCH NAME: ${pipeline?.branchStructure?.branchName}", EchoLevel.DEBUG)
    printOpen("BRANCH TYPE: ${pipeline?.branchStructure?.branchType}", EchoLevel.DEBUG)
    printOpen("IS PUSH CI: ${pipeline?.isPushCI()}", EchoLevel.DEBUG)
    script {
        if (pipeline?.branchStructure?.branchType == BranchType.FEATURE) {
            printOpen("FEATURE: ${pipeline?.branchStructure?.featureNumber}", EchoLevel.DEBUG)
        }
        if (pipeline?.branchStructure?.branchType == BranchType.MASTER) {
            printOpen("BRANCH ORIGIN TO MASTER: ${pipeline?.originPushToMaster}", EchoLevel.DEBUG)
            printOpen("BRANCH FEATURE/RELEASE TO MASTER: ${pipeline?.eventToPush}", EchoLevel.DEBUG)
            printOpen("MERGE USER: ${pipeline?.pushUser}", EchoLevel.DEBUG)
            printOpen("MERGE USER EMAIL: ${pipeline?.pushUserEmail}", EchoLevel.DEBUG)
        }
    }
    printOpen('************************************************************************************', EchoLevel.DEBUG)

}
