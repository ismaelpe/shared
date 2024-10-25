import groovy.transform.Field
import com.caixabank.absis3.EchoLevel
import hudson.matrix.*
import hudson.model.*

@Field Map pipelineParams

/* ************************************************************************************************************************************** *\
 * Pipeline Definition                                                                                                                    *
\* ************************************************************************************************************************************** */
def call(Map pipelineParameters) {
	pipelineParams = pipelineParameters
    
    pipeline {
        agent { label 'master' }
        options {
            buildDiscarder(logRotator(numToKeepStr: '30'))
            timestamps()
        }
        stages {           
            stage('delete-all_build-by-branch') {   
                steps {
                    deleteAllBuildByBranchStep()
                }
            }           
        }
        post {
            success {
                endPipelineSuccessStep()
            }
            failure {
                endPipelineFailureStep()
            }
            always {
                endPipelineAlwaysStep()
            }        
        }
	}
}

/* ************************************************************************************************************************************** *\
 * Splitted Pipeline Methods                                                                                                              *
\* ************************************************************************************************************************************** */

/**
 * Stage 'deleteAllBuildByBranchStep'
 */
def deleteAllBuildByBranchStep() {
                        
    def hudsonInstance = hudson.model.Hudson.instance
    def jobNames = hudsonInstance.getJobNames()                        
    def allItems = []
    
    for (name in jobNames) {
        allItems += hudsonInstance.getItemByFullName(name)
    }
    
    for (job in allItems) {
        printOpen("job: " + job.name);
        def counter = 0;
        def gitActions;
        def hudson.plugins.git.Revision r;
        for (build in job.getBuilds()) {
            // It is possible for a build to have multiple BuildData actions
            // since we can use the Mulitple SCM plugin.
            gitActions = build.getActions(hudson.plugins.git.util.BuildData.class)
            if (gitActions != null) {
                for (action in gitActions) {
                    action.buildsByBranchName = new HashMap<String, Build>();
                    r = action.getLastBuiltRevision();
                    if (r != null) {
                        for (branch in r.getBranches()) {
                            action.buildsByBranchName.put(branch.getName(), action.lastBuild)
                        }
                    }
                    build.actions.remove(action)
                    build.actions.add(action)                                      
                    build.save();
                    counter++;                                                                          
                }
            }
            if (job instanceof MatrixProject) {
                def runcounter = 0;
                for (run in build.getRuns()) {
                    gitActions = run.getActions(hudson.plugins.git.util.BuildData.class)
                    if (gitActions != null) {
                        for (action in gitActions) {
                            action.buildsByBranchName = new HashMap<String, Build>();
                            r = action.getLastBuiltRevision();
                            if (r != null) {
                                for (branch in r.getBranches()) {
                                    action.buildsByBranchName.put(branch.getName(), action.lastBuild)
                                }
                            }
                            run.actions.remove(action)
                            run.actions.add(action)                                               
                            run.save();
                            runcounter++;
                        }
                    }
                }
                if (runcounter > 0) {
                    printOpen(" -->> cleaned: " + runcounter + " runs");
                }
            }
        }
        if (counter > 0) {
            printOpen("-- cleaned: " + counter + " builds");
        }
    }
}

/**
 * Stage 'endPipelineSuccessStep'
 */
def endPipelineSuccessStep() {
    printOpen("Success!!", EchoLevel.INFO)
}

/**
 * Stage 'endPipelineFailureStep'
 */
def endPipelineFailureStep() {
    printOpen("Pipeline has failed", EchoLevel.ERROR)
}

/**
 * Stage 'endPipelineAlwaysStep'
 */
def endPipelineAlwaysStep() {
    cleanWorkspace()
}
