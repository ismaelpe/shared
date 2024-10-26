package com.project.alm

/**
 * Contains the structure of the executing pipeline
 * @author u0180790*
 */

class CIPipelineStructure extends PipelineStructure {

    CIPipelineStructure() {
        super(null, "CI_Pipeline")
    }

    CIPipelineStructure(String pipelineId) {
        super(pipelineId, "CI_Pipeline")
    }

    CIPipelineStructure(String pipelineIdParam, String nombrePipelineParam) {
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
                 nombre   : "100-init validate",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "105",
					estado   : "inactive",
					nombre   : "105-deploy-if-prototype",
					resultado: "",
					orden    : 0
				 ],
				[id       : pipelineId + "110",
				 estado   : "inactive",
				 nombre   : "110-deploy-ddl-bbdd",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "200",
                 estado   : "inactive",
                 nombre   : "200-validate-dependencies-version",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "250",
                 estado   : "inactive",
                 nombre   : "250-validate-dependencies-restrictions",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "300",
                 estado   : "inactive",
                 nombre   : "300-update-version",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "400",
                 estado   : "inactive",
                 nombre   : "400-validate-version",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "410",
                 estado   : "inactive",
                 nombre   : "410-build",
                 resultado: "",
                 orden    : 0
                ],
				[id       : pipelineId + "411",
				 estado   : "inactive",
				 nombre   : "411-error-translations",
				 resultado: "",
				 orden    : 0
				],
                [id       : pipelineId + "414",
                 estado   : "inactive",
                 nombre   : "414-push-release-to-git",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "415",
                 estado   : "inactive",
                 nombre   : "415-deploy-micro-artifactory-cloud",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "420",
                 estado   : "inactive",
                 nombre   : "420-copy-config-files",
                 resultado: "",
                 orden    : 0
                ],
		        [id       : pipelineId + "501",
			     estado   : "inactive",
			     nombre   : "501-deploy-to-cloud",
			     resultado: "",
			     orden    : 0
		        ],
				[id       : pipelineId + "502",
                 estado   : "inactive",
                 nombre   : "502-refresh-properties-configuration",
                 resultado: "",
                 orden    : 0
				],
				[id       : pipelineId + "506",
                 estado   : "inactive",
                 nombre   : "506-consolidate-new-deploy",
                 resultado: "",
                 orden    : 0
				],
                [id       : pipelineId + "510",
                 estado   : "inactive",
                 nombre   : "510-changelog-file",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "525",
                 estado   : "inactive",
                 nombre   : "525-deploy-nexus",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "530",
                 estado   : "inactive",
                 nombre   : "530-publish-client",
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
                 nombre   : "600-generate-report",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "910",
                 estado   : "inactive",
                 nombre   : "910-publish-artifact-catalog",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "920",
                 estado   : "inactive",
                 nombre   : "920-apimanager-technicalservices-registration",
                 resultado: "",
                 orden    : 0
                ],
                [id       : pipelineId + "930",
                 estado   : "inactive",
                 nombre   : "930-promote-mule-contract",
                 resultado: "",
                 orden    : 0
                ]
        ]
    }

}
