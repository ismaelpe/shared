package com.project.alm

/*
 * Modo por defecto de ejecucion
 */

class DefaultExecutionMode implements PipelineExecutionMode {

    String almSubFolder
    
    Boolean skipQuality
   
    DefaultExecutionMode() {
        this.skipQuality = false
    }

    DefaultExecutionMode(String subFolder, Boolean skipQuality) {
        this.almSubFolder = subFolder
        this.skipQuality = skipQuality;
    }

    String actionFlag() {
        return "NO_FLAG"
    }

    boolean invokeNextActionAuto() {
        return false
    }

    boolean skipTest() {
        return this.skipQuality      
    }

    boolean skipIntegrationTest() {
        return this.skipQuality 
    }

    boolean skipJavadoc() {
        return false 
        //return this.skipQuality 
    }

    boolean skipAgileworksValidation() {
        return false
    }

    boolean isNotifyforFailureNeeded() {
        return false
    }

    String parseJobName(String jobName) {
        if (almSubFolder != null && !almSubFolder.isEmpty() && jobName.indexOf(ALM_FOLDER + "/" + almSubFolder) == -1) {
            return jobName.replace(ALM_FOLDER, ALM_FOLDER + "/" + almSubFolder);
        } else {
            return jobName
        }
    }

    String toString() {
        return "DefaultExecutionMode:\n" +
            "\talmSubFolder: $almSubFolder\n"
    }
}
