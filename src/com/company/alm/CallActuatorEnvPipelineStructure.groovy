package com.caixabank.absis3

class CallActuatorEnvPipelineStructure extends PipelineStructure {


    public CallActuatorEnvPipelineStructure(String pipelineId) {
        super(pipelineId, "CallActuatorEnv_Pipeline")
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
				 nombre   : "200-get-application-properties",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "210",
				 estado   : "inactive",
				 nombre   : "210-get-system-properties",
				 resultado: "",
				 orden    : 0
				]
        ]
    }

}
