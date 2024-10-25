package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author absis
 */
class RefreshLiteralsPipelineStructure extends PipelineStructure {

    RefreshLiteralsPipelineStructure() {
        super(null, "RefreshLiterals_Pipeline")
    }

    RefreshLiteralsPipelineStructure(String pipelineId) {
        super(pipelineId, "RefreshLiterals_Pipeline")
    }

    RefreshLiteralsPipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
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
                 nombre   : "100-init",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-extract-literals",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "300",
                    estado   : "inactive",
                    nombre   : "300-transform-literals",
                    resultado: "",
                    orden    : 0
                ],
                [id       : pipelineId + "400",
                 estado   : "inactive",
                 nombre   : "400-load-literals",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "500",
                 estado   : "inactive",
                 nombre   : "500-refresh-micro-literals",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
