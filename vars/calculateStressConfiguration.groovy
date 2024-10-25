import com.caixabank.absis3.StressEnviromentConfiguration
import com.cloudbees.groovy.cps.NonCPS

@NonCPS
def call(String json) {
	def stressEnvConfiguration = new groovy.json.JsonSlurper().parseText(environmentConfiguration)
	return new StressEnviromentConfiguration(stressEnvConfiguration)
}