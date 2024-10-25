package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class StressTestsPreparationPipelineStructure extends PipelineStructure {


    StressTestsPreparationPipelineStructure(String pipelineId) {
        super(pipelineId, "StressTestsPreparation_Pipeline")
    }

    StressTestsPreparationPipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
        super(pipelineIdParam, nombrePipelineParam)
    }

    @Override
    def getResult(boolean result) {
        return resultPipelineData.getResult(result)
    }


    @Override
    def getStages() {
        return [
			[
				id       : pipelineId + "100",
				estado   : "inactive",
				nombre   : "get-git-repo",
				resultado: "",
				orden    : 0
            ],
            [
				id       : pipelineId + "200",
				estado   : "inactive",
				nombre   : "prepare-wiremock-server",
				resultado: "",
				orden    : 0
            ],
            [
				id       : pipelineId + "300",
				estado   : "inactive",
				nombre   : "prepare-stress-micro",
				resultado: "",
				orden    : 0
            ]
        ]
    }

}
