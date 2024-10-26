import groovy.transform.Field
import com.project.alm.*
import com.project.alm.KpiAlmEvent
import groovy.transform.Field
import com.project.alm.KpiAlmEventStage
import com.project.alm.KpiAlmEventOperation
import org.apache.commons.csv.CSVFormat
import groovy.json.JsonSlurperClassic
import java.util.ArrayList

@Field Map pipelineParams

@Field int numGitProjectsToMigrate
@Field String getInfoFromLocal
@Field Strin onlyApp

@Field String scriptsPath
@Field Strin buildCode

@Field String[] recordsOpenServices
@Field String[] listaWebhooksModificados
@Field Map statisticsApp
@Field Map statisticsAppAplicados

//Pipeline unico que construye todos los tipos de artefactos
//Recibe los siguientes parametros
//type: String con el tipo de artifact el repo del qual ha lanzado el PipeLine
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    numGitProjectsToMigrate = params.numGitProjectsToMigrateParam as Integer
    degetInfoFromLocal = params.getInfoFromLocalParam
    onlyApp = params.onlyAppParam

    scriptsPath = ''
    buildCode = env.BUILD_TAG

    recordsOpenServices = []
    listaWebhooksModificados = []
    statisticsApp = [:]
    statisticsAppAplicados = [:]

    pipelineOS.withoutSCM(pipelineParams) {
        try {
            printOpen("Numero de proyectos a migrar ${numGitProjectsToMigrateParam}", EchoLevel.INFO)

            stageOS('init-pipeline') {
                initGlobalVars()
                //De momento esto funciona
                def csvAlm3 = null
                def fileOutput = null

                if ('true'.equals(getInfoFromLocal)) {
                    fileOutput = CopyGlobalLibraryScript('GAR_APL_ALM.csv')
                    printOpen("El fichero es de ${fileOutput}", EchoLevel.INFO)
                }else {
                    csvAlm3 = 'https://eideswasp.svb.digitalscale.es/apw61/idegar/api/interfaz/v1/descargaFichero/00001/GAR_APL_ALM.csv'
                    fileOutput = CopyGlobalLibraryScript('', null, 'garAplAlm3.csv', EchoLevel.INFO)
                    sh(script: "curl ${csvAlm3} -X GET --output ${fileOutput} ", returnStdout:true)
                }

                def records = readCSV file: fileOutput , format: CSVFormat.DEFAULT.withDelimiter(';' as char)
                def nameApp
                def typeApp
                def entidad
                def domain
                int i = 0
                registroParcial = null
                records.each {
                    nameApp = it[0]
                    typeApp = it[1]
                    entidad = it[5].toLowerCase()
                    domain = it[6]
                    if (it[1].startsWith('SRV.') || it[1].startsWith('ARQ.')) {
                        registroParcial = [:]
                        registroParcial['nameApp'] = nameApp
                        registroParcial['typeApp'] = typeApp
                        registroParcial['entidad'] = entidad
                        registroParcial['domain'] = domain
                        recordsOpenServices[i] = registroParcial
                        i++
                    }
                }
            }
            stageOS('get-GAR-Project') {
            }
            stageOS('migrate-each-project') {
                withCredentials([
                    usernamePassword(credentialsId: 'JNKMSV-USER-TOKEN', usernameVariable: 'JNKMSV_USR', passwordVariable: 'JNKMSV_PSW')
                    ]) {
                    withCredentials([
                        usernamePassword(credentialsId: 'GITLAB_CREDENTIALS', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD'),
                        string(credentialsId: 'GITLAB_API_SECRET_TOKEN', variable: 'GITLAB_API_TOKEN')
                        ]) {
                        def nameApp
                        def typeApp
                        def entidad
                        def domain
                        int i = 0
                        def empresa
                        def tipoProyecto
                        def subtipo
                        def parcialPath = ''
                        def registroParcial = null
                        def suffix = ''
                        recordsOpenServices.each {
                            //Por cada uno que aplique tenemos que hacer el update de la redirectora si aplica
                            printOpen("El valor es de ${it}", EchoLevel.INFO)
                            registroParcial = it
                            parcialPath = ''
                            nameApp = registroParcial['nameApp']
                            domain = registroParcial['domain']
                            entidad = registroParcial['entidad']
                            parcialPath = null

                            if (registroParcial['typeApp'].startsWith('SRV.')) {
                                tipoProyecto = 'apps'
                                if (registroParcial['typeApp'].startsWith('SRV.MS')) {
                                    subtipo = 'service'
                                    suffix = '-micro'
                                } else if (registroParcial['typeApp'].startsWith('SRV.DS')) {
                                    subtipo = 'data-service'
                                    suffix = '-micro'
                                } else if (registroParcial['typeApp'].startsWith('SRV.BFF')) {
                                    subtipo = 'bff'
                                    suffix = '-micro'
                                } else if (registroParcial['typeApp'].startsWith('SRV.CFG')) {
                                    subtipo = 'conf'
                                    suffix = '-conf'
                                } else {
                                    subtipo = 'common'
                                    suffix = '-lib'
                                }
                                parcialPath = '/' + tipoProyecto + '/' + entidad + '/' + subtipo + '/' + domain + '/' + nameApp + suffix
                            }
                            if (registroParcial['typeApp'].startsWith('ARQ.') && 'false'.equals(onlyApp)) {
                                tipoProyecto = 'arch'

                                if (registroParcial['typeApp'].startsWith('ARQ.MIA')) {
                                    suffix = '-micro'
                                } else if (registroParcial['typeApp'].startsWith('ARQ.LIB') || registroParcial['typeApp'].startsWith('ARQ.MAP')) {
                                    suffix = '-lib'
                                } else if (registroParcial['typeApp'].startsWith('ARQ.CFG')) {
                                    suffix = '-conf'
                                }
                                parcialPath = '/' + tipoProyecto + '/' + domain + '/' + nameApp + suffix
                            }
                            if (parcialPath != null) {
                                increaseStatistics(statisticsApp, registroParcial['typeApp'])
                                String rutaProyecto = 'cbk/alm/services' + parcialPath
                                printOpen("El path es de ${rutaProyecto} ${rutaProyecto.substring(4)}", EchoLevel.INFO)

                                withCredentials([
                                    usernamePassword(credentialsId: 'GITLAB_CREDENTIALS', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD'),
                                    string(credentialsId: 'GITLAB_API_SECRET_TOKEN', variable: 'GITLAB_API_TOKEN')
                                ]) {
                                    if (i <= numGitProjectsToMigrate) {
                                        def projectPathUrlEncoded = URLEncoder.encode(rutaProyecto, 'UTF-8')
                                        try {
                                            def projectInfo = httpRequest consoleLogResponseBody: true,
                                                contentType: 'APPLICATION_JSON',
                                                httpMode: 'GET',
                                                ignoreSslErrors: true,
                                                customHeaders: [[name: 'Private-Token', value: "${GITLAB_API_TOKEN}"], [name: 'Accept', value: 'application/json']],
                                                url: "${GlobalVars.gitlabApiDomain}${projectPathUrlEncoded}",
                                                httpProxy: 'http://proxyserv.svb.digitalscale.es:8080',
                                                validResponseCodes: '200:300'

                                            def json = new JsonSlurperClassic().parseText(projectInfo.content)
                                            def projectId = json.id

                                            boolean isRedirected = rebuildGitProject.validateWebhookToJnkmsvAndSet(projectId, rutaProyecto.substring(4) )

                                            if (isRedirected) {
                                                i++
                                                increaseStatistics(statisticsAppAplicados, registroParcial['typeApp'])
                                                listaWebhooksModificados.add(registroParcial['typeApp'] + '.' + registroParcial['nameApp'])
                                                printOpen("Redirigido ahora ${rutaProyecto} ${rutaProyecto.substring(4)}", EchoLevel.INFO)
                                            }else {
                                                printOpen("NO Redirigido ahora ${rutaProyecto} ${rutaProyecto.substring(4)}", EchoLevel.INFO)
                                            }
                                        }catch (error) {
                                            printOpen("Error con el acceso al micro ${error}", EchoLevel.ERROR)
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }
            }
            stageOS('print-result') {
                printOpen("Result Total Sistema:  ${statisticsApp}", EchoLevel.INFO)
                printOpen("Result Aplicados:  ${statisticsAppAplicados}", EchoLevel.INFO)
            }
        }catch (err) {
            throw err
        }finally {
            cleanWorkspace()
        }
    }
}

def increaseStatistics(def mapInfo, def typeApp) {
    if (mapInfo[typeApp] == null) {
        mapInfo[typeApp] = 1
    }else {
        mapInfo[typeApp] = mapInfo[typeApp] + 1
    }
}
