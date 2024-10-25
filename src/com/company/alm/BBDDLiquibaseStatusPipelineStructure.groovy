package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class BBDDLiquibaseStatusPipelineStructure extends PipelineStructure {

    BBDDLiquibaseStatusPipelineStructure() {
        this(null)
    }

    BBDDLiquibaseStatusPipelineStructure(String pipelineId) {
        super(pipelineId, "BBDDLiquibaseStatus_Pipeline")
    }

    BBDDLiquibaseStatusPipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
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
                 nombre   : "100-init-command",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "110",
				estado   : "inactive",
				nombre   : "110-report-bbdd-status",
				resultado: "",
				orden    : 0
				]
        ]
    }

}
