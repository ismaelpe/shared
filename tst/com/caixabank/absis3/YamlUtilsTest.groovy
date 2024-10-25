package com.caixabank.absis3

import org.junit.Assert
import org.junit.Test

class YamlUtilsTest extends GroovyTestCase {

	private static final String VALID_CONTENT = "logging:\n  level:\n    org.springframework.jms: DEBUG"
	private static final String INVALID_CONTENT = "logging:\n  level:\n    org.springframework.jms: DEBUG\n PEPE"
	private static final String VALID_TWO_CONTENT = "spring:\n    config:\n        activate:\n            on-profile: test\n---\nspring:\n    config:\n        activate:\n            on-profile: prod"
	private static final String VALID_AND_INVALID_TWO_CONTENT = "spring:\n    config:\n        activate:\n            on-profile: test\n---\nspring:\n    config:\n        activate:\n            on-profile: prod\n PEPE"

	@Test
	void testValidYaml() {
		YamlUtils utils = new YamlUtils();
		utils.validateYaml(VALID_CONTENT)
	}

	@Test
	void testValidTwoYaml() {
		YamlUtils utils = new YamlUtils();
		utils.validateYaml(VALID_TWO_CONTENT)
	}

	@Test
	void testInvalidYaml() {
		YamlUtils utils = new YamlUtils();
		try {
			utils.validateYaml(INVALID_CONTENT)
			Assert.fail("This test should fail")
		}catch(RuntimeException e) {
			println e
		}
	}

	@Test
	void testInvalidTwoYaml() {
		YamlUtils utils = new YamlUtils();
		try {
			utils.validateYaml(VALID_AND_INVALID_TWO_CONTENT)
			Assert.fail("This test should fail")
		}catch(RuntimeException e) {
			println e
		}
	}

	@Test
	void testValidYamlFile() {
		def newFile = new File("new.yml")
		try {
			YamlUtils utils = new YamlUtils();
			newFile.write(VALID_CONTENT)
			utils.validateYamlFile(newFile)
		}finally {
			newFile.delete();
		}
	}

	@Test
	void testValidTwoYamlFile() {
		YamlUtils utils = new YamlUtils();
		def newFile = new File("new.yml")
		try {
			newFile.write(VALID_TWO_CONTENT)
			utils.validateYamlFile(newFile)
		}finally {
			newFile.delete();
		}
	}

	@Test
	void testInvalidYamlFile() {
		YamlUtils utils = new YamlUtils();
		def newFile = new File("new.yml")
		
		try {
			newFile.write(INVALID_CONTENT)
			utils.validateYamlFile(newFile)
			Assert.fail("This test should fail")
		}catch(RuntimeException e) {
			println e
		}finally {
			newFile.delete();
		}
	}

	@Test
	void testInvalidTwoYamlFile() {
		YamlUtils utils = new YamlUtils();
		def newFile = new File("new.yml")
		
		try {
			newFile.write(VALID_AND_INVALID_TWO_CONTENT)
			utils.validateYamlFile(newFile)
			Assert.fail("This test should fail")
		}catch(RuntimeException e) {
			println e
		}finally {
			newFile.delete();
		}
	}

	@Test
	void testValidYamlFolder() {
		YamlUtils utils = new YamlUtils();
		File folder = new File('./tst/resources/valid/yaml');
		utils.validateYamlsFolder(folder)
	}

	@Test
	void testInValidYamlFolder() {
		YamlUtils utils = new YamlUtils();
		File folder = new File('./tst/resources/invalid/yaml');
		try {
			utils.validateYamlsFolder(folder)
			Assert.fail("This test should fail")
		}catch(RuntimeException e) {
			println e
		}
	}
}
