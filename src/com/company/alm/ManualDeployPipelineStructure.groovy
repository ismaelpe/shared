package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class ManualDeployPipelineStructure extends PipelineStructure {


    public ManualDeployPipelineStructure(String pipelineId) {
        super(pipelineId, "ManualDeploy_Pipeline")
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
				[id       : pipelineId + "105",
				estado   : "inactive",
				nombre   : "105-check-cloud-availability",
				resultado: "",
				orden    : 0
			    ],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-build",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "220",
				 estado   : "inactive",
				 nombre   : "220-copy-config-files",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "300-deploy",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "400",
                 estado   : "inactive",
                 nombre   : "400-apimanager-technicalservices-registration",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "405",
                 estado   : "inactive",
                 nombre   : "405-apimanager-technicalservices-registration",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
