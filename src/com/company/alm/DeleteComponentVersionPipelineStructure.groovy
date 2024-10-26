package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0198324*
 */

class DeleteComponentVersionPipelineStructure extends PipelineStructure {

    DeleteComponentVersionPipelineStructure() {
        this(null)
    }

    DeleteComponentVersionPipelineStructure(String pipelineId) {
        super(pipelineId, "Delete_Component_Version_Pipeline")
    }

    DeleteComponentVersionPipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
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
                 nombre   : "100-init-pipeline",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-validate-component-dependecies",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "300-delete-component-from-environment",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "305",
				 estado   : "inactive",
				 nombre   : "305-undeploy-artifact-from-cloud",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "310",
                 estado   : "inactive",
                 nombre   : "310-delete-config-files",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "500",
                 estado   : "inactive",
                 nombre   : "500-apimanager-technicalservices-registration",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
