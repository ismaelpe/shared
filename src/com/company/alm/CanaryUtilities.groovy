package com.caixabank.absis3

class CanaryUtilities {

    static Integer[] CANARY_ROLLOUT_PERCENTAGES = [0, 5, 10, 25, 50, 100]
    static String[] ARTIFACTS_THAT_USE_ALL_CANARY_PERCENTAGES = ['service-manager-micro.*']

    static int initialPercentage() {
        return CANARY_ROLLOUT_PERCENTAGES[0]
    }

    static int rollbackPercentage() {
        return -1
    }

    static int finalPercentage(String artifactName = "") {

        boolean usesAllPercentages = false
        ARTIFACTS_THAT_USE_ALL_CANARY_PERCENTAGES.each {
            if (artifactName =~ it) usesAllPercentages = true
        }

        int finalIdx = usesAllPercentages ? 1 : 2

        return CANARY_ROLLOUT_PERCENTAGES[CANARY_ROLLOUT_PERCENTAGES.length - finalIdx]
    }

    static Integer incrementPercentage(Integer currentPercentage) {
        return CANARY_ROLLOUT_PERCENTAGES.find { it > currentPercentage }
    }

    static boolean weHaveReachedFinalPercentage(Integer currentPercentage, String artifactName = "") {
        currentPercentage >= CanaryUtilities.finalPercentage(artifactName) ||
            incrementPercentage(currentPercentage) > CanaryUtilities.finalPercentage(artifactName) ||
            incrementPercentage(currentPercentage) == null
    }
}
