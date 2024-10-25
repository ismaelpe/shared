package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class CreateReleaseCandidateBBDDPipelineStructure extends PipelineStructure {


    public CreateReleaseCandidateBBDDPipelineStructure(String pipelineId) {

        super(pipelineId, "CreateReleaseCandidateBBDD_Pipeline")
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
				nombre   : "100-prepare-RC",
				resultado: "",
				orden    : 0
			   ],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-next-Minor-Master",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "300-push-repo-url",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}