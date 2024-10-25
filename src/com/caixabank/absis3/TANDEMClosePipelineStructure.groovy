package com.caixabank.absis3

class TANDEMClosePipelineStructure extends PipelineStructure {


    TANDEMClosePipelineStructure(String pipelineId) {
        super(pipelineId, "TANDEM_Close_Pipeline")
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
                 nombre   : "init-pipeline",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "close-release",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }
}