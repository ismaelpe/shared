package com.caixabank.absis3

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import com.caixabank.absis3.util.AbsisPipelineTest


/**
 * Test para vars/absisPipelineBuildTest
 * @author Ismael Palacios Estudillo
 *
 */
class absisPipelineBuildTest extends AbsisPipelineTest {
	
	def pipelineParams

	/**
	 * Inicialización del test
	 */
	@BeforeEach
	void initTest() {
		// Obligatorio para setear correctamente los metodos
		this.setUp()
		
		// Cargamos la shared del vars
		//def jenkinsSharedLibrary = library().name('absis3-services').defaultVersion('1.0.0-SNAPSHOT').allowOverride(true).implicit(true).targetPath('<notNeeded>')	.retriever(projectSource()).build()
		//this.loadJenkinsSharedLibrary(jenkinsSharedLibrary)
		
		// Por si queremos cargar la config de Jenkins
		this.loadJenkinsEnvVars()	
		
		// Por si queremos añadir variables de entorno ad-hoc			
		this.addJenkinsEnvVars("gitlabMergeRequestLastCommit", "cf7a602b1cc943f8369c5391bfedd82a184df4ef")
		
		// Configuramos los parametros de entrada
		this.pipelineParams = [type: 'SIMPLE', subType: 'MICRO_ARCH', changelog: true]
	}

	/**
	 * Ejecución de la pipeline
	 */
	@Test
	void testAsisPipelineBuild() {
		//this.executePipeline("absisPipelineBuild", this.pipelineParams)
		this.executePipeline("changeBranch", "HolaMundo")
	}
}
