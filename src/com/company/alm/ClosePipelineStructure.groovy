package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class ClosePipelineStructure extends PipelineStructure {


    public ClosePipelineStructure(String pipelineId) {
        super(pipelineId, "Close_Pipeline")
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
				[id       : pipelineId + "110",
				 estado   : "inactive",
				 nombre   : "110-check-icp-availability",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-close-release",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "300-copy-config-files",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "400",
                 estado   : "inactive",
                 nombre   : "400-refresh-micro",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "550",
                 estado   : "inactive",
                 nombre   : "550-apimanager-technicalservices-registration",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "600",
                 estado   : "inactive",
                 nombre   : "600-create-MR",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
