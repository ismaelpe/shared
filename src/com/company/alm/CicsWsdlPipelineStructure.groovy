package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class CicsWsdlPipelineStructure extends CreateReleasePipelineStructure {


    CicsWsdlPipelineStructure(String pipelineId) {
        super(pipelineId, "CicsWsdl_Pipeline")
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
                 nombre   : "010-getting-last-committed-wsdl-file",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "011",
                 estado   : "inactive",
                 nombre   : "011-getting-txNumber-and-cicsType",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "012",
                 estado   : "inactive",
                 nombre   : "012-getting-artifactId",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "013",
                 estado   : "inactive",
                 nombre   : "013-generating-cics-wsdl-artifact-and-deploy-into-nexus",
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
                ]
        ]
    }

}