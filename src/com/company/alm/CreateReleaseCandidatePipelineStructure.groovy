package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class CreateReleaseCandidatePipelineStructure extends PipelineStructure {


    public CreateReleaseCandidatePipelineStructure(String pipelineId) {

        super(pipelineId, "CreateReleaseCandidate_Pipeline")
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
				nombre   : "90-checkmarx-scan",
				resultado: "",
				orden    : 0
			   ],
                [id       : pipelineId + "110",
                 estado   : "inactive",
                 nombre   : "100-prepare-RC",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "150",
                 estado   : "inactive",
                 nombre   : "150-verify-PRO-state",
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