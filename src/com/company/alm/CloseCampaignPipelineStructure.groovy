package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class CloseCampaignPipelineStructure extends PipelineStructure {


    public CloseCampaignPipelineStructure(String pipelineId) {
        super(pipelineId, "CloseCampaign_Pipeline")
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
                 nombre   : "100-init-data",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "200",
					estado   : "inactive",
					nombre   : "200-reset-percentage",
					resultado: "",
					orden    : 0
				 ],
				 [id       : pipelineId + "300",
					 estado   : "inactive",
					 nombre   : "300-refresh-apigateway",
					 resultado: "",
					 orden    : 0
			     ],
				 [id       : pipelineId + "400",
					 estado   : "inactive",
					 nombre   : "400-close-campaign",
					 resultado: "",
					 orden    : 0
				],
				[id       : pipelineId + "500",
				 estado   : "inactive",
				 nombre   : "500-end-pipeline",
				 resultado: "",
				 orden    : 0
				]
        ]
    }
}
