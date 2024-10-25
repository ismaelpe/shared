import com.caixabank.absis3.EchoLevel
import com.caixabank.absis3.GlobalVars

def call() {

    printOpen("NotificationToGpl ${GlobalVars.SEND_TO_GPL}", EchoLevel.ALL)

    return GlobalVars.SEND_TO_GPL
}

