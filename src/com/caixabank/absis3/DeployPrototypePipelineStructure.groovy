package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author absis
 */

class DeployPrototypePipelineStructure extends PipelineStructure {

    DeployPrototypePipelineStructure() {
        super(null, "CI_DeployPrototype")
    }

    DeployPrototypePipelineStructure(String pipelineId) {
        super(pipelineId, "CI_DeployPrototype")
    }

    DeployPrototypePipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
        super(pipelineIdParam, nombrePipelineParam)
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
                 nombre   : "100-init",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "200",
				 estado   : "inactive",
				 nombre   : "200-check-ICP-availiability",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "300-deploy-prototype",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
