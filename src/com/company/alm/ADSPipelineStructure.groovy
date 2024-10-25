package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author U0197181*
 */

class ADSPipelineStructure extends PipelineStructure {


    ADSPipelineStructure(String pipelineId) {
        super(pipelineId, "ADS_Pipeline")
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
                 nombre   : "000-get-git-info",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "010",
                 estado   : "inactive",
                 nombre   : "010-getting-last-committed-ads-file",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "011",
                 estado   : "inactive",
                 nombre   : "011-checking-files-pushed",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "070",
                 estado   : "inactive",
                 nombre   : "070-prepare-variables-and-calculate-version",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "150",
                 estado   : "inactive",
                 nombre   : "150-create-transaction-model",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "250",
                 estado   : "inactive",
                 nombre   : "250-create-project-from-archetype",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "300-push-to-config-server",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "350",
                 estado   : "inactive",
                 nombre   : "350-removing-previous-directory",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "400",
                 estado   : "inactive",
                 nombre   : "400-deploying-new-project",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "450",
                 estado   : "inactive",
                 nombre   : "450-publish-artifact-catalog",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "470",
                 estado   : "inactive",
                 nombre   : "470-refresh-connector-configuration",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "500",
                 estado   : "inactive",
                 nombre   : "500-prepare-result-for-next-step",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
