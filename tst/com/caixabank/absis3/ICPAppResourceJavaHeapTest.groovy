package com.caixabank.absis3

import java.lang.reflect.Method
import org.junit.Test

class ICPAppResourceJavaHeapTest extends GroovyTestCase {

	@Test
	void test() {
		def valuesDeployed = "ddd"
		def app = "ddd"
		def center
		def namespace
		def icpEnv
		def startAndStop
		def stableOrNew
		def garApp
		def jvmConfig
		def scalingMap
		def icpResources
		def garType
		
		println (GlobalVars.CAMPAIGN_GAR_APP)
		
		
		def variables = [:]
		
		Binding binding = new Binding(variables)
		
		Class<?> clazz = Class.forName("changeBranch");
		Object instance = clazz.newInstance(binding);
		
		Method method = clazz.getDeclaredMethod("call", String.class)
		method.invoke(instance, "Golas")

		
		
		
		//Object ss = calcClass.invokeMethod("call", ["golas"])
		
	
		
	

		//scriptRunner.call("testScript", [valuesDeployed, app])
	}
}
