package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author U0198730*
 */

class CicsHISPipelineStructure extends PipelineStructure {


    CicsHISPipelineStructure(String pipelineId) {
        super(pipelineId, "CicsHIS_Build_Pipeline")
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
                 nombre   : "010-getting-last-committed-dll-file",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "011",
                 estado   : "inactive",
                 nombre   : "011-checking-files-pushed",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "013",
                 estado   : "inactive",
                 nombre   : "013-getting-txNumber-and-cicsType",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "014",
                 estado   : "inactive",
                 nombre   : "014-generate-info-starter",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "015",
                 estado   : "inactive",
                 nombre   : "015-generating-installation-package",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "016",
                 estado   : "inactive",
                 nombre   : "016-installing-package-into-his",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "017",
                 estado   : "inactive",
                 nombre   : "017-generating-cics-his-artifact-and-deploy-into-nexus",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "020",
                 estado   : "inactive",
                 nombre   : "020-removing-previous-directory",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "030",
                 estado   : "inactive",
                 nombre   : "030-create-starter-from-wsdl",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "040",
                 estado   : "inactive",
                 nombre   : "040-install-starter-project-into-nexus",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "050",
                 estado   : "inactive",
                 nombre   : "050-publish-artifact-catalog",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "060",
                 estado   : "inactive",
                 nombre   : "060-prepare-result-for-next-step",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}