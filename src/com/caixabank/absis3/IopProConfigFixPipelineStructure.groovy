package com.caixabank.absis3

class IopProConfigFixPipelineStructure extends PipelineStructure {


    public IopProConfigFixPipelineStructure(String pipelineId) {
        super(pipelineId, "ConfigFix_Pro_Pipeline")
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
                 nombre   : "100-get-git-repo",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-error-translations",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "210",
                 estado   : "inactive",
                 nombre   : "210-copy-config-files",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "300-refresh-properties-configuration",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}