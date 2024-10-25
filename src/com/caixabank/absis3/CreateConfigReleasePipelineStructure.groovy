package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class CreateConfigReleasePipelineStructure extends PipelineStructure {


    CreateConfigReleasePipelineStructure(String pipelineId) {
        super(pipelineId, "CreateConfigRelease_Pipeline")
    }

    CreateConfigReleasePipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
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
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "300-validate-version",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "400",
				 estado   : "inactive",
				 nombre   : "400-error-translations",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "500",
                 estado   : "inactive",
                 nombre   : "500-copy-config-files",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "600",
				 estado   : "inactive",
				 nombre   : "600-refresh-properties-configuration",
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
				 nombre   : "800-publish-artifact-catalog",
				 resultado: "",
				 orden    : 0
			    ]
        ]
    }

}
