package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0185731*
 */

class CreateReleaseCandidateConfigLibPipelineStructure extends PipelineStructure {


    public CreateReleaseCandidateConfigLibPipelineStructure(String pipelineId) {

        super(pipelineId, "CreateRCConfigLib_Pipeline")
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