import com.project.alm.GlobalVars
import com.project.alm.MavenGoalExecutionException
import com.project.alm.MavenGoalExecutionFailureError
import com.project.alm.MavenGoalExecutionFailureErrorConditionals

def call(String mvnCommand) {

	MavenGoalExecutionFailureError lastError

	try {

		timeout(GlobalVars.MAVEN_INTEGRATION_TEST_RETRIES_TIMEOUT) {
			waitUntil(initialRecurrencePeriod: 15000) {
				try {

					runMavenCommand(mvnCommand)

					return true
				} catch (MavenGoalExecutionException mgee) {

					boolean shallWeStop = shallWeStopTheExecutionOfITTest(mgee.mavenError)

					if (shallWeStop) {

						throw mgee
					}

					lastError = mgee.mavenError

					return false
				}
			}
		}
	} catch(org.jenkinsci.plugins.workflow.steps.FlowInterruptedException fie) {

		if (lastError) {

			throw new MavenGoalExecutionException(lastError)
		}

		throw fie
	}
}

private boolean shallWeStopTheExecutionOfITTest(MavenGoalExecutionFailureError error) {

	if (MavenGoalExecutionFailureErrorConditionals.isAnCloudSSLEventualErrorOnITTest(error)) {

		return false
	} else if (MavenGoalExecutionFailureErrorConditionals.isAContractServerSSLEventualErrorOnOpenApiGeneration(error)) {

		return false
	} else if (MavenGoalExecutionFailureErrorConditionals.isANexusDownloadFailureDueToPrematureEndOfContentLength(error)) {

		return false
	} else if (MavenGoalExecutionFailureErrorConditionals.isANexusDownloadFailureDueToANonPresentPluginDependencyThatWasPreviouslyOnNexus(error)) {

		return false
	}

	return true
}