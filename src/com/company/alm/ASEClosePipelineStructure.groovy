package com.caixabank.absis3

class ASEClosePipelineStructure extends PipelineStructure {


    ASEClosePipelineStructure(String pipelineId) {
        super(pipelineId, "ASE_Close_Pipeline")
    }

    @Override
    def getResult(boolean result) {
        return resultPipelineData.getResult(result)
    }

    @Override
    def getStages() {
        return [
            [id       : pipelineId + "010",
             estado   : "inactive",
             nombre   : "010-init-pipeline",
             resultado: "",
             orden    : 0
            ],
            [id       : pipelineId + "020",
             estado   : "inactive",
             nombre   : "020-close-release",
             resultado: "",
             orden    : 0
            ]
        ]
    }
}
