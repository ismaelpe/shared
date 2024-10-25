import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.Java11Buildpack
import com.caixabank.absis3.Java8Buildpack
import com.caixabank.absis3.JavaVersionType

/**
 * Obtiene la version de Java desde la variable de entorno JAVA_VERSION_TYPE que esta definida en los agentes.
 * @return
 */
def call() {
	def javaVersionTypeVar = env.JAVA_VERSION_TYPE

	if (JavaVersionType.valueOfType(env.JAVA_VERSION_TYPE) == JavaVersionType.JAVA11) {
		printOpen("Find JAVA_VERSION_TYPE=${javaVersionTypeVar} -> Java11Buildpack resolved!", EchoLevel.ALL)
		return new Java11Buildpack()
	} else {		
		printOpen("Find JAVA_VERSION_TYPE=${javaVersionTypeVar} -> Java8Buildpack resolved!", EchoLevel.ALL)
		return new Java8Buildpack()
	}
}
