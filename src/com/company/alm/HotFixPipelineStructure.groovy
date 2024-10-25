package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class HotFixPipelineStructure extends PipelineStructure {


    public HotFixPipelineStructure(String pipelineId) {
        super(pipelineId, "HotFix_Pipeline")
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
                 nombre   : "100-prepare-Fix",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }
}
