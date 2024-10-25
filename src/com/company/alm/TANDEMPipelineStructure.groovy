package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author U0197181*
 */

class TANDEMPipelineStructure extends PipelineStructure {


    TANDEMPipelineStructure(String pipelineId) {
        super(pipelineId, "TANDEM_Pipeline")
    }

    @Override
    def getResult(boolean result) {
        return resultPipelineData.getResult(result)
    }


    @Override
    def getStages() {
        return [
                [id       : pipelineId + "000",
                 estado   : "inactive",
                 nombre   : "get-git-info",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "010",
                 estado   : "inactive",
                 nombre   : "getting-last-committed-tandem-file",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "011",
                 estado   : "inactive",
                 nombre   : "checking-files-pushed",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "070",
                 estado   : "inactive",
                 nombre   : "prepare-variables-and-calculate-version",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "150",
                 estado   : "inactive",
                 nombre   : "create-transaction-model",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "250",
                 estado   : "inactive",
                 nombre   : "create-project-from-archetype",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "push-to-config-server",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "350",
                 estado   : "inactive",
                 nombre   : "removing-previous-directory",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "400",
                 estado   : "inactive",
                 nombre   : "deploying-new-project",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "450",
                 estado   : "inactive",
                 nombre   : "publish-artifact-catalog",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "470",
                 estado   : "inactive",
                 nombre   : "refresh-connector-configuration",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "500",
                 estado   : "inactive",
                 nombre   : "prepare-result-for-next-step",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
