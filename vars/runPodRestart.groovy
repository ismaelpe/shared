import com.project.alm.EchoLevel
import com.project.alm.ICPPodsStatus

def call(ICPPodsStatus pods, String namespace, String environment, String center, String color) {

    def restartedPods = []

    List componentsToBeRestarted = pods.orderPodsWithNonConsecutiveComponentName(center, color)

    printOpen("componentsToBeRestarted: ${componentsToBeRestarted}", EchoLevel.ALL)

    for(component in componentsToBeRestarted) {
        restartedPods += restartPod(component.componentName, component.podId, namespace, environment, center, color)
    }


    printOpen("restartedPods: ${restartedPods}", EchoLevel.ALL)

    return restartedPods
}

private restartPod(String componentName, String podId, String namespace, String environment, String center, String color) {

    def podNotYetRestartedID = triggerPodRestartAndGetPodNewId(componentName, podId, namespace, environment, center, color)

    def started = false

    while( ! started ) {

        started = checkIfPodRestarted(componentName, podNotYetRestartedID, namespace, environment, center)

    }

    return [oldPod: podId, newPod: podNotYetRestartedID]
}

private triggerPodRestartAndGetPodNewId(String componentName, String podId, String namespace, String environment, String center, String color) {

    def restartingPod

    sendRequestToICP.restartPod(componentName, podId, namespace, environment, center)

    while ( ! restartingPod ) {

        sleep 5
        def response = sendRequestToICP.getPodsInfo(namespace, environment, center)
        ICPPodsStatus pods = new ICPPodsStatus(response?.body, componentName, environment)
        restartingPod = pods.getNotReadyPod(center, color, componentName, [podId])

    }
    printOpen("restartingPod: ${restartingPod}", EchoLevel.ALL)
    return restartingPod
}

private boolean checkIfPodRestarted(String componentName, String podId, String namespace, String environment, String center) {

    def response = sendRequestToICP.getPodsInfo(namespace, environment, center)
    ICPPodsStatus pods = new ICPPodsStatus(response?.body, componentName, environment)

    def podOnRestart = pods.findPod(podId)

    printOpen("Pod ${podId} has restarted: ${podOnRestart?.ready}", EchoLevel.ALL)

    return podOnRestart?.ready

}
