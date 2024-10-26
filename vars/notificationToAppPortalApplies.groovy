import com.project.alm.EchoLevel
import com.project.alm.GlobalVars

def call() {

    printOpen("NotificationToAppPortal ${GlobalVars.SEND_TO_AppPortal}", EchoLevel.ALL)

    return GlobalVars.SEND_TO_AppPortal
}

