package com.caixabank.absis3

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class CreateReleasePipelineStructure extends PipelineStructure {


    CreateReleasePipelineStructure(String pipelineId) {
        super(pipelineId, "CreateRelease_Pipeline")
    }

    CreateReleasePipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
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
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-prepare-Release",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "210",
                 estado   : "inactive",
                 nombre   : "200-validate-dependencies-version",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "300-validate-version",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "400",
                 estado   : "inactive",
                 nombre   : "400-build",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "403",
					estado   : "inactive",
					nombre   : "403-checkmarx-scan",
					resultado: "",
					orden    : 0
				   ],
				[id       : pipelineId + "405",
					estado   : "inactive",
					nombre   : "405-error-translations",
					resultado: "",
					orden    : 0
				],
				[id       : pipelineId + "406",
				 estado   : "inactive",
				 nombre   : "406-deploy-bbdd-scripts",
				 resultado: "",
				 orden    : 0
			    ],
   
                [id       : pipelineId + "410",
                 estado   : "inactive",
                 nombre   : "410-copy-config-files",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "415",
					estado   : "inactive",
					nombre   : "415-deploy-micros-on-nexus",
					resultado: "",
					orden    : 0
				 ],
                [id       : pipelineId + "500",
                 estado   : "inactive",
                 nombre   : "500-deploy-to-cloud",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "501",
				 estado   : "inactive",
				 nombre   : "501-deploy-to-cloud-icp",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "503",
                 estado   : "inactive",
                 nombre   : "503-run-remote-it",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "504",
				 estado   : "inactive",
				 nombre   : "504-run-remote-it-on-old-version",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "505",
				 estado   : "inactive",
				 nombre   : "505-consolidate-cloud-icp",
				 resultado: "",
				 orden    : 0
				],
				[id       : pipelineId + "507",
				 estado   : "inactive",
				 nombre   : "507-post-deploy",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "510",
                 estado   : "inactive",
                 nombre   : "510-apimanager-technicalservices-registration",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "521",
				 estado   : "inactive",
				 nombre   : "521-copy-config-files-to-tst-icp",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "522",
				 estado   : "inactive",
				 nombre   : "522-deploy-to-tst-icp",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "530",
                 estado   : "inactive",
                 nombre   : "530-apimanager-technicalservices-registration-tst",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "540",
                 estado   : "inactive",
                 nombre   : "540-generate-archetype",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "550",
                 estado   : "inactive",
                 nombre   : "550-deploy-archetype",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "600",
                 estado   : "inactive",
                 nombre   : "600-publish-client",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "700",
                 estado   : "inactive",
                 nombre   : "700-push-Release-to-git",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "800",
                 estado   : "inactive",
                 nombre   : "800-deploy-nexus",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "910",
                 estado   : "inactive",
                 nombre   : "910-publish-artifact-catalog",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "1000",
                 estado   : "inactive",
                 nombre   : "1000-promote-mule-contract",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
