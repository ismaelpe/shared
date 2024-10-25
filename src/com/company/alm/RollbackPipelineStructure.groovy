package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class RollbackPipelineStructure extends PipelineStructure {


    public RollbackPipelineStructure(String pipelineId) {
        super(pipelineId, "Rollback_Pipeline")
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
                [id       : pipelineId + "105",
				 estado   : "inactive",
				 nombre   : "105-coherence-validation",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "110",
				 estado   : "inactive",
				 nombre   : "110-check-icp-availability",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "150",
					estado   : "inactive",
					nombre   : "150-restore-configuration",
					resultado: "",
					orden    : 0
				   ],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-undeploy-artifact",
                 resultado: "",
                 orden    : 0
                ],                
				[id       : pipelineId + "210",
                 estado   : "inactive",
                 nombre   : "210-undeploy-artifact-icp",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "220",
				 estado   : "inactive",
				 nombre   : "220-send-undeploy-to-catalog",
				 resultado: "",
				 orden    : 0
				]

        ]
    }

}