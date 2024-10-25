package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class IncCampaignCannaryPipelineStructure extends PipelineStructure {


    public IncCampaignCannaryPipelineStructure(String pipelineId) {
        super(pipelineId, "IncCampaignCannary_Pipeline")
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
				 nombre   : "110-validate-dependencies",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-modify-percentage",
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
                 nombre   : "400-end-pipeline",
                 resultado: "",
                 orden    : 0
                ]

        ]
    }

}