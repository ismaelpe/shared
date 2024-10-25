import com.cloudbees.groovy.cps.NonCPS
/**
 * Custom sh
 */
@NonCPS
def call(String command) {
    if (env.CONSOLE_SH_ECHO.toBoolean()) {
        return steps.sh(command)
    } else {
        return steps.sh("#!/bin/sh -e\n$command".toString())
    }
}

/**
 * Custom sh
 */
@NonCPS
def call(Map params) {
    if (!env.CONSOLE_SH_ECHO.toBoolean()) {
        def command = params['script']
        params['script'] = "#!/bin/sh -e\n$command".toString()
    }

    return steps.sh(params)
}