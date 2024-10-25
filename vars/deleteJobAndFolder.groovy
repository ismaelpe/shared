import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*
import hudson.scm.*
import hudson.tasks.*
import com.cloudbees.hudson.plugins.folder.*
import java.util.ArrayList
import com.caixabank.absis3.*


def call(def typeApp, def nameApp) {

	
	def nameAppRegularizado=nameApp.trim().toUpperCase()
		//Consultaremos el componente
	def microOwnerOfTheSecret
	def response1 = sendRequestToAbsis3MS(
			 'GET',
			 "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${typeApp}/${nameAppRegularizado}",
			 null,
			 "${GlobalVars.CATALOGO_ABSIS3_ENV}",
			 [
				 kpiAlmEvent: new KpiAlmEvent(
					 null, null,
					 KpiAlmEventStage.UNDEFINED,
					 KpiAlmEventOperation.CATMSV_HTTP_CALL)
			 ])
		 
		 if (response1.status == 200) {
			 //microOwnerOfTheSecret = readJSON text: response1.content
			 microOwnerOfTheSecret = response1.content
			 microOwnerOfTheSecret.srvDeployTypeId='N'
             printOpen("El micro desplegado es ${microOwnerOfTheSecret.id}", EchoLevel.INFO)
		 }else {
			 throw new Exception("La aplicacion indicada no existe ${typeApp}.${nameAppRegularizado}"); 
		 }
    def response = sendRequestToAbsis3MS(
			'PUT',
			"${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app",
			microOwnerOfTheSecret,
			"${GlobalVars.CATALOGO_ABSIS3_ENV}",
			[
				kpiAlmEvent: new KpiAlmEvent(
					null, null,
					KpiAlmEventStage.UNDEFINED,
					KpiAlmEventOperation.CATMSV_HTTP_CALL)
			])
		
		if (response.status == 200) {
            printOpen("Cataleg actualitzat correctament", EchoLevel.INFO)
		}else {
            printOpen("Error al actualitzar el cataleg", EchoLevel.ERROR)
		}

}
 

def call(def typeApp, def domain, def nameApp, def empresa, def modulo) {
	
	def folder=new ArrayList()
	folder.add("absis3")
	folder.add("services")
	
	int nivel=0
	
	if (typeApp == "Library" || typeApp == "SRV.LIB" || typeApp == "ARQ.LIB") {
		if (!nameApp.contains('-lib')) nameApp = nameApp + '-lib'
	} else if (typeApp == "MicroService" || typeApp == "SRV.MS" || typeApp == "DataService" || typeApp == "SRV.DS" || typeApp == 'ARQ.MIA' || typeApp == "SRV.BFF") {
		if (!nameApp.contains('-micro')) nameApp = nameApp + '-micro'
	} else if (typeApp == 'ARQ.MAP') {
		if (!nameApp.contains('-plugin')) nameApp = nameApp + '-plugin'
	} else if (typeApp == 'ARQ.CFG' || typeApp == "SRV.CFG") {
		if (!nameApp.contains('-conf')) nameApp = nameApp + '-conf'
	}
	def isArq = true
	def jenkinsPath = "absis3/services" 
	
	if (typeApp == "Library" || typeApp == "MicroService" || typeApp == "DataService"
		|| typeApp == "SRV.LIB" || typeApp == "SRV.DS" || typeApp == "SRV.MS" || typeApp == "SRV.CFG" ) {
		isArq = false
		folder.add("apps")
		folder.add(empresa.toLowerCase())
		jenkinsPath = jenkinsPath + "/apps" + "/" + empresa.toLowerCase()
		if (typeApp == "Library" || typeApp == "SRV.LIB") {
			 folder.add("common")
			 folder.add(domain)
			 jenkinsPath = jenkinsPath + "/common/" + domain
		}
		if (typeApp == "SRV.CFG") {
			 folder.add("conf")
			 folder.add(domain)
			 jenkinsPath = jenkinsPath + "/conf/" + domain
		}
		if (typeApp == "MicroService" || typeApp == "SRV.MS") {
			 folder.add("service")
			 folder.add(domain)
			 jenkinsPath = jenkinsPath + "/service/" + domain
		}
		if (typeApp == "DataService" || typeApp == "SRV.DS") {
			 folder.add("data-service")
			 folder.add(domain)
			 jenkinsPath = jenkinsPath + "/data-service/" + domain
		}
	
		if (empresa.toLowerCase().equals("bpi")) {
			folder.add(modulo)
			jenkinsPath = jenkinsPath + "/" + modulo
		}
	
	} else {
		//Domain puede llegar con '/'
		if (domain.contains('/')) {
			//El domain contiene una como minimo deberia parsear la informacion y sacar del domain y el folder.
			def tokens = domain.tokenize('/')
			def contador = 0
			def domainAux
	
			tokens.each {
				if (contador == 0) {
	
					domainAux = it
					contador = 1
	
				} else if (contador == 1) {
	
					modulo = it
					contador = 2
				}
			}
	
			domain = domainAux
	
		} else modulo = ''
	
		printOpen( "empresa: ${empresa}", EchoLevel.DEBUG)
		printOpen( "nameApp: ${nameApp}", EchoLevel.DEBUG)
		printOpen( "typeApp: ${typeApp}", EchoLevel.DEBUG)
		printOpen( "pathToRepo: ${pathToRepo}", EchoLevel.DEBUG)
		printOpen( "domain: ${domain}", EchoLevel.DEBUG)
		printOpen( "modulo: ${modulo}", EchoLevel.DEBUG)
	
		if (empresa.toLowerCase().equals("bpi")) {
			folder.add("arch")
			folder.add("bpi")
			folder.add(domain)
			jenkinsPath = jenkinsPath + "/arch/bpi/" + domain
		} else {
			folder.add("arch")
			folder.add(domain)
			jenkinsPath = jenkinsPath + "/arch/" + domain
		}
	
		printOpen( "jenkinsPath: ${jenkinsPath}", EchoLevel.DEBUG)
	
	
		if (modulo != null && modulo != '') {
			folder.add(domain)
			jenkinsPath = jenkinsPath + "/" + modulo
		}
	}


	def jen = Jenkins.instance
	jen.getItems().each{

		if(it instanceof Folder){
			if (it.name.equals(folder.get(0))) {
				processFolder(it,folder,nameApp,1)
				return true
			}			
			return
		}
	}
} 
void processJob(Item job, def folderList, def name, def level){
    printOpen("Se puede borrar", EchoLevel.INFO)
}
void processFolder(Item folder, def folderList, def name, def level){
	
	def encontrado=true
	if (level<folderList.size() ) {

		folder.getItems().any{

			if(it instanceof Folder && it.name.trim().equalsIgnoreCase(folderList.getAt(level))){
				processFolder(it,folderList,name,level+1)
				return true
			}else {
				encontrado=false
				return
			}
		}
	}else {
        printOpen("limite alcanzado, buscamos en los jobs de la carpeta ${folder.name}", EchoLevel.INFO)
		folder.getItems().any{
			if(it instanceof Job && it.name.trim().equalsIgnoreCase(name)){
				processJob(it,folderList,name,level+1)
				it.delete()				
				return true
			}else{
				encontrado=false
				return
			}
		}
	}
	if (encontrado==false) {
        printOpen("No hemos encontrado el proyecto ${name} path ${folderList} ${level}", EchoLevel.INFO)
	}
}
