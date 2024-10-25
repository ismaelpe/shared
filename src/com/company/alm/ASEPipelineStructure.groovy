package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author U0197181*
 */

class ASEPipelineStructure extends PipelineStructure {

    ASEPipelineStructure(String pipelineId) {
        super(pipelineId, "ASE_Pipeline")
    }

    ASEPipelineStructure(String pipelineId, String nombrePipelineParam) {
        super(pipelineId, nombrePipelineParam)
    }

    @Override
    def getResult(boolean result) {
        return resultPipelineData.getResult(result)
    }


    @Override
    def getStages() {
        return [
            [id       : pipelineId + "010",
             estado   : "inactive",
             nombre   : "010-get-git-info",
             resultado: "",
             orden    : 0
            ],
            [id       : pipelineId + "020",
             estado   : "inactive",
             nombre   : "020-getting-last-committed-ase-contract",
             resultado: "",
             orden    : 0
            ],
            [id       : pipelineId + "030",
             estado   : "inactive",
             nombre   : "030-prepare-variables-and-calculate-version",
             resultado: "",
             orden    : 0
            ],
            [id       : pipelineId + "040",
             estado   : "inactive",
             nombre   : "040-create-project-from-archetype",
             resultado: "",
             orden    : 0
            ],
            [id       : pipelineId + "050",
             estado   : "inactive",
             nombre   : "050-deploying-new-project",
             resultado: "",
             orden    : 0
            ],
            [id       : pipelineId + "060",
             estado   : "inactive",
             nombre   : "060-publish-artifact-catalog",
             resultado: "",
             orden    : 0
            ],
            [id       : pipelineId + "070",
             estado   : "inactive",
             nombre   : "070-prepare-result-for-next-step",
             resultado: "",
             orden    : 0
            ]
        ]
    }

}
