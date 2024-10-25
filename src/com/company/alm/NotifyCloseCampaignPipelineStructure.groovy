package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class NotifyCloseCampaignPipelineStructure extends PipelineStructure {


    public NotifyCloseCampaignPipelineStructure(String pipelineId) {
        super(pipelineId, "NotifyCloseCampaign_Pipeline")
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
				[id       : pipelineId + "110",
				 estado   : "inactive",
				 nombre   : "110-get-iop",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "200",
				 estado   : "inactive",
				 nombre   : "200-notify-close-campaign",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "300",
				 estado   : "inactive",
				 nombre   : "300-end-pipeline",
				 resultado: "",
				 orden    : 0
				]
        ]
    }
}
