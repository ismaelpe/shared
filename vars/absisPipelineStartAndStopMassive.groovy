import com.project.alm.AppMassive

import groovy.transform.Field
import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.ICPAppResources
import com.project.alm.PipelineData


@Field Map pipelineParams

@Field PipelineData pipelineData

@Field boolean successPipeline = false

@Field String icpEnv = "${environmentParam}"
@Field String appList = "${appnameParam}"
@Field String center = "${centerParam}"
@Field String stableOrNew = "${stableOrNewParam}"
@Field String scaleCPUCores = "${scaleCPUCoresParam}"
@Field String scaleMemory = "${scaleMemoryParam}"
@Field String scaleNumInstances = "${scaleNumInstancesParam}"
@Field String jvmConfig = "${jvmConfigParam}"
@Field String useCatalogSize = "${useCatalogSizeParam}"
@Field String useProSize = "${useProSizeParam}"
@Field String userId = "${userId}"?.trim() ? "${userId}" : "AB3ADM"
@Field String numberParalel = "${numberParalelParam}"

@Field String action = "${actionParam}"
@Field boolean startAndStop = false

@Field ArrayList<AppMassive> apps = null
@Field int numThreads = 0
@Field ICPAppResources icpResources = null
@Field String sizeCPU = "M"
@Field String sizeMEM = "M"
@Field String sizeREP = "M"
@Field String icpEnvResources
@Field LinkedHashMap<String, GString> scalingMap
@Field boolean resize
/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */


def call(Map pipelineParameters) {
    pipelineParams = pipelineParameters

    // las variables que se obtienen como parametro del job no es necesario
    // redefinirlas, se hace por legibilidad del codigo
    successPipeline = false
    icpEnv = params.environmentParam
    center = params.centerParam
    stableOrNew = params.stableOrNewParam
    scaleCPUCores = params.scaleCPUCoresParam
    scaleMemory = params.scaleMemoryParam
    scaleNumInstances = params.scaleNumInstancesParam
    jvmConfig = params.jvmConfigParam
    useCatalogSize = params.useCatalogSizeParam
    useProSize = params.useProSizeParam
    userId = params.userId?.trim() ? params.userId : "AB3ADM"
    action = params.actionParam
    numberParalel = params.numberParalelParam

    startAndStop = false


    pipeline {
        agent { node(absisJenkinsAgent(pipelineParams)) }
        options {
            gitLabConnection('gitlab')
            buildDiscarder(logRotator(numToKeepStr: '10'))
            timestamps()
            timeout(time: 3, unit: 'HOURS')
        }
        //Environment sobre el qual se ejecuta este tipo de job
        environment {
            GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
            JNKMSV = credentials('JNKMSV-USER-TOKEN')
            ICP_CERT = credentials('icp-alm-pro-cert')
            ICP_PASS = credentials('icp-alm-pro-cert-passwd')
            http_proxy = "${GlobalVars.proxyCaixa}"
            https_proxy = "${GlobalVars.proxyCaixa}"
            proxyHost = "${GlobalVars.proxyCaixaHost}"
            proxyPort = "${GlobalVars.proxyCaixaPort}"
        }
        stages {
            stage("init") {
                steps {
                    initStep()
                }
            }
            stage("get-app-icp") {
                steps {
                    getAppIcpStep()
                }
            }
            stage("restart-app-icp") {
                steps {
                    restartAppIcpStep()
                }
            }
            stage('Parallel For Loop') {
                steps {
                    script {
                        int numThreads = numberParalel.toInteger()
                        for (int i = 0; i < apps.size(); i += numThreads) {
                            def tasks = [:]
                            def chunk = apps.subList(i, Math.min(i + numThreads, apps.size()))
                            for (int j = 0; j < chunk.size(); j++) {
                                def app = chunk[j]
                                def workDir = "workspace/${app.getAppName()}"
                                // Define the working directory based on the app name
                                tasks[app.getAppName()] = {
                                    try {
                                        sh "mkdir -p ${workDir}"
                                        dir(workDir) {
                                            printOpen("Aqui llega? ", EchoLevel.DEBUG)
                                            if (resize) {
                                                toPararel(app, icpEnvResources, sizeMEM, sizeCPU, sizeREP, scalingMap, icpResources, workDir)
                                            } else {
                                                toparalell2(app, scalingMap,workDir)
                                            }
                                        }
                                        app.setEstado("Ok")
                                    }catch (Exception e){
                                        printOpen("Error en la app ${app.getAppName()} "+e.printStackTrace(), EchoLevel.ALL)
                                        app.setEstado("KO")
                                    }
                                    finally {
                                        sh "rm -rf ${workDir}"
                                    }

                                }
                            }

                            // Execute all tasks in parallel
                            parallel tasks
                        }
                    }
                }
            }

        }
        post {
            success {
                endPipelineSuccessStep()
            }
            failure {
                endPipelineFailureStep()
            }
            always {
                endPipelineAlwaysStep()
            }
        }
    }

}


/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Stage 'initStep'
 */
def initStep() {
    initGlobalVars(pipelineParams)

}

/**
 * Stage 'getAppIcpStep'
 */
def getAppIcpStep() {
// Fomato lista SRV.DS.micro_1,SRV.DS.micro_2
    apps = new ArrayList<AppMassive>()
    def appSplit = appList.split(",")
    appSplit.each { String appname ->
        AppMassive appMassive = createAppMassive(appname)
        currentBuild.displayName = "${action}_${appMassive.getAppName()} of ${icpEnv} and the namespace ${appMassive.getNamespace()} and the center ${center}"
        try {
            printOpen("Get App ", EchoLevel.ALL)
            Map valuesDeployed = null
            valuesDeployed = getLastAppInfoICP(icpEnv, appMassive.getAppName(), appMassive.getNamespace(), center)
            printAppICP(valuesDeployed)
            appMassive.setValuesDeployed(valuesDeployed)
        } catch (Exception e) {
            throw e
        }
        apps.add(appMassive)

    }
}

private static AppMassive createAppMassive(String appname) {
    AppMassive appMassive = new AppMassive()
    def splitPunto = appname.split("\\.")
    def splitGuion = splitPunto[2].split("_")
    appMassive.setGarApp(splitGuion[0])
    appMassive.setAppName(splitGuion[0] + splitGuion[1])
    appMassive.setGarType(splitPunto[0] + "." + splitPunto[1])

    calculateNamespace(appMassive)

    return appMassive
}

private static void calculateNamespace(AppMassive appMassive) {
    switch (appMassive.getGarType()) {
        case "SRV.MS":
            appMassive.setNamespace("APP")
            appMassive.setIcpNamespace(GlobalVars.ICP_APP_APPS)
            appMassive.setIcpNamespaceId(GlobalVars.ICP_APP_ID_APPS)
            break
        case "SRV.DS":
            appMassive.setNamespace("APP")
            appMassive.setIcpNamespace(GlobalVars.ICP_APP_APPS)
            appMassive.setIcpNamespaceId(GlobalVars.ICP_APP_ID_APPS)
            break
        case "ARQ.MIA":
            appMassive.setNamespace("ARCH")
            appMassive.setIcpNamespace(GlobalVars.ICP_APP_ARCH)
            appMassive.setIcpNamespaceId(GlobalVars.ICP_APP_ID_ARCH)
            appMassive.setIsArchProject(true)
            break
        default:
            appMassive.setNamespace("APP")
            appMassive.setIcpNamespace(GlobalVars.ICP_APP_APPS)
            appMassive.setIcpNamespaceId(GlobalVars.ICP_APP_ID_APPS)
    }
}

/**
 * Stage 'restartAppIcpStep'
 */

def restartAppIcpStep() {
    try {
        printOpen("Restart Apps", EchoLevel.ALL)

        scalingMap = [
            scaleCPUCores    : "${scaleCPUCores}",
            scaleMemory      : "${scaleMemory}",
            scaleNumInstances: "${scaleNumInstances}"
        ]

        if (action == "START") {
            startAndStop = true
        }

        if ("yes".equals(useCatalogSize) || (!"NO".equals(scaleCPUCores) || !"NO".equals(scaleMemory) || !"DEFAULT".equals(scaleNumInstances)) && (env.CATMSV_SIZE!=null && "true".equals(env.CATMSV_SIZE))) {
            icpResources = null
            sizeCPU = "M"
            sizeMEM = "M"
            sizeREP = "M"

            /**
             * Si ha seleccionado hacer el scaling de algo no tiene que usar los tamaños del catalogo
             */
            if (!"NO".equals(scaleCPUCores) || !"NO".equals(scaleMemory) || !"DEFAULT".equals(scaleNumInstances)) {
                useCatalogSize = "no"
            }

            if (!"NO".equals(scaleCPUCores)) {
                sizeCPU = scaleCPUCores
            }
            if (!"NO".equals(scaleMemory)) {
                sizeMEM = scaleMemory
            }
            if (!"DEFAULT".equals(scaleNumInstances)) {
                sizeREP = scaleNumInstances
            }

            icpEnvResources = icpEnv
            /**
             * Vamos a usar los tamaños del catalogo de PRO para realizar el deploy sea cual sea el entorno
             */
            if ("yes".equals(useProSize)) {
                icpEnvResources = "PRO" //Usaremos recursos de PRO
                printOpen("El tamaño que queremos usar es el de PRO no el del entorno donde estamos", EchoLevel.ALL)
            } else {
                printOpen("El entorno del tamaño a usar es el del entorno ${icpEnv}", EchoLevel.ALL)
            }

            //   numThreads = numberParalel.toInteger() > apps.size() ? apps.size() : numberParalel.toInteger()


            //   for (AppMassive app : apps) {
            /**
             * Consultamos el tamaño del micro en catalogo o usamos los tamaños definidos para el entorno
             */
            //          toPararel(app, icpEnvResources, sizeMEM, sizeCPU, sizeREP, scalingMap, icpResources)
            //   }

            resize = true
        } else {
            resize = false
            /**
             * Aplicamos el start & stop
             */

            // for (AppMassive app : apps) {
            /**
             * Consultamos el tamaño del micro en catalogo o usamos los tamaños definidos para el entorno
             */
            //   toparalell2(app, scalingMap)
            //}
        }

    } catch (Exception e) {

        throw e
    }
}

private void toparalell2(AppMassive app, LinkedHashMap<String, GString> scalingMap, String workspace) {
    String garApp = app.getGarApp()
    String garType = app.getGarType()
    String namespace = app.getNamespace()
    Map valuesDeployed = app.getValuesDeployed()
    String appname = app.getAppName()
    printOpen("No se desea escalar nada, no hace falta consultar el catalogo", EchoLevel.DEBUG)
    startAndStopApp(valuesDeployed, appname, center, namespace, icpEnv, startAndStop, stableOrNew, garApp, jvmConfig, scalingMap,workspace)
    absisPipelineStageCloneToOcp(appname - garApp + '.0.0', garApp + '-micro', garApp, namespace, icpEnv, userId, garType, userId, 'false', 'true', action)
}


private void toPararel(AppMassive app, String icpEnvResources, String sizeMEM, String sizeCPU,
                       String sizeREP, LinkedHashMap<String, GString> scalingMap, ICPAppResources icpResources, String workspace) {
    String garApp = app.getGarApp()
    String appname = app.getAppName()
    String garType = app.getGarType()
    String icpNamespace = app.getNamespace()
    Boolean isArchProject = app.getIsArchProject()
    String namespace = app.getNamespace()
    Map valuesDeployed = app.getValuesDeployed()

    if ("yes".equals(useCatalogSize)) {
        printOpen("Se deben usar los tamaños del catalogo", EchoLevel.DEBUG)
        icpResources = generateICPResources(null, icpEnvResources, true, garApp, null, garType, appname - garApp, icpNamespace)

    } else {
        printOpen("No se debe usar los tamaños del catalogo", EchoLevel.DEBUG)
        icpResources = getSizesFromCatalog(icpNamespace, garType, icpEnvResources, isArchProject, sizeMEM, sizeCPU, sizeREP)
    }

    app.setICPAppResources(icpResources)
    /**
     * Tenemos que aplicar las restricciones de tamaño para poder ya que sino es PRO ciertos valores no se pueden permitir
     */
    //icpResources=restrictSizesFromCatalog(icpResources,icpEnv)

    if (!"NO".equals(scaleCPUCores) || "yes".equals(useCatalogSize)) {
        scaleCPUCores = icpResources.cpuSize
    }
    if (!"NO".equals(scaleMemory) || "yes".equals(useCatalogSize)) {
        scaleMemory = icpResources.memSize
    }
    if (!"DEFAULT".equals(scaleNumInstances) || "yes".equals(useCatalogSize)) {
        scaleNumInstances = icpResources.replicasSize
    }

    scalingMap = [
        scaleCPUCores    : "${scaleCPUCores}",
        scaleMemory      : "${scaleMemory}",
        scaleNumInstances: "${scaleNumInstances}"
    ]
    /**
     * Aplicamos el start & stop
     */
    printOpen("Se desea escalar, no hace falta consultar el catalogo ${scalingMap} ${icpResources}", EchoLevel.INFO)
    startAndStopApp(valuesDeployed, appname, center, namespace, icpEnv, startAndStop, stableOrNew, garApp, jvmConfig, scalingMap, app.getICPAppResources(), app.getGarType(), workspace)
    //Se tiene que clonar OCP
    absisPipelineStageCloneToOcp(appname - garApp + '.0.0', garApp + '-micro', garApp, icpNamespace, icpEnv, userId, garType, userId, 'false', 'true', action)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    printOpen("El estado de las apps son: ", EchoLevel.INFO)
    for(AppMassive app: apps){
        printOpen("El estado de "+app.getAppName()+" es: "+app.getEstado(), EchoLevel.INFO)
    }
    cleanWorkspace()
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    successPipeline = true
    printOpen("Is pipeline successful? ${successPipeline}", EchoLevel.INFO)

}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    successPipeline = false
    printOpen("Is pipeline unsuccessful? ${successPipeline}", EchoLevel.ERROR)
}

