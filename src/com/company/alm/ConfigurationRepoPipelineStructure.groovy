package com.project.alm

class ConfigurationRepoPipelineStructure extends PipelineStructure {

    ConfigurationRepoPipelineStructure(String projectName, String pipelineId) {
        super(pipelineId, projectName + "_Configuration_Pipeline")
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
                 nombre   : "100-get-git-info",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-validate-files",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }
}