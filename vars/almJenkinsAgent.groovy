import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.project.alm.PomXmlStructure
import com.project.alm.Utilities

/**
 * Resuelve el agente a usar en la pipeline
 * @param pipelineParams
 * @return
 */
def call(Map pipelineParams) {
    def jenkinsAgentConfig = [
        standard: env.JENKINS_AGENT_STANDARD,
        standard_jdk8: env.JENKINS_AGENT_STANDARD_JDK8,
        extra: env.JENKINS_AGENT_EXTRA,
        light: env.JENKINS_AGENT_LIGHT
    ]

    def jenkinsAgentType = pipelineParams ? pipelineParams.get('agent', 'standard') : 'standard'
    def jenkinsSlaveLabel = jenkinsAgentConfig[jenkinsAgentType]

    printOpen("Se usará el agente '${jenkinsSlaveLabel}'", EchoLevel.ALL)

    return jenkinsSlaveLabel
}

/**
 * Resuelve el agente a usar en la pipeline, cuando se le pasa el nombre directamente al metodo
 * @param pipelineParams
 * @return
 */
def call(String jenkinsAgentType) {
    def jenkinsAgentConfig = [
        standard: env.JENKINS_AGENT_STANDARD,
        standard_jdk8: env.JENKINS_AGENT_STANDARD_JDK8,
        extra: env.JENKINS_AGENT_EXTRA,
        light: env.JENKINS_AGENT_LIGHT
    ]

    def jenkinsSlaveLabel = jenkinsAgentConfig[jenkinsAgentType]

    printOpen("Se usará el agente '${jenkinsSlaveLabel}'", EchoLevel.ALL)

    return jenkinsSlaveLabel
}
