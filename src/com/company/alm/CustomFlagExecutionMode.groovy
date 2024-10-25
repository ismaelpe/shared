package com.project.alm

/*
 * Modo por defecto de ejecucion
 */

class CustomFlagExecutionMode extends DefaultExecutionMode {

    private String actionFlag;
    private String almSubFolder;

    CustomFlagExecutionMode(String actionFlag, String almSubFolder) {
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
        return false
    }

    boolean isNotifyforFailureNeeded() {
        return false
    }

    String toString() {
        return "CustomFlagExecutionMode:\n" +
            "\tactionFlag: $actionFlag\n" +
            "\talmSubFolder: $almSubFolder\n"
    }


}
