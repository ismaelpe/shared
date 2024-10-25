package com.project.alm

import org.apache.tools.ant.taskdefs.Echo

import java.util.regex.Matcher

class NexusUtils {

    private final def scriptContext

    NexusUtils(scriptContext) {
        this.scriptContext = scriptContext
    }

    String getLastVersionNumber(String groupId, String artifactId, Integer majorVersion) {
        return getLastVersionNumber(groupId, artifactId, majorVersion, GlobalVars.NEXUS_PUBLIC_REPO_NAME)
    }

    String getLastVersionNumber(String groupId, String artifactId, String repositoryName) {
        return getLastVersionNumber(groupId, artifactId, null, repositoryName)
    }

    String getLastVersionNumber(String groupId, String artifactId, Integer majorVersion, String repositoryName) {

        return getLastVersionNumber(groupId, artifactId, majorVersion, repositoryName, null)
    }
	
	boolean exists(String groupId, String artifactId, String version) {
		String avoidCaching = "?\$(date +%s)"
		groupId = groupId.replace(".", "/")
		
		try {
            String metadata = ""

            scriptContext.withCredentials([scriptContext.usernamePassword(credentialsId: 'icpcoucxba3msje01', passwordVariable: 'MVN_PASS', usernameVariable: 'MVN_USR')]) {
                String url = "https://$scriptContext.env.MVN_USR:$scriptContext.env.MVN_PASS@$GlobalVars.NEXUS_URL_BASE/artifactory/$GlobalVars.NEXUS_PUBLIC_REPO_NAME/$groupId/$artifactId/maven-metadata.xml$avoidCaching"

                metadata = scriptContext.sh(
                    script: 'curl --insecure -s "' + url + '" --connect-timeout 90',
                    returnStdout: true
                )
            }
			
			return metadata.contains("<version>"+version+"</version>");

		} catch (err) {
            scriptContext.printOpen("NexusUtils.exists threw exception while retrieving maven-metadata.xml from ${groupId}:${artifactId}", EchoLevel.ERROR)
            scriptContext.printOpen(err.getMessage(), EchoLevel.ERROR)
			return false
		}
	}

    boolean exists(String artifactFullName) {

        def artifact = artifactFullName.split(":")
        def hasClassifier = artifactFullName.count(":") > 3
        def hasExtension = artifactFullName.count(":") > 2
        def groupId = artifact[0]
        def artifactId = artifact[1]
        def artifactExtension = hasExtension ? artifact[2] : "jar"
        def artifactClassifier = hasClassifier ? "-"+artifact[3] : ""
        def version = artifact[artifact.size() - 1]

        String avoidCaching = "?\$(date +%s)"
        groupId = groupId.replace(".", "/")

        try {
            String httpHEADResult = ""
            
            scriptContext.withCredentials([scriptContext.usernamePassword(credentialsId: 'icpcoucxba3msje01', passwordVariable: 'MVN_PASS', usernameVariable: 'MVN_USR')]) {
                String url = "https://$scriptContext.env.MVN_USR:$scriptContext.env.MVN_PASS@$GlobalVars.NEXUS_URL_BASE/artifactory/$GlobalVars.NEXUS_PUBLIC_REPO_NAME/$groupId/$artifactId/$version/$artifactId-${version$artifactClassifier}.$artifactExtension$avoidCaching"

                httpHEADResult = scriptContext.sh(
                    script: 'curl --insecure -s -o /dev/null --write-out %{http_code} -I "' + url + '"  --connect-timeout 90',
                    returnStdout: true
                )
            }

            return "200".equals(httpHEADResult?.trim())

        } catch (err) {
            scriptContext.printOpen("NexusUtils.exists threw exception while doing HEAD to ${artifactFullName}\n"+err.getMessage(), EchoLevel.ERROR)
            return false
        }

    }
	
	String getBuildCodeFromSnapshot(String groupId, String artifactId, String version, String repositoryName) {
		
		String avoidCaching = "?\$(date +%s)"
		
		groupId = groupId.toLowerCase().replace(".", "/")
		repositoryName = repositoryName.toLowerCase()
		
		String versionWithOut=version-"SNAPSHOT"
		String url = null
		try {
			scriptContext.withCredentials([scriptContext.usernamePassword(credentialsId: 'icpcoucxba3msje01', passwordVariable: 'MVN_PASS', usernameVariable: 'MVN_USR')]) {
				url = "https://$scriptContext.env.MVN_USR:$scriptContext.env.MVN_PASS@$GlobalVars.NEXUS_URL_BASE/artifactory/${repositoryName}/${groupId}/${artifactId}/${version}/maven-metadata.xml${avoidCaching}"
			
				version = scriptContext.sh(
					script: 'curl --insecure -s "' + url + '"  --connect-timeout 90 | ' +
						'grep -E "<value>" | sort --version-sort | ' +
						'tail -n1 | ' +
						'sed -e "s#\\(.*\\)\\(<value>\\)\\(.*\\)\\(</value>\\)\\(.*\\)#\\3#g"',
					returnStdout: true
				)
				if (version!=null) {
					version=version.replaceAll("\\n","")
					version=version.replaceAll("\\r","")
				}
			}

			scriptContext.printOpen("NexusUtils.getBuildCodeFromSnapshot: last version of ${groupId}:${artifactId} is ${version}", EchoLevel.INFO)
			scriptContext.printOpen("La url es de : ${url}:", EchoLevel.INFO)

		} catch (err) {
			scriptContext.printOpen("NexusUtils.getBuildCodeFromSnapshot threw exception while retrieving build code of ${groupId}:${artifactId}", EchoLevel.ERROR)
			scriptContext.printOpen(err.getMessage(), EchoLevel.ERROR)

			def sw = new StringWriter()
			def pw = new PrintWriter(sw)
			err.printStackTrace(pw)
			scriptContext.printOpen(sw.toString(), EchoLevel.ERROR)
		}
		
		return  version
		
	}

    String getLastVersionNumber(String groupId, String artifactId, Integer majorVersion, String repositoryName, BuildType buildType) {

        String avoidCaching = "?\$(date +%s)"

        groupId = groupId.toLowerCase().replace(".", "/")
        artifactId = artifactId.toLowerCase()
        repositoryName = repositoryName.toLowerCase()
        def majorVersionStr = majorVersion == null ? "" : majorVersion.toString()

        String version = majorVersionStr + '.*'

        if (BuildType.RELEASE_CANDIDATE == buildType) {
            version += '\\.[0-9]{1,}\\.[0-9]{1,}-RC[0-9]{1,}'
        } else if (BuildType.SNAPSHOT == buildType) {
            version += '\\.[0-9]{1,}\\.[0-9]{1,}-SNAPSHOT'
        } else if (BuildType.FINAL == buildType) {
            version += '\\.[0-9]{1,}\\.[0-9]{1,}'
        } else {
            version += '.*'
        }

        try {
            scriptContext.withCredentials([scriptContext.usernamePassword(credentialsId: 'icpcoucxba3msje01', passwordVariable: 'MVN_PASS', usernameVariable: 'MVN_USR')]) {
                String url = "https://$scriptContext.env.MVN_USR:$scriptContext.env.MVN_PASS@$GlobalVars.NEXUS_URL_BASE/artifactory/${repositoryName}/${groupId}/${artifactId}/maven-metadata.xml${avoidCaching}"
            
                version = scriptContext.sh(
                    script: 'curl --insecure -s "' + url + '"  --connect-timeout 90 | ' +
                        'grep -E "<version>' + version +'</version>" | sort --version-sort | ' +
                        'tail -n1 | ' +
                        'sed -e "s#\\(.*\\)\\(<version>\\)\\(.*\\)\\(</version>\\)\\(.*\\)#\\3#g"',
                    returnStdout: true
                )
            }

            scriptContext.printOpen("NexusUtils.getLastVersion: last version of ${groupId}:${artifactId} is ${version}", EchoLevel.INFO)

        } catch (err) {
            scriptContext.printOpen("NexusUtils.getLastVersion threw exception while retrieving last version of ${groupId}:${artifactId}", EchoLevel.ERROR)
            scriptContext.printOpen(err.getMessage(), EchoLevel.ERROR)

            def sw = new StringWriter()
            def pw = new PrintWriter(sw)
            err.printStackTrace(pw)
            scriptContext.printOpen(sw.toString(), EchoLevel.ERROR)
        }

        return version.trim()
    }

    /**
     * STATIC METHODS
     */

    static ArrayList<String> extractArtifactsFromLog(String log) {
        def resultsList = []

        def pattern1 = /Uploaded.*: (http(s){0,1}:\/\/.+\.(zip|ear|jar|pom|ejb|war)) \(/
        Matcher matches = log =~ pattern1

        int numberOfMatches = matches.getCount()

        for (int i = 0; i < numberOfMatches; i++) {            
            resultsList.add(matches.getAt(i)[1])
        }
        return resultsList
    }

    static String getBuildId(ArrayList<String> artifactsDeployedsOnNexus, String artifactId, String version) {
        String result = null

        if (artifactsDeployedsOnNexus != null) {
            artifactsDeployedsOnNexus.each {
                if (it.endsWith('.pom')) {

                    String artifact = it.substring(it.lastIndexOf('/') + 1)
                    artifact = artifact - '.pom'
                    //result=artifact-artifactId-version-'.pom'
                    result = artifact.substring(artifact.lastIndexOf(version) + version.length())
                    return result
                }
            }
        }

        return result
    }

    static String getBuildId(ArrayList<String> artifactsDeployedsOnNexus, String version) {
        String result = null

        def unqualifiedVersion = MavenVersionUtilities.getArtifactVersionWithoutQualifier(version)

        if (artifactsDeployedsOnNexus != null) {
            artifactsDeployedsOnNexus.each {
                if (it.endsWith('.pom')) {

                    String artifact = it.substring(it.lastIndexOf('/') + 1)
                    artifact = artifact - '.pom'
                    def hasQualifier = unqualifiedVersion != version
                    result = hasQualifier ? artifact.substring(artifact.lastIndexOf(unqualifiedVersion+'-') + 1 + unqualifiedVersion.length()) : ''
                    return result
                }
            }
        }

        return result ? result : 'F'
    }

}
