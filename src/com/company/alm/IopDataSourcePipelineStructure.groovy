package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class IopDataSourcePipelineStructure extends PipelineStructure {


    public IopDataSourcePipelineStructure(String pipelineId) {
        super(pipelineId, "IopDataSource_Pipeline")
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
                 nombre   : "100-init-data",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}