package com.caixabank.absis3

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import com.caixabank.absis3.util.AbsisPipelineTest

class sendLitmidRequestTest extends AbsisPipelineTest {

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
		this.registerScript("retreiveLitmitErrors")

		// Por si queremos cargar la config de Jenkins
		this.loadJenkinsEnvVars()

		// Add Dummy Jenkins BUILD_TAG
		this.addJenkinsEnvVars("BUILD_TAG", "999")
		this.addJenkinsEnvVars("gitlabBranch", "master")
		
		// Load File from test resources
		this.addWorkSpaceFile("resources/pom.MS.SRV.xml", "pom.xml")

		this.pipelineParams 		= [type: 'SIMPLE', subType: 'MICRO_ARCH', changelog: true]

		this.pipelineData 			= this.getPipelineScript("getInfoGit").call()
		this.branchStructure		= this.getPipelineScript("getBranchInfo").call()

		pipelineData.init(branchStructure, pipelineParams.subType, pipelineParams.type, pipelineParams.get('isArchetype', false), pipelineParams.get('archetypeModel', './'))

		this.pomXmlStructure		= this.getPipelineScript("analizePomXml").call(pipelineParams.type, pipelineParams.subType)

		pipelineData.garArtifactType = GarAppType.MICRO_SERVICE
		
		// Register mock
		this.registerMockedScript("kpiLogger", [KpiData], { KpiData kpiData ->
			println ("Mocked kpiData")
		})
		
		this.registerMockedScript("checkEndpoint", [
			String,
			Map
		], {String command, Map params ->
			return [status: '200']
		})
	}

	/**
	 * Ejecución de la pipeline
	 */
	@Test
	void testGetLimidError_Ok() {
		this.getPipelineScript("initGlobalVars").call()
		this.getPipelineScript("sendLitmidRequest").call(this.pipelineData, this.pomXmlStructure)
	}

	@AfterEach
	void after() {
		this.printCallStack()
	}
}
