package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class DeployScriptBBDDPipelineStructure extends PipelineStructure {


    public DeployScriptBBDDPipelineStructure(String pipelineId) {
        super(pipelineId, "DeployScript_Pipeline")
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
                 nombre   : "100-get-git-repo",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "110",
				 estado   : "inactive",
				 nombre   : "110-check-cloud-availability",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "200",
			     estado   : "inactive",
				 nombre   : "200-prepare-Release",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "406",
				 estado   : "inactive",
				 nombre   : "406-deploy-bbdd-scripts",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "500",
				 estado   : "inactive",
				 nombre   : "500-create-MR",
				 resultado: "",
				 orden    : 0
				]
        ]
    }

}
