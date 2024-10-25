package com.caixabank.absis3.util

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource

import java.lang.reflect.Method
import java.nio.file.Files
import java.util.function.Consumer
import java.util.function.Predicate

import org.apache.commons.io.FilenameUtils
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.MetaClassHelper
import org.junit.jupiter.api.BeforeEach
import org.kohsuke.groovy.sandbox.impl.Super
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

import com.caixabank.absis3.PipelineData
import com.caixabank.absis3.PomXmlStructure
import com.lesfurets.jenkins.unit.BasePipelineTest
import com.lesfurets.jenkins.unit.PipelineTestHelper
import com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration
import com.lesfurets.jenkins.unit.global.lib.LibraryLoader.LibraryLoadingException

public class AbsisPipelineTest  extends BasePipelineTest {

	def workspace = "dummy"

	/**
	 * Obligatory SetUp BeforeEach Test
	 */
	@Override
	void setUp() {
		super.setUp();

		super.helper.registerAllowedMethod("pipeline", [Closure])
		super.helper.registerAllowedMethod("stages", [Closure])
		super.helper.registerAllowedMethod("stage", [Closure])
		super.helper.registerAllowedMethod("steps", [Closure])
		super.helper.registerAllowedMethod("script", [Closure])

		super.helper.registerAllowedMethod('agent', [Closure])
		super.helper.registerAllowedMethod('docker', [Closure])
				
		super.helper.registerAllowedMethod('sh', [String], { args ->
			println("sh command: ${args}")
		})

		super.helper.registerAllowedMethod('image', [String], { String image ->
			println("agent: using image docker-> ${image}")
		})

		super.helper.registerAllowedMethod('args', [String],  { String args ->
			println("agent: using image docker arguments -> ${args}")
		})

		super.helper.registerAllowedMethod('registryUrl', [String], { String registryUrl ->
			println("agent:  using docker registry -> ${registryUrl}")
		})

		super.helper.registerAllowedMethod('options', [Closure])

		super.helper.registerAllowedMethod("gitLabConnection", [String], { String gitlab ->
			println("options:  gitLabConnection -> ${gitlab}")
		})
		super.helper.registerAllowedMethod("timestamps")
		super.helper.registerAllowedMethod("credentials", [String], { String credential ->
			println("options:  credentials -> ${credential}")
		})

		super.helper.registerAllowedMethod('environment', [Closure])

		super.helper.registerAllowedMethod("when", [Closure])
		super.helper.registerAllowedMethod("expression", [Closure])

		super.helper.registerAllowedMethod("waitUntil", [Closure])
		
		super.helper.registerAllowedMethod('readProperties', [Map], { args -> helper.readFile(args) })
		
		super.helper.registerAllowedMethod("configFileProvider", [ArrayList, Closure])
		super.helper.registerAllowedMethod("configFile", [Map], { Map map ->
			super.helper.binding.setVariable(map.variable, map.fileId)
		})

		super.helper.registerAllowedMethod("withSonarQubeEnv", [String, Closure])

		// Mock GPL
		super.helper.registerAllowedMethod("sendStageStartToGPL", [
			PomXmlStructure,
			PipelineData,
			String
		], { PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId ->
			println("sendStageStartToGPL ${stageId}")
		})

		super.helper.registerAllowedMethod("sendStageEndToGPL", [
			PomXmlStructure,
			PipelineData,
			String
		], { PomXmlStructure pomXmlStructure, PipelineData pipelineData, String stageId ->
			println("sendStageEndToGPL ${stageId}")
		})

		super.helper.registerAllowedMethod("sendStageEndToGPL", [
			PomXmlStructure,
			PipelineData,
			String,
			String,
			String,
			String
		], { PomXmlStructure pomXmlOrIClientInfo, PipelineData pipelineData, String stageId, String log, String environment, String state ->
			println("sendStageEndToGPL ${stageId}, Error: Log: ${log}, Environment: ${environment}, State: ${state}")
		})

		super.helper.binding.setVariable("WORKSPACE", workspace)
	}

	/**
	 * Load Library Pipeline
	 * @param library
	 * @throws Exception
	 */
	void loadJenkinsSharedLibrary(LibraryConfiguration library) throws Exception {
		println "Loading shared library ${library.name} with version ${library.defaultVersion}"

		long init = System.currentTimeMillis()

		try {
			def urls = library.retriever.retrieve(library.name,library.defaultVersion, library.targetPath)
			urls.forEach { URL url ->
				def file = new File(url.toURI())
				def varsPath = file.toPath().resolve('vars')
				super.helper.gse.getGroovyClassLoader().addURL(varsPath.toUri().toURL())
				if (varsPath.toFile().exists()) {
					def ds = Files.list(varsPath)
					ds.map { it.toFile() }
					.filter ({File it -> it.name.endsWith('.groovy') } as Predicate<File>)
					.map { FilenameUtils.getBaseName(it.name) }
					.forEach ({ String it -> this.registerScript(it) } as Consumer<String>)
					ds.close()
				}
			}
			println "Shared library ${library.name} Loaded!!"
		} catch (Exception e) {
			throw new LibraryLoadingException(e, library, library.defaultVersion)
		}

		long end = (System.currentTimeMillis() - init) / 1000

		println "Loaded ${end} s"
	}

	/**
	 * Add file to dummyworkspace
	 * @param pomXml
	 * @param fileName
	 */
	void addWorkSpaceFile(String file, String fileName) {
		def fileContents = new File("./tst/${file}").getText('UTF-8')
		super.helper.addReadFileMock("${workspace}/${fileName}", fileContents)
	}

	/**
	 * Load JenkinsConfig
	 */
	void loadJenkinsEnvVars() {
		def options = new DumperOptions()
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)

		Yaml yml = new Yaml(options)
		Map valuesYml = yml.load(("./tst/resources/jaasconfig/config.yml" as File).text)
		def jcascConfig = valuesYml.get("master").get("JCasC").get("configScripts").get("jenkins")

		Map jenkins = yml.load(jcascConfig)
		def enVarsList = jenkins.get("jenkins").get("globalNodeProperties")[0].get("envVars").get("env")

		def envVars = [:]
		enVarsList.each { it -> envVars.put(it.key, it.value)}
		this.binding.setVariable("env", envVars)
	}

	/**
	 * Add Single entry to EnvVars
	 * @param key
	 * @param value
	 */
	void addJenkinsEnvVars(String key, String value) {
		this.binding.getVariable("env").put(key, value)
	}

	/**
	 * Configure Mocks Clouse
	 */
	void configureMocks() {
		// For Optional overriding
	}

	Script getPipelineScript(String pipeline) {
		return loadScript("vars/${pipeline}.groovy")
	}

	/**
	 * Execute pipeline with parameters
	 * @param pipeline
	 * @param pipelineParams
	 */
	Object executePipeline(String pipeline, Object ...args) {
		def script = loadScript("vars/${pipeline}.groovy")
		Object result
		if (args == null || args.length == 0) {
			result = script.call()
		} else {
			result = script.call(args)
		}

		printCallStack()

		return result;
	}


	void executeJenkinsFile(String pipeline) {
		def script = loadScript("./tst/resources/${pipeline}.jenkins")
		script.run()
		printCallStack()
	}


	def toList(value) {
		[value].flatten().findAll { it != null }
	}

	/**
	 * Add Mocked Function
	 * @param scriptName
	 * @param typeParams
	 * @param callBack
	 */
	void registerMockedScript(String scriptName, List<Class> args, Closure callBack) {
		super.helper.registerAllowedMethod(scriptName, args, callBack)
	}

	/**
	 * Add Full Script to Execution Context
	 * @param scriptName
	 */
	void registerScript(String scriptName) {

		try {
			Script  classScript = helper.loadScript("vars/${scriptName}.groovy", binding)

			classScript.getMetaClass().getMethods().findAll{ MetaMethod metaMethod ->  metaMethod.getName().equals("call") }.forEach ({ MetaMethod metaMethod ->
				def nativeParameters = metaMethod.getNativeParameterTypes()

				if (nativeParameters.size() > 0) {
					super.helper.registerAllowedMethod(scriptName, toList(nativeParameters), { Object ... args ->
						return classScript.invokeMethod("call", args)
					})
				} else {
					super.helper.registerAllowedMethod(scriptName, { Closure c ->
						return classScript.call()
					})
				}
			})
			println("Script ${scriptName} Loaded!!")
		} catch (Exception e) {
			println("Script ${scriptName} Not Loaded!!")
		}
	}
}
