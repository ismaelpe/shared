def jenkinsPathFinal = jenkinsPath

folder(jenkinsPathFinal) {
    displayName(domain)
    description("Folder for domain $domain")
}

if (jenkinsPathModule) {
    jenkinsPathFinal = jenkinsPathModule
    folder(jenkinsPathFinal) {
        displayName(modulo)
        description(isArq ? "Folder for subdomain $modulo" : "Folder for the application module $modulo")
    }
}

// Current jop defitinion
pipelineJob("$jenkinsPathFinal/$nameApp") {
    definition {       
        cpsScm {
            scm {
                git {
                    remote {
                        url("${pathToRepo}")
                        credentials("${gitCredentials}")
                    }
                    branches('origin/\${gitlabTargetBranch}')
                }
            }
        }
    }
    logRotator {
        numToKeep(30)
    }
    triggers {
        gitlabPush {
            buildOnMergeRequestEvents(true)
            buildOnPushEvents(true)
            rebuildOpenMergeRequest('source')
            includeBranches('master,feature/*,release/*,hotfix/*,configfix/*')
        }
    }  
}

if (createJobOndemand) {
    // Current jop defitinion
    pipelineJob("$jenkinsPathFinal/$nameApp-ondemand") {
        parameters {
            stringParam('pathToRepoParam', pathToRepo, 'Path to the Repo')
            stringParam('originBranchParam', 'master', 'Branch origin of the release')
        }
        definition {
            cps {
                script("""
    @Library('absis3-services') _

    absisPipelineOnDemandBuild()
                """)
            }
        }
        logRotator {
            numToKeep(30)
        }
    }
}