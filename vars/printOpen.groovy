import com.project.alm.EchoLevel
import com.project.alm.GlobalVars
import com.cloudbees.groovy.cps.NonCPS
/**
 * Wrapper Echo for custom logger
 */
@NonCPS
def call(text, logger = null) {
    consoleLogger = EchoLevel.toEchoLevel(GlobalVars.CONSOLE_LOGGER_LEVEL)
     if (consoleLogger == null) {
         // Si se ha definido la variable de entorno global por defecto pintamos todo.
         steps.echo("[DEFAULT] - $text".toString())
         if(env.logsReport) GlobalVars.PIPELINE_LOGS += "[DEFAULT] - $text\n"
     } else {
        // By default los echos sin loggerLevel son INFO
        if (logger == null) {
            logger = EchoLevel.INFO
        }

        // El caso NONE nos vale para poder no pintar logs de forma selecctiva
        // como por ejemplo información sensible
        if (logger == EchoLevel.NONE) {
             steps.echo("[$logger] - Contenido no mostrado por seguridad".toString())
        } else {
            // Para los demas casos vemos compramos la jerarquia de log a pintar
            if ((logger.level() & consoleLogger.level()) == logger.level()) {
                steps.echo("[$logger] - $text".toString())
                if(env.logsReport) GlobalVars.PIPELINE_LOGS += "[$logger] - $text\n"
                if(env.sendLogsToAppPortal && (logger == EchoLevel.INFO || logger == EchoLevel.ERROR)) {
                    String htmlLog = "[${new Date().format("yyyy-MM-dd'T'HH:mm:ss")}][$logger] - $text"
                    GlobalVars.STAGE_LOGS += htmlLog+"\n"
                }
            }
        }
     }
}
