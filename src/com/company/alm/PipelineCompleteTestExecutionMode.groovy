package com.caixabank.absis3


class PipelineCompleteTestExecutionMode extends DefaultExecutionMode {

    static final String DEFAULT_FLAG = "actionInTestingPipeline"
    static final String FLAG_WHEN_HOTFIX = "actionWhenHotfixInTestingPipeline"
    static final String FLAG_WHEN_CONFIGURATION_FIX = "actionWhenConfigurationFixInTestingPipeline"
    static final String SUBFOLDER = "stage"

    String actionFlag = DEFAULT_FLAG
    String almSubFolder = SUBFOLDER

    PipelineCompleteTestExecutionMode() {
        super()
    }

    PipelineCompleteTestExecutionMode(String customActionFlag, String subFolder, Boolean skipQuality) {
        super(subFolder, skipQuality)
        
        if (customActionFlag != null && !customActionFlag.isEmpty()) {
            actionFlag = customActionFlag
        }

        if (subFolder != null && !subFolder.isEmpty()) {
            this.almSubFolder = subFolder
        }
    }

    String actionFlag() {
        return actionFlag
    }

    boolean invokeNextActionAuto() {
        return true
    }

    boolean skipTest() {
        return super.skipTest()
    }

    boolean skipIntegrationTest() {
        return super.skipIntegrationTest()
    }

    boolean skipJavadoc() {
        return super.skipJavadoc()
    }

    boolean skipAgileworksValidation() {
        return false
    }

    boolean isNotifyforFailureNeeded() {
        return false
    }

    String toString() {
        return "PipelineCompleteTestExecutionMode:\n" +
            "\tactionFlag: $actionFlag\n" +
            "\talmSubFolder: $almSubFolder\n"
    }
}
