/**
 *  Este script sirve para desplegar en bluemix el artefacto
 *
 *  Parametros:
 *    usr | String | Obligatorio | usuario de bluemix
 *    pwd | String | Obligatorio | password del usuario de bluemix
 *    distFolder | String | Opcional - "build" por defecto | La carpeta del
 *      proyecto que se desplegará
 *    deployUrl  | String | Opcional - "null" por defecto en cuyo caso se
 *      generará una URL a partir del nombre del repositorio | URL que será
 *      donde quede desplegado el artefacto
 */

/*
  deployBluemix {
    usr = ""
    pwd = ""
    org = ""
    space = ""
    entorn = ""
    apiBmx = ""
    deployUrl = ""
    artifactType = ""  // SERVICE | APP
    distFolder = ""
    buildpack = ""
    memory = ""
    disk = ""
  }
*/


import com.project.alm.EchoLevel
import com.project.alm.GlobalVars

def call(body) {

    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    // set defaults for arguments
    config.distFolder = config.distFolder ?: "build"
    config.entorn = config.entorn ?: "TST"
    config.memory = config.memory ?: "64M"
    config.disk = config.disk ?: "256M"

    def appName = gitRepoNameGet()

    def domainName = "${GlobalVars.domainTst}"
    if (config.entorn == 'PRE') {
        domainName = "${GlobalVars.domainPre}"
    } else if (config.entorn == 'PRO') {
        domainName = "${GlobalVars.domainPro}"
    }

    if (config.deployUrl == null) {
        // obtenemos el nombre del repositorio y calculamos la URL para el despliegue
        config.deployUrl = "${appName}"
        if (config.entorn == 'TST') {
            def normalizedBranchName = env.BRANCH_NAME.replaceAll(/#/, '').replaceAll(/\//, '-')
            if (normalizedBranchName != 'master') {
                config.deployUrl = "${appName}-${normalizedBranchName}"
                if (config.artifactType == "SERVICE") {
                    config.deployUrl = "beta.${appName}.tst"
                }
            }
        }
    }

    // Construir manifest.yml o passar-ho tot per paràmetre
    // ---
    // applications:
    // - name: REPO_NAME
    //   memory: 48M
    //   disk_quota: 64M
    //   buildpack: https://gitlab-ci-token:gy2Mzz5PovhjgdxeqJy2@git.svb.digitalscale.es/cbk/cf/alm/nginx-buildpack.git
    //   path: build
    //   routes:
    //   - route: container.tst.ca.project.com/repo_name
    //
    // cf push ${deployUrl} -p ${distFolder} -m 48M -k 64M -b staticfile_buildpack -d tst.ca.project.com -n container --route-path app2

/*
printOpen("############# DATOS BLUEMIX ##################################", EchoLevel.ALL)
      printOpen("org ${config.org}", EchoLevel.ALL)
      printOpen("space ${config.space}", EchoLevel.ALL)
      printOpen("entorn ${config.entorn}", EchoLevel.ALL)
      printOpen("apiBmx ${config.apiBmx}", EchoLevel.ALL)
      printOpen("deployUrl ${config.deployUrl}", EchoLevel.ALL)
      printOpen("artifactType ${config.artifactType}", EchoLevel.ALL)
      printOpen("distFolder ${config.distFolder}", EchoLevel.ALL)
      printOpen("buildpack ${config.buildpack}", EchoLevel.ALL)
      printOpen("memory ${config.memory}", EchoLevel.ALL)
      printOpen("disk ${config.disk}", EchoLevel.ALL)
printOpen("#########################################################################", EchoLevel.ALL)
*/

    sh "cf api --skip-ssl-validation ${config.apiBmx}"
    retry(GlobalVars.CF_LOGIN_MAX_RETRIES) {
        timeout(time: GlobalVars.CF_LOGIN_TIMEOUT, unit: 'SECONDS') {
            sh "cf login --skip-ssl-validation -u ${config.usr} -p \'${config.pwd}\' -o ${config.org} -s ${config.space}"
        }
    }
    if (config.artifactType == "APP") {
        if (config.deployUrl != appName) {
            retry(GlobalVars.CF_PUSH_MAX_RETRIES) {
                sh "cf push ${config.deployUrl} -t ${GlobalVars.CF_PUSH_TIMEOUT} -p ${config.distFolder} -m ${config.memory} -k ${config.disk} -b ${GlobalVars.bpGeneric} -d ${domainName} -n ${config.deployUrl}"
            }
        } else {
            retry(GlobalVars.CF_PUSH_MAX_RETRIES) {
                sh "cf push ${appName} -t ${GlobalVars.CF_PUSH_TIMEOUT} -p ${config.distFolder} -m ${config.memory} -k ${config.disk} -b ${GlobalVars.bpApps} -d ${domainName} -n ${GlobalVars.containerAppName} --route-path ${appName}"
            }
        }
    } else {
        if (config.buildpack == null) {
            config.buildpack = GlobalVars.bpGeneric
        }
        retry(GlobalVars.CF_PUSH_MAX_RETRIES) {
            sh "cf push ${appName} -t ${GlobalVars.CF_PUSH_TIMEOUT} -p ${config.distFolder} -m ${config.memory} -k ${config.disk} -b ${config.buildpack} -d ${domainName} -n ${config.deployUrl}"
        }
    }

    def deployUrl = "http://${config.deployUrl}.${domainName}"
    printOpen("***************************  DEPLOYED TO *************************************", EchoLevel.ALL)
    echo deployUrl
    printOpen("***************************  DEPLOYED TO *************************************", EchoLevel.ALL)

    return deployUrl;

}
