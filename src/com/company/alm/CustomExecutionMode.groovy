package com.caixabank.absis3

/*
 * Modo por defecto de ejecucion
 */

class CustomExecutionMode extends DefaultExecutionMode {

    private String actionFlag;
    private String almSubFolder;
	private skipTest=false;
	private skipIntegrationTest=false;
	private skipJavadoc=false;
	
	CustomExecutionMode() {
	}

    CustomExecutionMode(String actionFlag, String almSubFolder) {

        this.actionFlag = actionFlag;
        this.almSubFolder = almSubFolder;
    }

    String actionFlag() {
        return actionFlag
    }
	
	
	
	boolean skipTest() {
		return skipTest
	}

	boolean skipIntegrationTest() {
		return skipIntegrationTest
	}

	boolean skipJavadoc() {
		return skipJavadoc
	}
	

    boolean invokeNextActionAuto() {
        return true
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
