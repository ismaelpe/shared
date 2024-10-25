package com.project.alm

class DeploymentState {

    String appName
    String state
    String instancesRunning
    String maxInstances
    String assignedMemory
    String filesystemSize
    def routes = []

    DeploymentState(String logDump) {
        def arr = logDump.trim().split("[ \t]{2,}")
        this.appName = arr[0]
        this.state = arr[1]
        this.instancesRunning = arr[2].split("/")[0]
        this.maxInstances = arr[2].split("/")[1]
        this.assignedMemory = arr[3]
        this.filesystemSize = arr[4]
        this.routes = arr.length > 5 ? arr[5].split(", ") : []
    }

    boolean isAncient() {
        return appName.startsWith("ancient_")
    }

    String toString() {
        return "DeploymentState:\n" +
                "\tappName: $appName\n" +
                "\tstate: $state\n" +
                "\tinstancesRunning: $instancesRunning\n" +
                "\tmaxInstances: $maxInstances\n" +
                "\tassignedMemory: $assignedMemory\n" +
                "\tfilesystemSize: $filesystemSize\n" +
                "\troutes: $routes\n"
    }

}
	
	
