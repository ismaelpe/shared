package com.project.alm

/*
 * Modo por defecto de ejecucion
 */

class CoreUpdateExecutionMode extends DefaultExecutionMode {

    private String actionFlag;
    private String almSubFolder;

    CoreUpdateExecutionMode(String actionFlag, String almSubFolder) {
        this.actionFlag = actionFlag;
        this.almSubFolder = almSubFolder;
    }

    String actionFlag() {
        return actionFlag
    }

    boolean invokeNextActionAuto() {
        return true
    }

    boolean skipTest() {
        return false
    }

    boolean skipIntegrationTest() {
        return false
    }

    boolean skipJavadoc() {
        return false
    }

    boolean skipAgileworksValidation() {
        return true
    }

    boolean isNotifyforFailureNeeded() {
        return true
    }

    String toString() {
        return "CoreUpdateExecutionMode:\n" +
            "\tactionFlag: $actionFlag\n" +
            "\talmSubFolder: $almSubFolder\n"
    }


}
