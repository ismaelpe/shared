import com.project.alm.EchoLevel
import com.project.alm.GlobalVars

def call(String archetypeModel) {

    printOpen("Generating archetype from project. $archetypeModel will be used as model", EchoLevel.INFO)

    def archetypePropertiesPath
    def archetypeVersionOnPom
    def mvnOutput

    archetypePropertiesPath = sh(returnStdout: true, script: "realpath \$(find . -name \"archetype.properties\")")

    printOpen("Archetype properties file: " +
        (archetypePropertiesPath == '' ? 'NOT FOUND. PIPELINE WILL FAIL' : archetypePropertiesPath), EchoLevel.DEBUG)

    configFileProvider([configFile(fileId: 'alm-maven-settings-with-singulares', variable: 'MAVEN_SETTINGS')]) {
        archetypeVersionOnPom = sh(
                returnStdout: true,
                script: "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} " +
                        "-s $MAVEN_SETTINGS " +
                        "help:evaluate -Dexpression=project.version -q -DforceStdout"
        ).trim()
    }

    printOpen("The archetype version is $archetypeVersionOnPom. This will be set on archetype.properties", EchoLevel.DEBUG)

    sh(
            returnStdout: true,
            script: "sed -i -e 's/1.0.0-SNAPSHOT/$archetypeVersionOnPom/g' $archetypePropertiesPath"
    )

    def cmd = "mvn  ${GlobalVars.GLOBAL_MVN_PARAMS} -Dhttp.proxyHost=${env.proxyHost} -Dhttp.proxyPort=${env.proxyPort} -Dhttps.proxyHost=${env.proxyHost} -Dhttps.proxyPort=${env.proxyPort} <Default_Maven_Settings> -f ${archetypeModel}/pom.xml org.apache.maven.plugins:maven-archetype-plugin:3.0.2-SNAPSHOT::create-from-project -Darchetype.properties=$archetypePropertiesPath"
    runMavenCommand(cmd)

}
