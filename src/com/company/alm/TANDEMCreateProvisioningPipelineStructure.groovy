package com.caixabank.absis3

class TANDEMCreateProvisioningPipelineStructure extends PipelineStructure {


    TANDEMCreateProvisioningPipelineStructure(String pipelineId, String name = "TANDEM_Provisioning_Pipeline") {
        super(pipelineId, name)
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
                 nombre   : "init",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "020",
                 estado   : "inactive",
                 nombre   : "download-tandem-artifact-and-update",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "050",
                 estado   : "inactive",
                 nombre   : "push-to-config-server",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "060",
                 estado   : "inactive",
                 nombre   : "deploy-tandem-lib",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "070",
                 estado   : "inactive",
                 nombre   : "publish-artifact",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "470",
                 estado   : "inactive",
                 nombre   : "refresh-connector-configuration",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "080",
                 estado   : "inactive",
                 nombre   : "prepare-result-for-next-step",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
