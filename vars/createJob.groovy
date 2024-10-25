import com.caixabank.absis3.GlobalVars

def call(String jenkinsPath, String nameApp, String pathToRepo, String gitCredentials) {

    pipelineJob("${jenkinsPath}/${nameApp}") {
        definition {
            cpsScm {
                scm {
                    git {
                        remote {
                            url("${pathToRepo}")
                            credentials("${gitCredentials}")
                        }
                        branches('master', '*/feature/*', '*/release/*', '*/hotfix/*')
                    }
                }
            }
        }
        triggers {
            gitlabPush {
                buildOnMergeRequestEvents(true)
                buildOnPushEvents(true)
            }
        }
    }
}