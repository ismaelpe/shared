import com.project.alm.EchoLevel
import com.project.alm.PomXmlStructure
import com.project.alm.ArtifactType
import com.project.alm.ArtifactSubType
import com.project.alm.GlobalVars

def call(PomXmlStructure pomXml) {

    printOpen("The new artifactVersion is ${pomXml.artifactVersion}", EchoLevel.DEBUG)

    printOpen("The revision version is ${pomXml.revision}", EchoLevel.DEBUG)

    printOpen("The arch version is ${pomXml.archVersion}", EchoLevel.DEBUG)

    if (ArtifactType.AGREGADOR == pomXml.getArtifactType() && ArtifactSubType.PLUGIN == pomXml.getArtifactSubType()) {
        pomXml = pluginUpgradeVersion(pomXml)
    } else {
        pomXml = defaultUpgradeVersion(pomXml)
    }

    printOpen("Version upgraded. The new version is ${pomXml.artifactVersion}", EchoLevel.INFO)

    return pomXml
}

private pluginUpgradeVersion(PomXmlStructure pomXml) {

    def cmd = ""
    if (pomXml?.revision) {

        pomXml.revision = pomXml.artifactVersion

        try {
            configFileProvider([configFile(fileId: 'absis3-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
                /*sh "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS versions:set -DgenerateBackupPoms=false -DnewVersion=${pomXml.artifactVersion} -DgroupId=* -DartifactId=*"
                sh "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS versions:set-property -DgenerateBackupPoms=false -Dproperty=revision -DnewVersion=${pomXml.revision} -N"*/
				cmd = "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -s $MAVEN_SETTINGS versions:set -DgenerateBackupPoms=false -DnewVersion=${pomXml.artifactVersion} -DgroupId=* -DartifactId=*"
				runMavenCommand(cmd)
                cmd = "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -s $MAVEN_SETTINGS versions:set-property -DgenerateBackupPoms=false -Dproperty=revision -DnewVersion=${pomXml.revision} -N"
                runMavenCommand(cmd)
            }
        } catch (Throwable t) {
            configFileProvider([configFile(fileId: 'absis3-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
                /* Cuidado, deben mantenerse los dos parametros "-Dmaven.test.skip=true -DskipTests" para evitar la compilacion de tests pero tambien
                   la ejecucion en caso de realizarse con un target ya generado anteriormente */
                /*sh "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS clean verify -Dmaven.test.skip=true -DskipTests"
                sh "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS versions:set -DgenerateBackupPoms=false -DnewVersion=${pomXml.artifactVersion} -DgroupId=* -DartifactId=*"
                sh "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS versions:set-property -DgenerateBackupPoms=false -Dproperty=revision -DnewVersion=${pomXml.revision} -N"*/
				cmd = "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS}  -s $MAVEN_SETTINGS clean verify -Dmaven.test.skip=true -DskipTests"
                runMavenCommand(cmd)
                cmd = "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS}  -s $MAVEN_SETTINGS versions:set -DgenerateBackupPoms=false -DnewVersion=${pomXml.artifactVersion} -DgroupId=* -DartifactId=*"
                runMavenCommand(cmd)
                cmd = "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS}  -s $MAVEN_SETTINGS versions:set-property -DgenerateBackupPoms=false -Dproperty=revision -DnewVersion=${pomXml.revision} -N"
                runMavenCommand(cmd)
            }
        }
    } else {
        configFileProvider([configFile(fileId: 'absis3-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
            cmd = "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS versions:set -DgenerateBackupPoms=false -DnewVersion=${pomXml.artifactVersion} -DgroupId=* -DartifactId=*"
            runMavenCommand(cmd)
        }
    }

    return pomXml
}

private defaultUpgradeVersion(PomXmlStructure pomXml) {

    def cmd = ""
    if (pomXml?.revision) {

        pomXml.revision = pomXml.artifactVersion

        try {
            configFileProvider([configFile(fileId: 'absis3-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
                cmd = "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS versions:set -DgenerateBackupPoms=false -DnewVersion=${pomXml.artifactVersion} -DoldVersion=* -DgroupId=* -DartifactId=*"
                runMavenCommand(cmd)
                cmd = "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS versions:set-property -DgenerateBackupPoms=false -Dproperty=revision -DnewVersion=${pomXml.revision} -N"
                runMavenCommand(cmd)
            }
        } catch (Throwable t) {
            configFileProvider([configFile(fileId: 'absis3-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
                /* Cuidado, deben mantenerse los dos parametros "-Dmaven.test.skip=true -DskipTests" para evitar la compilacion de tests pero tambien
                   la ejecucion en caso de realizarse con un target ya generado anteriormente */
                cmd = "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS clean verify -Dmaven.test.skip=true -DskipTests"
                runMavenCommand(cmd)
                cmd = "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS versions:set -DgenerateBackupPoms=false -DnewVersion=${pomXml.artifactVersion} -DoldVersion=* -DgroupId=* -DartifactId=*"
                runMavenCommand(cmd)
                cmd = "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS versions:set-property -DgenerateBackupPoms=false -Dproperty=revision -DnewVersion=${pomXml.revision} -N"
                runMavenCommand(cmd)
            }
        }
    } else {
        configFileProvider([configFile(fileId: 'absis3-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
            cmd = "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} -s $MAVEN_SETTINGS versions:set -DgenerateBackupPoms=false -DnewVersion=${pomXml.artifactVersion} -DoldVersion=* -DgroupId=* -DartifactId=*"
            runMavenCommand(cmd)
        }
    }

    return pomXml
}
