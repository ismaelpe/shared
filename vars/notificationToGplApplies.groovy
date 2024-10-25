import com.project.alm.EchoLevel
import com.project.alm.GlobalVars

def call() {

    printOpen("NotificationToGpl ${GlobalVars.SEND_TO_GPL}", EchoLevel.ALL)

    return GlobalVars.SEND_TO_GPL
}

