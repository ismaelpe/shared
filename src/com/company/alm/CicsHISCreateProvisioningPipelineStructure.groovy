package com.project.alm

class CicsHISCreateProvisioningPipelineStructure extends PipelineStructure {


    CicsHISCreateProvisioningPipelineStructure(String pipelineId) {
        super(pipelineId, "CicsHIS_Provisioning_Pipeline")
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
                [id       : pipelineId + "010",
                 estado   : "inactive",
                 nombre   : "010-validating-agileworkid",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "020",
                 estado   : "inactive",
                 nombre   : "030-download-starter-artifact-and-update",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "025",
                 estado   : "inactive",
                 nombre   : "025-validating-agileworkid",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "030",
                 estado   : "inactive",
                 nombre   : "020-download-cics-artifact-and-update",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "040",
                 estado   : "inactive",
                 nombre   : "040-deploy-cics-ftp",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "050",
                 estado   : "inactive",
                 nombre   : "050-deploy-starter-nexus",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "060",
                 estado   : "inactive",
                 nombre   : "060-deploy-cics-nexus",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "070",
                 estado   : "inactive",
                 nombre   : "070-publish-artifact-catalog",
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