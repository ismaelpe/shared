package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class CreateReleaseConfigLibPipelineStructure extends PipelineStructure {


    CreateReleaseConfigLibPipelineStructure(String pipelineId) {
        super(pipelineId, "CreateReleaseConfigLib_Pipeline")
    }

    CreateReleaseConfigLibPipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
        super(pipelineIdParam, nombrePipelineParam)
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
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-prepare-Release",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "410",
                 estado   : "inactive",
                 nombre   : "410-copy-config-files",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "700",
                 estado   : "inactive",
                 nombre   : "700-push-Release-to-git",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "800",
                 estado   : "inactive",
                 nombre   : "800-deploy-artifactory",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "810",
				 estado   : "inactive",
				 nombre   : "810-refresh-micros",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "910",
                 estado   : "inactive",
                 nombre   : "910-publish-artifact-catalog",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
