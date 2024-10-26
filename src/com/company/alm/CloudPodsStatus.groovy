package com.project.alm

import com.cloudbees.groovy.cps.NonCPS

class CloudPodsStatus {

    def podsStatus = [:]
    def environment

    CloudPodsStatus(def podsRawMetadata, def componentNamesToMatch, def environment){

        this.podsStatus = buildFromMultipleCentersMetadata(podsRawMetadata, componentNamesToMatch)
        this.environment = environment

	}

    @NonCPS
    private buildFromMultipleCentersMetadata(def podsRawMetadata, def componentNamesToMatch) {

        def pods = [:]

        for(centerMetadata in podsRawMetadata) {

            def center = centerMetadata?.metadata?.cluster?.startsWith("cloud01") ?
                'AZ1' :
                centerMetadata?.metadata?.cluster?.startsWith("cloud02") ? 'AZ2' : 'UNKNOWN'

            if (center == 'UNKNOWN') continue

            pods.put(center, buildFromSingleCenterMetadata(centerMetadata, componentNamesToMatch))

        }

        return pods
    }

    @NonCPS
    private buildFromSingleCenterMetadata(def podsRawMetadata, def componentNamesToMatch) {

        def pods = [blue:[:], green:[:]]

        for (resource in podsRawMetadata?.resources) {

            String podId = resource?.name
            String componentName = podId?.substring(0, podId.indexOf("-")).toLowerCase()
            if ( ! componentNamesToMatch.contains(componentName) ) continue
            boolean ready = resource?.status?.ready?.toBoolean()

            String color = podId?.substring(podId.indexOf("-") + 1, podId.indexOf("-") + 2).toLowerCase()

            if (color == 'g') {

                def podsByComponent = pods.green.get(componentName) ? pods.green.get(componentName) : []
                podsByComponent += [podId: podId, ready: ready]
                pods.green.put(componentName, podsByComponent)

            } else if (color == 'b') {

                def podsByComponent = pods.blue.get(componentName) ? pods.blue.get(componentName) : []
                podsByComponent += [podId: podId, ready: ready]
                pods.blue.put(componentName, podsByComponent)

            } else {
                continue
            }

        }

        return pods
    }

    public removeComponent(String center, String color, String componentName) {

        podsByCenterAndColor = this.podsStatus.get(center).get(color)
        return podsByCenterAndColor.remove(componentName.toLowerCase())

    }

    public getNotReadyPod(String center, String color, String componentName, def ignoreList = []) {

        def podsByCenterAndColorAndComponentName = this.podsStatus.get(center).get(color).get(componentName)
        for(pod in podsByCenterAndColorAndComponentName) {

            if(pod.ready == false && ! ignoreList.contains(pod.podId)) return pod.podId

        }

        return null
    }

    public findPod(String podId) {

        for(podsByCenter in this.podsStatus.values()) {

            for(podsByColor in podsByCenter.values()) {

                for (podsByComponentName in podsByColor.values()) {

                    for (singlePod in podsByComponentName) {

                        if (singlePod.podId == podId) return singlePod

                    }

                }

            }

        }

        return null
    }

    public removePod(String podId) {

        for(podsByCenter in this.podsStatus.values()) {

            for(podsByColor in podsByCenter.values()) {

                for (podsByComponentName in podsByColor.values()) {

                    for (singlePod in podsByComponentName) {

                        if (singlePod.podId == podId) return podsByComponentName.remove(singlePod)

                    }

                }

            }

        }

        return null
    }

    public orderPodsWithNonConsecutiveComponentName(String center, String color) {

        def components = podsStatus.get(center).get(color)
        def orderedComponents = []

        def foundAPod = true

        while(foundAPod) {

            foundAPod = false

            for(component in components) {

                try {

                    def pod = component.value.pop()
                    foundAPod = true
                    orderedComponents += [componentName: component.key, podId: pod.podId]

                } catch (NoSuchElementException nsee) {
                    continue
                }

            }

        }

        return orderedComponents
    }
	
}

