package com.project.alm

class AppDeploymentState {

    DeploymentState ancient
    DeploymentState live
    boolean isDeployed
    boolean bothAreRunning
    boolean onlyAncientIsRunning
    boolean onlyLiveIsRunning
    int numBetaRoutesOnAncient
    int numLiveRoutesOnAncient
    int numBetaRoutesOnLive
    int numLiveRoutesOnLive
	AppDeploymentState() {
		
	}
    AppDeploymentState(MultipleDeploymentsState multipleDeploymentsState, String appName) {
        for (DeploymentState deployment in multipleDeploymentsState.deployments) {
            if ("ancient_${appName}" == deployment.appName) {
                ancient = deployment
            } else if ("${appName}" == deployment.appName) {
                live = deployment
            }
        }

        isDeployed = ancient || live
        bothAreRunning = "started" == ancient?.state && "started" == live?.state
        onlyAncientIsRunning = "started" == ancient?.state && "started" != live?.state
        onlyLiveIsRunning = "started" != ancient?.state && "started" == live?.state
        numBetaRoutesOnAncient = (ancient?.routes =~ /(?i)${appName}-beta\./).findAll().size()
        numLiveRoutesOnAncient = (ancient?.routes =~ /(?i)${appName}\./).findAll().size()
        numBetaRoutesOnLive = (live?.routes =~ /(?i)${appName}-beta\./).findAll().size()
        numLiveRoutesOnLive = (live?.routes =~ /(?i)${appName}\./).findAll().size()
    }

    Current getCurrent() {
        int totalAncientRoutes = numBetaRoutesOnAncient + numLiveRoutesOnAncient
        int totalLiveRoutes = numBetaRoutesOnLive + numLiveRoutesOnLive
        int totalRoutes = totalAncientRoutes + totalLiveRoutes
        if (bothAreRunning && numLiveRoutesOnAncient == 3 && numBetaRoutesOnLive == 3 && totalRoutes == 6) {
            return Current.CANARY
        } else if (onlyLiveIsRunning && totalRoutes == 6 && numLiveRoutesOnLive == 3 && numBetaRoutesOnLive == 3) {
            return Current.CONSOLIDATED
        } else if (totalRoutes < 6 || (onlyAncientIsRunning && totalAncientRoutes < 6) && (onlyLiveIsRunning && totalLiveRoutes < 6)) {
            return Current.INCIDENT_LIKELY
        } else if (bothAreRunning && (totalAncientRoutes != 3 || totalLiveRoutes != 3)) {
            return Current.INCONSISTENT_ROUTES
        } else if (!isDeployed) {
            return Current.NOT_DEPLOYED
        } else {
            return Current.UNKNOWN
        }
    }

    String toString() {
        return "AppDeploymentState:\n" +
                "\tANCIENT: \n${ancient.toString()}\n" +
                "\tLIVE: \n${live.toString()}\n" +
                "\tisDeployed: $isDeployed\n" +
                "\tbothAreRunning: $bothAreRunning\n" +
                "\tonlyAncientIsRunning: $onlyAncientIsRunning\n" +
                "\tonlyLiveIsRunning: $onlyLiveIsRunning\n" +
                "\tnumBetaRoutesOnAncient: $numBetaRoutesOnAncient\n" +
                "\tnumLiveRoutesOnAncient: $numLiveRoutesOnAncient\n" +
                "\tnumBetaRoutesOnLive: $numBetaRoutesOnLive\n" +
                "\tnumLiveRoutesOnLive: $numLiveRoutesOnLive\n"
    }

    enum Current {
        CANARY,
        CONSOLIDATED,
        INCONSISTENT_ROUTES,
        INCIDENT_LIKELY,
        NOT_DEPLOYED,
        UNKNOWN
    }

}
	
	
