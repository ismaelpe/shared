import com.project.alm.*

class CICSHISUtils implements Serializable {

    /*
    Ejemplo de un path de git
    pathToDllFile = "proxy/426-corealmbpi/hisTxDemoAlm/txdemoalm/bin/txdemoalm.DLL"
     */

    static String extensionSeparator = "."
    static String txSeparator = "tx"

    static String getTransactionType(String pathToDllFile) {

        def pathArray = pathToDllFile.split('/')
        String transactionType
        try {
            transactionType = pathArray[0].toLowerCase()
        } catch (Exception e) {
            printOpen("Can't extract TransactionType from path", EchoLevel.ERROR)
            transactionType = ''
        }
        printOpen( "TransactionType: " + transactionType, EchoLevel.DEBUG)
        return transactionType
    }

    static String getTransactionNumber(String pathToDllFile) {
        String transactionNumber
        int dot = pathToDllFile.lastIndexOf(extensionSeparator)
        int sep = pathToDllFile.lastIndexOf(txSeparator)
        transactionNumber = pathToDllFile.substring(sep + 2, dot)
        printOpen( "TransactionNumber: " + transactionNumber, EchoLevel.DEBUG)
        return transactionNumber
    }

    static String getTransactionName(String pathToDllFile) {
        String transactionName
        int dot = pathToDllFile.lastIndexOf(extensionSeparator)
        int sep = pathToDllFile.lastIndexOf(txSeparator)
        transactionName = pathToDllFile.substring(sep, dot)
        printOpen( "TransactionName: " + transactionName, EchoLevel.DEBUG)
        return transactionName
    }

    static String getTargetFolder(String pathToDllFile) {
        String targetFolder
        int lastSlash = pathToDllFile.lastIndexOf("/")
        targetFolder = pathToDllFile.substring(0, lastSlash)
        printOpen( "TargetFolder: " + targetFolder, EchoLevel.DEBUG)
        return targetFolder
    }

    static String getRemoteEnvironment(String transactionType) {
        String remoteEnvironment
        if (transactionType.equalsIgnoreCase("linkapi")) {
            remoteEnvironment = "RE/CICSLinkApi"
        } else if (transactionType.equalsIgnoreCase("link")) {
            remoteEnvironment = "RE/CICSLink"
        } else if (transactionType.equalsIgnoreCase("normal")) {
            remoteEnvironment = "RE/CICSNormal"
        } else if (transactionType.equalsIgnoreCase("proxy")) {
            remoteEnvironment = "RE/CICSProxy"
        }
        printOpen( "RemoteEnvironment: " + remoteEnvironment, EchoLevel.DEBUG)
        return remoteEnvironment
    }

    static String getModuleName(String pathToDllFile) {

        def pathArray = pathToDllFile.split('/')
        def moduleArray
        String moduleName
        try {
            moduleArray = pathArray[1].split('-')
            if (moduleArray.length == 2) {
                moduleName = moduleArray[1]
            }
        } catch (Exception e) {
            printOpen( "Can't extract ModuleName from path", EchoLevel.ERROR)
            moduleName = ''
        }
        printOpen( "ModuleName: " + moduleName, EchoLevel.DEBUG)
        return moduleName
    }

    static String getModuleId(String pathToDllFile) {
        def pathArray = pathToDllFile.split('/')
        def moduleArray
        String moduleId
        try {
            moduleArray = pathArray[1].split('-')
            if (moduleArray.length == 2) {
                moduleId = moduleArray[0]
            }
        } catch (Exception e) {
            printOpen( "Can't extract ModuleId from path", EchoLevel.ERROR)
            moduleId = ''
        }
        printOpen( "ModuleId: " + moduleId, EchoLevel.DEBUG)
        return moduleId
    }

    static Boolean isNullOrEmpty(String someString) {
        Boolean res = false
        if (!someString?.trim()) {
            res = true
        }
        return res
    }
}
