import com.project.alm.*

def call(DeployStructure deployStructure, PomXmlStructure artifactPom, BranchStructure branchStructure) {
    printOpen("Init detect minor", EchoLevel.ALL)
    AncientVersionInfo ancientVersionInfo = new AncientVersionInfo(false)
    if (!artifactPom.itContainsSampleApp()) {
        String newReleaseArtifact = BmxUtilities.calculateArtifactId(artifactPom, branchStructure)

        //String lastVersionName = sh( returnStdout: true, script: "cf apps | grep ${newReleaseArtifact} | grep -v 'ancient_' | awk '{print \$1}'")
        String lastVersionName = sh(returnStdout: true, script: "cf apps | grep ${newReleaseArtifact} | awk '{print \$1}'")
        printOpen("El release Artifact es de ${newReleaseArtifact}", EchoLevel.ALL)
        printOpen("Minor anterior ${lastVersionName}", EchoLevel.ALL)

        //The blue && Green only applies to micros
        if (lastVersionName != null && lastVersionName != '') {
            /*
             * Eliminamos esto ya que te obliga a si tienes que subir un fix de una version que esta en beta
             * la consolides para proceder al deploy del fix */
            printOpen("Validando si dicho artefacto esta consolidado, debe tener todas las rutas mapeadas, no solo beta", EchoLevel.ALL)
            String mappedRoutes = sh(returnStdout: true, script: "cf app ${newReleaseArtifact} | grep 'routes:' | cut -d ':' -f 2 | xargs")
            boolean onlyBeta = RouteUtilities.isOnlyBetaUrls(mappedRoutes)
            if (onlyBeta) {
                ancientVersionInfo.isConsolidated = false
            }

            //Tenim que iterar sobre totes les apps antigues
            List oldVersions = Utilities.splitStringToList(lastVersionName, 'Discard')
            String artifactAncient = ''
            String existsAncient = ''

            oldVersions.each {
                if (it.equals(newReleaseArtifact) || it.equals("ancient_" + newReleaseArtifact)) {
                    if (it.contains('ancient_')) existsAncient = it
                    else artifactAncient = it
                }
            }

            printOpen("El ancientVersion es de ${artifactAncient}", EchoLevel.ALL)

            if (artifactAncient != '') {
                ancientVersionInfo.hasAncient = true
                String commandLineDetectAbsisBlue = "cf env ${artifactAncient} | grep -m 1 'ABSIS_BLUE' | awk '{ print \$2}'"
                String isBlue = sh(returnStdout: true, script: commandLineDetectAbsisBlue)

                if (isBlue != null && isBlue != '') {
                    ancientVersionInfo.isBlue = true
                } else {
                    ancientVersionInfo.isBlue = false
                }

                boolean ancientIsStarted = false
                boolean ancientIsAlive = false
                if (existsAncient != '') {
                    String aliveRoute = BmxUtilities.calculateRoute(artifactPom, branchStructure) + "." + deployStructure.url_int;
                    printOpen("Ruta de validacion: ${aliveRoute}", EchoLevel.ALL)
                    String mappedRoutesForAncient = sh(returnStdout: true, script: "cf app ${existsAncient} | grep 'routes:' | cut -d ':' -f 2 | xargs")
                    printOpen("Rutas obtenidas en ancient: ${mappedRoutesForAncient}", EchoLevel.ALL)
                    String runningState = sh(returnStdout: true, script: "cf app ${existsAncient} | grep 'requested state:' | cut -d ':' -f 2 | xargs")
                    printOpen("Running state en ancient: <${runningState}>", EchoLevel.ALL)
                    ancientIsStarted = (runningState != null ? runningState.contains("started") : false)
                    ancientIsAlive = RouteUtilities.isRouteAlive(aliveRoute, mappedRoutesForAncient);
                    printOpen("Validando si la version ancient esta dando servicio; Started: ${ancientIsStarted}, Rutas: isAliveVIP: ${ancientIsAlive}", EchoLevel.ALL)
                }
                //Si el ancient esta stopped o no tiene las rutas, podemos borrarlo
                if (!ancientIsAlive || !ancientIsStarted) {
                    String ancientArtifactName = 'ancient_' + newReleaseArtifact

                    //Validar si existe el ancient
                    //String existsAncient = sh( returnStdout: true, script: "cf apps | grep ${ancientArtifactName} | awk '{print \$1}'")
                    //No aplicamos el delete al nuevo artifact
                    if (existsAncient != null && existsAncient != '' && ancientVersionInfo.isConsolidated == true) {
                        List ancients = Utilities.splitStringToList(existsAncient, 'Discard')
                        ancients.each {
                            retry(GlobalVars.DEFAULT_RETRY_DELETE_APP_POLICY) {
                                sh "cf delete '${it}' -r -f"
                            }
                        }
                    }

                    if (ancientVersionInfo.isConsolidated == true) {
                        sh "cf rename '${newReleaseArtifact}' '${ancientArtifactName}'"
                        ancientVersionInfo.isRenamed = true
                    }
                } else {
                    ancientVersionInfo.isConsolidated = false
                    ancientVersionInfo.isRenamed = false
                }
            } else if (existsAncient != '') {//BUG NO TIENE PORQUE SER UN ANCIENT ESTO, SINO COINCIDE
                printOpen("Tenemos ancient pero no tenemos el artefacto. Esto es raro....!!!!", EchoLevel.ALL)
                ancientVersionInfo.hasAncient = true
                String commandLineDetectAbsisBlue = "cf env ${existsAncient} | grep -m 1 'ABSIS_BLUE' | awk '{ print \$2}'"
                String isBlue = sh(returnStdout: true, script: commandLineDetectAbsisBlue)


                if (isBlue != null && isBlue != '') {
                    ancientVersionInfo.isBlue = true
                } else {
                    ancientVersionInfo.isBlue = false
                }
                ancientVersionInfo.isRenamed = true
            }
        }
    }
    return ancientVersionInfo

}
