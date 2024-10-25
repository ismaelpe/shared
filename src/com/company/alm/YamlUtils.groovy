package com.project.alm
import groovy.io.FileType
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

class YamlUtils {

	Yaml yml
	
	YamlUtils(){
		def options = new DumperOptions()
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
		yml = new Yaml(options)
	}

	void validateYaml(String content) {
		try {
			Iterable<Object> objects = yml.loadAll(content);
			for (Object ymlMap : objects) {
				Map map = (Map) ymlMap;
			}
		}catch(RuntimeException e) {
			throw new RuntimeException("Yaml file with content:\n " + content + " \n,is not well formed",e)
		}
	}
	
	void validateYamlFile(File file) {
		validateYaml(file.text)
	}
	
	void validateYamlsFolder(File folder) {
		folder.eachFileRecurse (FileType.FILES) { file ->
			validateYamlFile(file)
		}
	}
}
