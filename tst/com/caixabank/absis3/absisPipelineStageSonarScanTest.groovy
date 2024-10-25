package com.caixabank.absis3

import static org.junit.jupiter.api.Assertions.*

import com.caixabank.absis3.util.AbsisPipelineTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class absisPipelineStageSonarScanTest extends AbsisPipelineTest {

	def pipelineParams
	PipelineData pipelineData
	BranchStructure branchStructure
	PomXmlStructure pomXmlStructure

	/**
	 * Inicialización del test
	 */
	@BeforeEach
	void initTest() {
		// Obligatorio para setear correctamente los metodos
		this.setUp()

		// Registramos los scripts necesarios para poder ejecutar el test
		this.registerScript("infoGitToPipelineData")
		this.registerScript("validateDependenciesForDataServices")
		this.registerScript("sonarScanWorkspace")

		// Por si queremos cargar la config de Jenkins
		this.loadJenkinsEnvVars()

		// Add Dummy Jenkins BUILD_TAG
		this.addJenkinsEnvVars("BUILD_TAG", "999")
		this.addJenkinsEnvVars("gitlabBranch", "master")

		// Load File from test resources
		this.addWorkSpaceFile("resources/pom.MS.SRV.xml", "pom.xml")

		this.pipelineParams 	= [type: 'SIMPLE', subType: 'MICRO_ARCH', changelog: true]

		this.pipelineData 		= this.getPipelineScript("getInfoGit").call()
		this.branchStructure	= this.getPipelineScript("getBranchInfo").call()

		pipelineData.init(branchStructure, pipelineParams.subType, pipelineParams.type, pipelineParams.get('isArchetype', false), pipelineParams.get('archetypeModel', './'))

		this.pomXmlStructure	= this.getPipelineScript("analizePomXml").call(pipelineParams.type, pipelineParams.subType)

		pipelineData.garArtifactType = GarAppType.MICRO_SERVICE
	}

	/**
	 * Ejecución de la pipeline
	 */
	@Test
	void testSonarScanFailedMavenCommand_FailedRunMavenGoalWithRetries() {
		this.registerScript("runMavenGoalWithRetries")
		
		this.registerMockedScript("kpiLogger", [KpiData], { KpiData kpiData ->
			println ("Mocked kpiData")
		})
		
		this.registerMockedScript("runMavenCommand", [			
			String,
			Map
		], {String mvnCommand, Map parameters ->
			throw new MavenGoalExecutionException (MavenGoalExecutionFailureErrorDecoder.getErrorFromLog("Error Mock", -1))
		})

		assertThrowsExactly(MavenGoalExecutionException.class, {
			this.getPipelineScript("absisPipelineStageSonarScan").call(this.pomXmlStructure, this.pipelineData, "412")
		})
	}

	@AfterEach
	void after() {
		this.printCallStack()
	}
}
