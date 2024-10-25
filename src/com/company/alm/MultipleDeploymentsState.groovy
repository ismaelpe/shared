package com.project.alm

class MultipleDeploymentsState {

    String center
    String organization
    String space
    List<DeploymentState> deployments = new ArrayList<>()

    MultipleDeploymentsState(String center, String organization, String space, String logDump) {
        this.center = center
        this.organization = organization
        this.space = space
        logDump.trim().tokenize('\n').each {
            deployments.add(new DeploymentState(it))
        }
    }

    AppDeploymentState getAppState(String appName) {
        return new AppDeploymentState(this, appName)
    }

    String toString() {
        def msg =
                "MultipleDeploymentsState:\n" +
                        "\tcenter: $center\n" +
                        "\torganization: $organization\n" +
                        "\tspace: $space\n"
        deployments.each {
            msg += it.toString()
        }
        return msg
    }

}
	
	
