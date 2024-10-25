package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class IopProPipelineStructure extends PipelineStructure {


    public IopProPipelineStructure(String pipelineId) {
        super(pipelineId, "IopPro_Pipeline")
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
				[id       : pipelineId + "110",
				 estado   : "inactive",
				 nombre   : "110-check-icp-availability",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "250",
			     estado   : "inactive",
				 nombre   : "250-error-translations",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "251",
				 estado   : "inactive",
				 nombre   : "251-deploy-bbdd-scripts",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "300-copy-config-files",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "400",
                 estado   : "inactive",
                 nombre   : "400-deploy-to-cloud-icp",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "401",
                 estado   : "inactive",
                 nombre   : "401-deploy-to-cloud",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "402",
				 estado   : "inactive",
				 nombre   : "402-post-deploy",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "500",
				 estado   : "inactive",
				 nombre   : "500-run-remote-it",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "600",
				 estado   : "inactive",
				 nombre   : "600-redirect-all-services-to-new-micro",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "700",
                 estado   : "inactive",
                 nombre   : "700-verify-endpoints",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "800",
                 estado   : "inactive",
                 nombre   : "800-promote-mule-contract",
                 resultado: "",
                 orden    : 0
                ]

        ]
    }

}
