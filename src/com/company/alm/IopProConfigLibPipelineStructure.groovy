package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0185731*
 */

class IopProConfigLibPipelineStructure extends PipelineStructure {


    public IopProConfigLibPipelineStructure(String pipelineId) {
        super(pipelineId, "IopProConfigLib_Pipeline")
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
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "300-copy-config-files",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "310",
				 estado   : "inactive",
				 nombre   : "310-refresh-micros",
				 resultado: "",
				 orden    : 0
				]
        ]
    }

}