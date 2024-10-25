import com.project.alm.ArtifactType
import com.project.alm.EchoLevel
import com.project.alm.FileUtils
import com.project.alm.GitUtils
import com.project.alm.GlobalVars
import com.project.alm.PipelineData
import com.project.alm.PomXmlStructure
import com.project.alm.PipelineStructureType
import com.project.alm.GarAppType
import com.project.alm.Utilities
import groovy.json.JsonSlurperClassic





def getOpenedMicros(def days) {
    
    //def response=sendRequestToAbsis3MS('GET', "${GlobalVars.URL_CATALOGO_ABSIS3_TST}/app/traceability/PRO/BETA?daysToDest=${days}&onlyInstalled=true",null, "TST")
    def response=sendRequestToAbsis3MS('GET', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/traceability/PRO/BETA?daysToDest=${days}&onlyInstalled=true",null, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
    if (response.status == 200) {
       printOpen("Devolveremos los micros abiertos", EchoLevel.ALL)
       
       //def listMicros = new JsonSlurperClassic().parseText(response.content)
       def listMicros = response.content
       if (listMicros.size()>0) {
           printOpen("Devolvemos los micros abiertos ${listMicros.get(0)}", EchoLevel.ALL)
           return listMicros
       }else {
           return listMicros 
       }
    } else if (response.status == 404) {
        throw new Exception("Fallan los endpoints")
    }else {
        throw new Exception("Error al consultar los micros abiertos")
    }    
}

private sendEmail(def subject, def body, def replyTo, def from, def to) {
    printOpen("${body}-${replyTo}", EchoLevel.ALL)
    emailext(
        mimeType: 'text/html',
        replyTo: "${replyTo}",
        body: "${body}",
        subject: "[OpenServices] ${subject} ",
        from: "${from}",
        to: "${to}")
}

def notifyToAppTeam(def micros) {
    if (env.SEND_TO_ABSIS3_CATALOG!="" && env.SEND_TO_ABSIS3_CATALOG=="true") {
        micros.each{
            x-> 
                printOpen("Se procede notificar a la aplicacion ${x.appType}${x.garApp} para el cierre la ${x.major}.${x.minor}.${x.fix}.${x.typeVersion}", EchoLevel.ALL)
                //enviarCorreo al reponsable
                def usuarioReponsable=idecuaRoutingUtils.getResponsiblesAppEmailList(x.garApp, x.appType)
                printOpen("Enviaremos el correo a este señor ${usuarioReponsable}", EchoLevel.ALL)
                def subject = "Version ${x.major}.${x.minor}.${x.fix}.${x.typeVersion} de la app ${x.garApp} aún en cannary"
                def body = "<p>Buenos días,</p><p>No se ha aplicado el cierre a la versión <b>${x.major}.${x.minor}.${x.fix}.${x.typeVersion} de la app ${x.garApp}.</b></p><p> Debe proceder al cierre, lleva abierta desde ${x.installedOn}. </p><p> No podrá generar nuevas releases sin cerrar esta versión.</p>"+
                           "<p>Saludos </p> "             
                sendEmail(subject,body,null, GlobalVars.EMAIL_FROM_ALM,usuarioReponsable)                
        }
    }
    return null
}


def validateCoherence(def type, def application, def major, def minor, def fix, def versionType, def enviroment) {

    ///app/${type}/${application}/version/${major}/${minor}/${fix}/${versionType}/dependency/validate/${enviroment}
    def response=sendRequestToAbsis3MS('GET', "${GlobalVars.URL_CATALOGO_ABSIS3_PRO}/app/${type}/${application}/version/${major}/${minor}/${fix}/${versionType}/dependency/validate/${enviroment}", null, "${GlobalVars.CATALOGO_ABSIS3_ENV}")
    printOpen("El status de las peticiones son de ${response.status}", EchoLevel.ALL)
    if (response.status == 200) {
       printOpen("Validaciones de depencias correctas", EchoLevel.ALL)
    } else if (response.status == 400) {
        printOpen("Tenemos dependencias no validadas", EchoLevel.ALL)
        //def responseJson = new groovy.json.JsonSlurper().parseText(response.content)
        def responseJson = response.content
        printOpen("El json es de ${responseJson}", EchoLevel.ALL)
        def dependencies = responseJson.collect{ "\t\u2022 $it.appName:$it.major.$it.minor.$it.fix:$it.typeVersion"}.join("\n")

        throw new Exception("No deberia hacer rollback del micro por fallos de dependencias:\n${dependencies}\n Gestione o avise a sus invocantes.")
    } else {
        // Con la campaña en marcha no se puede hacer rollback del micro por fallos de dependencias
        throw new Exception("No se puede validar")
    }
    
}

