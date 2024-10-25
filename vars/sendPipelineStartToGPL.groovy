import com.project.alm.*

def call(PomXmlStructure pomXml, PipelineData pipelineData, String pipelineOrigId) {

    if (notificationToGplApplies()) {

        printOpen("Sending to GPL a Pipeline Start operation", EchoLevel.DEBUG)

        def url = idecuaRoutingUtils.pipelineUrl();
        String commitId = ''

        if (env.gitlabMergeRequestLastCommit != null) commitId = env.gitlabMergeRequestLastCommit

        if (pipelineData.commitId != '' && pipelineData.commitId != null) {
            commitId = pipelineData.commitId
        }

        def body = [
            id                : "${pipelineData.pipelineStructure.pipelineId}",
            plataforma        : pipelineData.pipelineStructure.plataforma,
            tipo              : "${pipelineData.garArtifactType.getGarName()}",
            aplicacion        : "${pomXml.getApp(pipelineData.garArtifactType)}",
            componente        : "${pomXml.artifactName}",
            userId            : "${pipelineData.pushUser}",
            versionMajor      : "${MavenVersionUtilities.getMajor(pomXml.getArtifactVersion())}",
            versionMinor      : "${MavenVersionUtilities.getMinor(pomXml.getArtifactVersion())}",
            versionFix        : "${MavenVersionUtilities.getPatch(pomXml.getArtifactVersion())}",
            build             : "${pipelineData.buildCode}",
            commit            : "${commitId}",
            branch            : "${pipelineData.branchStructure.branchName}",
            nombre            : pipelineData.pipelineStructure.nombre,
            estado            : "${GlobalVars.GPL_STATE_RUNNING}",
            fechaCreacion     : new Date(),
            anteriorPipelineId: "${pipelineOrigId}",
            stages            : pipelineData.pipelineStructure.getStages(),
            path              : "${env.JOB_NAME}",
            runId             : "${env.BUILD_NUMBER}",
            //serverUrl         : "${env.JENKINS_URL}",
            //serverUrl         : "https://jnkmsv.pro.int.srv.project.com/jenkins",
            serverUrl         : "${env.JNKMSV_DEVPORTAL_URL}",
            //jenkinsUserId     : "${GPL_USR}",
            //jenkinsUserToken  : "${GPL_PSW}"
            jenkinsUserId     : "${JNKMSV_USR}",
            jenkinsUserToken  : "${JNKMSV_PSW}"
        ]

        def response = sendRequestToGpl('POST', url, "", body, pipelineData, pomXml)

        return response
    }
}

/**
 * New version of sendPipelineStartToGPL, do not depend on PomXmlStructure, send minimun parameters
 * @param pipelineData
 * @param pipelineParams
 * @return
 */
def call(PipelineData pipelineData, Map pipelineParams) {

    if (notificationToGplApplies()) {

        printOpen("Sending to GPL a Pipeline Start operation", EchoLevel.DEBUG)

        def url = GlobalVars.URL_GPL + GlobalVars.PATH_GPL_PIPELINE

        ArtifactSubType artifactSubType = ArtifactSubType.valueOfSubType(pipelineParams.subType)
        GarAppType garAppType = PipelineData.initFromGitUrlGarApp(pipelineData.gitUrl, artifactSubType)
        String aplicacion = MavenUtils.sanitizeArtifactName(env.JOB_BASE_NAME, garAppType)

        def body = [
            id              : "${pipelineData.pipelineStructure.pipelineId}",
            plataforma      : "${pipelineData.pipelineStructure.plataforma}",
            tipo            : "${garAppType.getGarName()}",
            aplicacion      : "${aplicacion}",
            userId          : "${pipelineData.pushUser}",
            commit          : "${pipelineData.commitId}",
            nombre          : "${pipelineData.pipelineStructure.nombre}",
            estado          : "${GlobalVars.GPL_STATE_RUNNING}",
            fechaCreacion   : new Date(),
            stages          : pipelineData.pipelineStructure.getStages(),
            path            : "${env.JOB_NAME}",
            runId           : "${env.BUILD_NUMBER}",
            //serverUrl       : "${env.JENKINS_URL}",
            serverUrl         : "${env.JNKMSV_DEVPORTAL_URL}",
            //jenkinsUserId   : "${GPL_USR}",
            //jenkinsUserToken: "${GPL_PSW}"
            jenkinsUserId     : "${JNKMSV_USR}",
            jenkinsUserToken  : "${JNKMSV_PSW}"

        ]

        def response = sendRequestToGpl('POST', url, "", body, aplicacion, garAppType.getGarName())

        return response
    }
}


/**
 * New version of sendPipelineStartToGPL, do not depend on PomXmlStructure, send minimun parameters
 * @param pipelineData
 * @param pipelineParams
 * @return
 */
def call(PipelineData pipelineData, String garAppType, String garAppName, String major, String environment, String userId) {

    if (notificationToGplApplies()) {

        def response = sendRequestToAbsis3MS(
            'GET',
            "${GlobalVars.URL_CATALOGO_ALM_PRO}/app/${garAppType}/${garAppName}",
            null,
            "${GlobalVars.CATALOGO_ALM_ENV}",
            [
                kpiAlmEvent: new KpiAlmEvent(
                    null, null,
                    KpiAlmEventStage.UNDEFINED,
                    KpiAlmEventOperation.CATMSV_HTTP_CALL)
            ])

        if (response.status == 200) {
            //def json = new JsonSlurperClassic().parseText(response.content)
            def json = response.content

            def componente = json.name


            response = sendRequestToAbsis3MS(
                'GET',
                "${GlobalVars.URL_CATALOGO_ALM_PRO}/app/${garAppType}/${garAppName}/version/${major}/environment/${environment}",
                null,
                "${GlobalVars.CATALOGO_ALM_ENV}",
                [
                    kpiAlmEvent: new KpiAlmEvent(
                        null, null,
                        KpiAlmEventStage.UNDEFINED,
                        KpiAlmEventOperation.CATMSV_HTTP_CALL)
                ])

            if (response.status == 200) {
                printOpen("Recuperado los datos ${json}", EchoLevel.ALL)
                json = response.content

                def url = GlobalVars.URL_GPL + GlobalVars.PATH_GPL_PIPELINE

                def body = [
                    id              : "${pipelineData.pipelineStructure.pipelineId}",
                    plataforma      : "${pipelineData.pipelineStructure.plataforma}",
                    tipo            : "${garAppType}",

                    componente      : "${componente}",
                    versionMajor    : "${json.major}",
                    versionMinor    : "${json.minor}",
                    versionFix      : "${json.fix}",
                    build           : "${json.buildCode}",

                    aplicacion      : "${garAppName}",
                    userId          : "${userId}",
                    commit          : "N/A",
                    nombre          : "${pipelineData.pipelineStructure.nombre}",
                    estado          : "${GlobalVars.GPL_STATE_RUNNING}",
                    fechaCreacion   : new Date(),
                    stages          : pipelineData.pipelineStructure.getStages(),
                    path            : "${env.JOB_NAME}",
                    runId           : "${env.BUILD_NUMBER}",
                    //serverUrl       : "${env.JENKINS_URL}",
                    serverUrl       : "${env.JNKMSV_DEVPORTAL_URL}",
                    //jenkinsUserId   : "${GPL_USR}",
                    //jenkinsUserToken: "${GPL_PSW}"
                    jenkinsUserId   : "${JNKMSV_USR}",
                    jenkinsUserToken: "${JNKMSV_PSW}"

                ]

                response = sendRequestToGpl('POST', url, "", body, garAppName, garAppType)

                return response

            }

        } else {
            printOpen("Elemento no localizado en el catalogo ", EchoLevel.DEBUG)
        }

        return null
    }
}


def call(IClientInfo clientInfo, PipelineData pipelineData, String pipelineOrigId) {

    if (notificationToGplApplies()) {

        printOpen("Sending to GPL a Pipeline Start operation", EchoLevel.DEBUG)

        def url = idecuaRoutingUtils.pipelineUrl();

        String commitId = ''

        if (env.gitlabMergeRequestLastCommit != null) commitId = env.gitlabMergeRequestLastCommit

        if (pipelineData.commitId != '' && pipelineData.commitId != null) {
            commitId = pipelineData.commitId
        }

        def body = [
            id                : "${pipelineData.pipelineStructure.pipelineId}",
            plataforma        : pipelineData.pipelineStructure.plataforma,
            userId            : "${pipelineData.pushUser}",
            tipo              : "${pipelineData.garArtifactType.getGarName()}",
            aplicacion        : "${clientInfo.getApp(pipelineData.garArtifactType)}",
            componente        : "${clientInfo.getArtifactId()}",
            versionMajor      : "${MavenVersionUtilities.getMajor(clientInfo.getArtifactVersion())}",
            versionMinor      : "${MavenVersionUtilities.getMinor(clientInfo.getArtifactVersion())}",
            versionFix        : "${MavenVersionUtilities.getPatch(clientInfo.getArtifactVersion())}",
            build             : "${pipelineData.buildCode}",
            commit            : "${commitId}",
            branch            : "${pipelineData.branchStructure?.branchName}",
            nombre            : pipelineData.pipelineStructure.nombre,
            estado            : "${GlobalVars.GPL_STATE_RUNNING}",
            fechaCreacion     : new Date(),
            anteriorPipelineId: "${pipelineOrigId}",
            stages            : pipelineData.pipelineStructure.getStages(),
            path              : "${env.JOB_NAME}",
            runId             : "${env.BUILD_NUMBER}",
            //serverUrl         : "${env.JENKINS_URL}",
            serverUrl         : "${env.JNKMSV_DEVPORTAL_URL}",
            //jenkinsUserId     : "${GPL_USR}",
            //jenkinsUserToken  : "${GPL_PSW}"
            jenkinsUserId     : "${JNKMSV_USR}",
            jenkinsUserToken  : "${JNKMSV_PSW}"
        ]

        def response = sendRequestToGpl('POST', url, "", body, pipelineData, clientInfo)

        return response
    }
}
