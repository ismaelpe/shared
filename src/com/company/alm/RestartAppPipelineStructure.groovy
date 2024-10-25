package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class RestartAppPipelineStructure extends PipelineStructure {


    RestartAppPipelineStructure(String pipelineId) {
        super(pipelineId, "RestartApp_Pipeline")
    }

    RestartAppPipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
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
                 nombre   : "100-get-app-icp",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-restart-app-icp",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
