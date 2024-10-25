package com.project.alm

import java.util.regex.Matcher


class PomForbiddenDependenciesValidator {

    def restrictions
    def whitelist

    PomForbiddenDependenciesValidator(def restrictions, def whitelist = "{}") {
        this.restrictions = restrictions

        try {

            this.whitelist = new groovy.json.JsonSlurper().parseText(whitelist)

        } catch (err) {
            //If format is not correct, no whitelist will be loaded
            this.whitelist = [:]
        }
    }

    public List validate(def artifact, def dependencies) {

        def forbiddenDependencies = []

        def dependencyList = dependencies.split( '\n' )
		
		for(dependency in dependencyList) {
            forbiddenDependencies += checkRestrictions(artifact, dependency.trim())

        }

        return forbiddenDependencies
    }

    private checkRestrictions(def artifact, def dependency) {

        def forbiddenDependencies = []

        for(restriction in this.restrictions) {
			
            Matcher matches = dependency =~ restriction
            if ( matches.getCount() && ! isWhitelisted(artifact, dependency) ) {

                forbiddenDependencies += dependency

            }

        }

        return forbiddenDependencies
    }

    private isWhitelisted(String artifact, conflictiveDependency) {

        for (entry in whitelist.entrySet()) {

            Matcher matchesArtifact = artifact =~ entry.getKey()
            if (matchesArtifact.getCount() == 0) continue

            for (whitelistedDependency in entry.getValue()) {

                Matcher matchesDependency = conflictiveDependency =~ whitelistedDependency
                if (matchesDependency.getCount() == 0) continue

                return true
            }

        }

        return false
    }

}


