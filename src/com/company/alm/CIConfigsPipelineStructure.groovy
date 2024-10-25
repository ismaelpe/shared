package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class CIConfigsPipelineStructure extends PipelineStructure {

    CIConfigsPipelineStructure() {
        this(null)
    }

    CIConfigsPipelineStructure(String pipelineId) {
        super(pipelineId, "CIConfigs_Pipeline")
    }

    CIConfigsPipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
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
                 nombre   : "init validate",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "150",
				 estado   : "inactive",
				 nombre   : "update-version",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "500",
                 estado   : "inactive",
                 nombre   : "copy-config-files",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "510",
				 estado   : "inactive",
				 nombre   : "refresh-micros",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "600",
                 estado   : "inactive",
                 nombre   : "changelog-file",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "700",
                 estado   : "inactive",
                 nombre   : "push-release-to-git",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "800",
                 estado   : "inactive",
                 nombre   : "deploy-artifactory",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "950",
                 estado   : "inactive",
                 nombre   : "publish-artifact-catalog",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
