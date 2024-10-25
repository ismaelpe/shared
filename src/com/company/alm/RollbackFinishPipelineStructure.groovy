package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class RollbackFinishPipelineStructure extends PipelineStructure {


    public RollbackFinishPipelineStructure(String pipelineId) {
        super(pipelineId, "Rollback_Finish_Pipeline")
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
                 nombre   : "100-send-ancient-version-restored-to-GPL",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-apimanager-technicalservices-registration",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
