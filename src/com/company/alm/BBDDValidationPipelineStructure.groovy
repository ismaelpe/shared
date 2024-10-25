package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class BBDDValidationPipelineStructure extends PipelineStructure {

    BBDDValidationPipelineStructure() {
        this(null)
    }

    BBDDValidationPipelineStructure(String pipelineId) {
        super(pipelineId, "BBDDValidation_Pipeline")
    }

    BBDDValidationPipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
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
					nombre   : "110-validate-ddl-bbdd",
					resultado: "",
					orden    : 0
				]
        ]
    }

}
