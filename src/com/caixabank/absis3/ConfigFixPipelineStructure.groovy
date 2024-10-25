package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class ConfigFixPipelineStructure extends PipelineStructure {


    public ConfigFixPipelineStructure(String pipelineId) {
        super(pipelineId, "ConfigFix_Pipeline")
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
                 nombre   : "100-prepare-ConfigFix",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }
}
