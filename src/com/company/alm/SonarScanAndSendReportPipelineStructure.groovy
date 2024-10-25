package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class SonarScanAndSendReportPipelineStructure extends PipelineStructure {


    SonarScanAndSendReportPipelineStructure(String pipelineId) {
        super(pipelineId, "QA_Pipeline")
    }

    SonarScanAndSendReportPipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
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
                 nombre   : "100-check-micro-version",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-build-and-test",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "300-sonar-scan",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "400",
                 estado   : "inactive",
                 nombre   : "400-sonar-quality-gate",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "500",
                 estado   : "inactive",
                 nombre   : "500-send-sonar-report",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }
}
