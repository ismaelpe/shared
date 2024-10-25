import com.caixabank.absis3.DeployStructure
import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.GarAppType
import com.caixabank.absis3.AbstractBuildpack
import com.caixabank.absis3.BmxUtilities
import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure


String getManifestEdenOrDev(String pathToOriginalManifest, String suffix) {
    String output = pathToOriginalManifest - 'manifest.yml'
    output = output + 'manifest-' + suffix + '.yml'
    return output

}

def call(PomXmlStructure artifactPom, DeployStructure deploy, PipelineData pipeline, boolean alternativePath) {

    printOpen("Generating manifest...", EchoLevel.INFO)

    String pathToOriginalManifest = artifactPom.getRouteToManifest()
    String appMajorVersion = artifactPom.getMajorVersion()
    String garAppType = pipeline.garArtifactType.name
    String appName = artifactPom.getApp(GarAppType.valueOfType(pipeline.garArtifactType.name))
    String domainApp = pipeline.domain
    String subDomain = pipeline.subDomain
    String company = pipeline.company
    String archVersion = artifactPom.archVersion
	
    Set<String> vcapsServiceIds = pipeline.vcapsServiceIds

    String pathManifestYml = CopyGlobalLibraryScript('manifest.yml',alternativePath)
    String pathManifestServicesYml = CopyGlobalLibraryScript('manifestServices.yml',alternativePath)
    String pathManifestEnvYml = CopyGlobalLibraryScript('manifestEnv.yml',alternativePath)
    String pathManifestEnvProxyYml = CopyGlobalLibraryScript('manifestEnvProxy.yml',alternativePath)
    String pathManifestServicesTmpYml = CopyGlobalLibraryScript('manifestServicesTmp.yml',alternativePath)

	String pathManifestJre = ""
	
	AbstractBuildpack javaNeeded = getJavaVersionType()
    if (javaNeeded.getAlternativeJre() != null) {
		pathManifestJre = CopyGlobalLibraryScript("${javaNeeded.getAlternativeJre()}",alternativePath)
    }

    String newMemory = GlobalVars.DEFAULT_MEMORY
    String javaOpts = GlobalVars.DEFAULT_JAVA_OPTS
    boolean noManifest = false

    boolean existManifestEdenDev = false
    boolean proxyInManifest = false

    try {
        String manifestOfApp = null
        try {
            if (deploy.environment.equals('eden')) {
                String manifestEden = getManifestEdenOrDev(pathToOriginalManifest, 'eden')
                manifestOfApp = sh(returnStdout: true, script: "cat ${manifestEden}")
                printOpen("Content of original the manifest-eden", EchoLevel.INFO)
                sh "cat ${manifestEden}"
                existManifestEdenDev = true
            } else if (deploy.environment.equals('dev')) {
                String manifestDev = getManifestEdenOrDev(pathToOriginalManifest, 'dev')                
                manifestOfApp = sh(returnStdout: true, script: "cat ${manifestDev}")
                printOpen("Content of original the manifest-dev", EchoLevel.INFO)
                sh "cat ${manifestDev}"
                existManifestEdenDev = true
            }
        } catch (Exception e) {
            printOpen("ERROR: ${e.getMessage()}", EchoLevel.DEBUG)
            existManifestEdenDev = false
            printOpen("No existe Manifest ni en Eden ni Eden... se procede a parsear el manifest por defecto", EchoLevel.DEBUG)
        }

        if (!existManifestEdenDev) {
            manifestOfApp = sh(returnStdout: true, script: "cat ${pathToOriginalManifest}")
            printOpen("Content of the original manifest", EchoLevel.DEBUG)
            printFile("${pathToOriginalManifest}", EchoLevel.DEBUG)
        }


        boolean servicesFound = false
		
		def manifestResources=manifestOfApp.tokenize('\n')
		
		
		for (int i=0;i<manifestResources.size();i++) {
			def x = manifestResources.getAt(i)
			
			if (x.contains('memory:')) {
				def valueInManifest=x.tokenize(' ')
				for (int j=0;j<valueInManifest.size();j++) {
					def y=valueInManifest.getAt(j)
					if (!y.contains('memory:')) {
						newMemory = y
                        printOpen("The new memory is ${y}", EchoLevel.DEBUG)
					}
				}
			}else if (x.contains('http_proxy:')) {
                printOpen("contiene proxy", EchoLevel.DEBUG)
				if (GarAppType.valueOfType(garAppType) == GarAppType.ARCH_MICRO) {
                    printOpen("contiene proxy y es de arquitectura ${garAppType}", EchoLevel.DEBUG)
					proxyInManifest = true
				}
			} else if (x.contains('services:')) {
                printOpen("services found", EchoLevel.DEBUG)
				servicesFound = true
			} else if (servicesFound && !x.contains('configserver') && !x.contains('absis-kafka-cloudbus')) {
                printOpen("The service is ${x}", EchoLevel.DEBUG)
				if ((GarAppType.valueOfType(garAppType) == GarAppType.DATA_SERVICE && x.indexOf('database') == -1) ||
						GarAppType.valueOfType(garAppType) != GarAppType.DATA_SERVICE)
					sh "echo '  ${x.trim()}' >>  ${pathManifestServicesTmpYml}"
				else printOpen("Ignoramos las cups de database ${x.trim()}", EchoLevel.DEBUG)
			} else {
                printOpen("The value ${x} is useless", EchoLevel.DEBUG)
			}	
		}

        if (pipeline.isDataserviceWithH2InMemoryDatabase) {

            printOpen("This is a dataservice with a configured H2. Skipping database services generation...", EchoLevel.INFO)

        } else if (GarAppType.valueOfType(garAppType) == GarAppType.DATA_SERVICE) {

            //Tenemos que vincular con las cups del backend
            //Ejemplo cbk-apps-demo-arqrun-database
            //
            //sh "echo '  cbk-apps-${x.trim()}-database' >>  ${pathManifestServicesTmpYml}"

            //if vcapsServiceIds=0 then we used DataSourceGenerator.groovy
            printOpen("vcapsServiceIds is " + vcapsServiceIds, EchoLevel.DEBUG)
            if (vcapsServiceIds.size() == 0) {
                vcapsServiceIds.add('cbk-apps-' + domainApp.toLowerCase() + '-' + appName.toLowerCase() + '-database')
                printOpen("vcapsServiceIds with only one datasource", EchoLevel.DEBUG)
            } else { // else we used DataSourceGeneratorV2.groovy
                printOpen("vcapsServiceIds with optional mutiple datasource", EchoLevel.DEBUG)
            }

			vcapsServiceIds.each {
				def cups = it
				
			   //if alternativePath=true the deploy is to ICP and we dont share secrets with the TST environment
			  if (!alternativePath && (deploy.environment.equals('eden') || deploy.environment.equals('dev'))) {
				//Exists the cup dev on the environment
				def status = sh(
						script: "cf service ${cups}-dev",
						returnStatus: true
				)
				if (status == 0) cups = "${cups}-dev"
			   }
			   sh "echo '  - ${cups}' >>  ${pathManifestServicesTmpYml}"
			}

        }

    } catch (Exception e) {
        printOpen("-----------excepcion -----" + e.getMessage(), EchoLevel.ERROR)

        if (deploy.environment.equals('eden')) {
            printOpen("File manifest-eden.yml Not Found", EchoLevel.INFO)
            existManifestEdenDev = true
        } else if (deploy.environment.equals('dev')) {
            printOpen("File manifest-dev.yml Not Found", EchoLevel.INFO)
            existManifestEdenDev = true
        }
        if (!existManifestEdenDev) {
            printOpen("File ${pathToOriginalManifest} Not Found", EchoLevel.INFO)
        }
        newMemory = GlobalVars.DEFAULT_MEMORY
        noManifest = true
    }

    if (archVersion?.contains("-SNAPSHOT")) {
        printOpen("We are using an SNAPSHOT of the architecture. Routing to TST will not be enabled", EchoLevel.INFO)
        deploy.springProfilesActive = deploy.calculateSpringCloudActiveProfiles(artifactPom.isApplicationWithNewHealthGroups())
    } else {
        printOpen("We are using a final version of the architecture. Routing to TST will be enabled via Spring profiles", EchoLevel.INFO)
        deploy.springProfilesActive = deploy.calculateSpringCloudActiveProfiles(garAppType, company, artifactPom.isApplicationWithNewHealthGroups())
    }
    sh "sed -i 's/#SPRING_PROFILES#/${deploy.springProfilesActive}/g' ${pathManifestYml}"
    sh "sed -i 's/#MEMORY#/${newMemory}/g' ${pathManifestYml}"
    sh "sed -i 's/#JAVA_OPTS#/${javaOpts}/g' ${pathManifestYml}"

    sh "echo ${deploy.getEnvVariables(garAppType, appName,appMajorVersion, domainApp, subDomain, company)} >> ${pathManifestEnvYml} "
	
    if (javaNeeded.getAlternativeJre() != null) sh "cat ${pathManifestJre} >> ${pathManifestEnvYml} "

    sh "cat ${pathManifestEnvYml} >> ${pathManifestYml}"
    //Opcional si usa proxy o no

    if (proxyInManifest) sh "cat ${pathManifestEnvProxyYml} >> ${pathManifestYml}"
    //Para los servicios deberiamos o no consultar la info del manifest

    sh "cat ${pathManifestServicesYml} >> ${pathManifestYml}"
    sh "cat ${pathManifestServicesTmpYml} >> ${pathManifestYml}"

    printOpen("The manifest has been updated.", EchoLevel.INFO)
    printFile(pathManifestYml, EchoLevel.DEBUG)

    return pathManifestYml
}
