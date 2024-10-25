package com.caixabank.absis3

class CGSClosePipelineStructure extends PipelineStructure {


    CGSClosePipelineStructure(String pipelineId) {
        super(pipelineId, "CGS_Close_Pipeline")
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
                 nombre   : "100-init-pipeline",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-close-release",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }
}
