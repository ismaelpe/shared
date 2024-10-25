/**
 * Pipeline para la documentacion de arquitectura canal SPA
 *
 * Se encuentra documentado aqui https://git.svb.lacaixa.es/absis2cloud/canal/blob/master/arquitectura/20-elementos-arquitectura/30-application-lifecycle-management-arq/continuous-integration/pipelines/documentacion-canal.md
 */

import com.project.alm.EchoLevel
import com.project.alm.GlobalVars

def call(domain) {
    def entorno_param = "${environmentParam}"
    pipeline {
        agent {
            docker {
                // FIXME: ¿Por que tenemos esta Imagen docker Referenciando a Canal?
                image "alm/alm-canal:latest"
                args "$env.DOCKER_AGENT_MVN_LOCAL_REPO"
                registryUrl "$env.DOCKER_REGISTRY_URL"
            }
        }

        stages {

            stage('Install Dependencies') {
                steps {
                    projectDependenciesInstall_Doc()
                }
            }

            stage('Build') {
                steps {
                    projectBuild_Doc()
                }
            }

            stage('Deploy') {
                environment {
                    GPL = credentials('IDECUA-JENKINS-USER-TOKEN')
                    ICP_CERT = credentials('icp-alm-pro-cert')
                    ICP_PASS = credentials('icp-alm-pro-cert-passwd')                    
                    BLUEMIX_CREDENTIALS = credentials('BLUEMIX_CREDENTIALS')
                    http_proxy = "${GlobalVars.proxyCaixa}"
                    https_proxy = "${GlobalVars.proxyCaixa}"
                    proxyHost = "${GlobalVars.proxyCaixaHost}"
                    proxyPort = "${GlobalVars.proxyCaixaPort}"
                }

                steps {
                    script {

                        def sc_usr = BLUEMIX_CREDENTIALS_USR
                        def sc_pwd = BLUEMIX_CREDENTIALS_PSW

                        def org_cd1 = GlobalVars.BMX_TST_ORG_CD1
                        def org_cd2 = GlobalVars.BMX_TST_ORG_CD2
                        def space_entorn = GlobalVars.BMX_TST_SPACE
                        def apiBmx_cd1 = GlobalVars.blueMixUrl_CD1_TST
                        def apiBmx_cd2 = GlobalVars.blueMixUrl_CD2_TST

                        if (entorno_param == 'TST') {
                            org_cd1 = GlobalVars.BMX_TST_ORG_CD1
                            org_cd2 = GlobalVars.BMX_TST_ORG_CD2
                            space_entorn = GlobalVars.BMX_TST_SPACE
                            apiBmx_cd1 = GlobalVars.blueMixUrl_CD1_TST
                            apiBmx_cd2 = GlobalVars.blueMixUrl_CD2_TST
                        } else if (entorno_param == 'PRE') {
                            org_cd1 = GlobalVars.BMX_PRE_ORG_CD1
                            org_cd2 = GlobalVars.BMX_PRE_ORG_CD2
                            space_entorn = GlobalVars.BMX_PRE_SPACE
                            apiBmx_cd1 = GlobalVars.blueMixUrl_CD1_PRE
                            apiBmx_cd2 = GlobalVars.blueMixUrl_CD2_PRE
                        } else if (entorno_param == 'PRO') {
                            org_cd1 = GlobalVars.BMX_PRO_ORG_CD1
                            org_cd2 = GlobalVars.BMX_PRO_ORG_CD2
                            space_entorn = GlobalVars.BMX_PRO_SPACE
                            apiBmx_cd1 = GlobalVars.blueMixUrl_CD1_PRO
                            apiBmx_cd2 = GlobalVars.blueMixUrl_CD2_PRO
                        }

                        deployBluemix_Doc {
                            usr = sc_usr
                            pwd = sc_pwd
                            org = org_cd1
                            space = space_entorn
                            entorn = entorno_param
                            apiBmx = apiBmx_cd1
                            deployUrl = domain ?: "servicios-docs"
                            artifactType = false
                            distFolder = "_book"
                            buildpack = GlobalVars.bpGeneric
                        }

                        deployBluemix_Doc {
                            usr = sc_usr
                            pwd = sc_pwd
                            org = org_cd2
                            space = space_entorn
                            entorn = entorno_param
                            apiBmx = apiBmx_cd2
                            deployUrl = domain ?: "servicios-docs"
                            artifactType = false
                            distFolder = "_book"
                            buildpack = GlobalVars.bpGeneric
                        }
                    }
                }
            } // stages
        }
        post {
            always {
                script {
                    if (getContext(hudson.FilePath)) {
                        printOpen("Cleaning pipeline workspace...", EchoLevel.INFO)
                        cleanWs()

                    } else {

                        printOpen("No hudson.FilePath context available so we won't try to clean the workspace", EchoLevel.INFO)

                    }
                }
            }

            failure {
                jobStatusMailNotification_Doc('Failure')
            }
        }

    } // pipeline
} // def
