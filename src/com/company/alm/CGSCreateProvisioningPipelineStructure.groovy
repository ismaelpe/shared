package com.project.alm

class CGSCreateProvisioningPipelineStructure extends PipelineStructure {


    CGSCreateProvisioningPipelineStructure(String pipelineId) {
        super(pipelineId, "CGS_Provisioning_Pipeline")
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
                 nombre   : "000-init",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "020",
                 estado   : "inactive",
                 nombre   : "020-download-cgs-artifact-and-update",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "050",
                 estado   : "inactive",
                 nombre   : "050-push-to-config-server",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "060",
                 estado   : "inactive",
                 nombre   : "060-deploy-cgs-lib",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "070",
                 estado   : "inactive",
                 nombre   : "070-publish-artifact",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "080",
                 estado   : "inactive",
                 nombre   : "080-prepare-result-for-next-step",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
