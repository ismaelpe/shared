import com.project.alm.*
import java.util.HashMap
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

/**
 * Obtiene el log en formato Yaml a partir de un Mapa en profundidad
 * @return String con las propiedades
 */
def call(Map mainMap) {

	//Filter Post-process
    Map cleanMap = new HashMap();
    executePostProcess(mainMap, cleanMap)

    DumperOptions options = new DumperOptions();
    options.setIndent(1);
    options.setPrettyFlow(true);
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    Yaml output = new Yaml(options);

    StringWriter writer =null;
     try {
        writer = new StringWriter();
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    output.dump(cleanMap, writer);
	return writer.toString()

}


def executePostProcess(Map it, Map cleanMap) {
	// Simplificamos el mapa; si existe solo una "subKey" unimos la de los dos niveles "key.subKey"
    it.each { k,v ->

        Map mapIt = new HashMap();

        if (!(v instanceof Map)) {

            if(!cleanMap.containsKey(k)) cleanMap.put(k,v)

        } else if (v.keySet()!=null && v.keySet().size()==1 && v.keySet()[0].contains(".")) {

            if(cleanMap.containsKey(k)) cleanMap.remove(k)
            cleanMap.put(k+"."+v.keySet()[0], v.get(v.keySet()[0]))

        } else if (v.keySet()!=null && v.keySet().size()==1) {

            if (cleanMap.containsKey(k)) cleanMap.remove(k)
            mapIt.put(k+"."+v.keySet()[0], v.get(v.keySet()[0]))
		    cleanMap.put(k+"."+v.keySet()[0], v.get(v.keySet()[0]))
            executePostProcess(mapIt,cleanMap)

        } else {

            v.each { k1, v1 ->
                if (!k1.contains(".")) mapIt.put(k1,v1)
            }
            if(!cleanMap.containsKey(k)) cleanMap.put(k,v)
            if(!mapIt.isEmpty()) executePostProcess(mapIt,v)

        }
    }
}