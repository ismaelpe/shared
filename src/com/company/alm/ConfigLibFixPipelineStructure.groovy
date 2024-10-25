package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class ConfigLibFixPipelineStructure extends PipelineStructure {


    public ConfigLibFixPipelineStructure(String pipelineId) {
        super(pipelineId, "ConfigLibFix_Pipeline")
    }

    @Override
    def getResult(boolean result) {
        return resultPipelineData.getResult(result)
    }


    @Override
    def getStages() {
        return [
                [id       : pipelineId + "100",
                 estado   : "inactive",
                 nombre   : "100-prepare-ConfigLibFix",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }
}
