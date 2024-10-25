package com.caixabank.absis3

import org.junit.Test

class MavenGoalExecutionFailureDueToRevapiValidationLogParserTest extends GroovyTestCase {

    String logMvnRevapiFail =
        '[INFO] --- spring-boot-maven-plugin:2.1.3.RELEASE:repackage (default) @ demoarqbpi-micro ---\n' +
            '[INFO] \n' +
            '[INFO] --- revapi-maven-plugin:0.10.5:check (default) @ demoarqbpi-micro ---\n' +
            '[INFO] Comparing [com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.contract:demoarqbpi-micro:jar:3.0.0] against [com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.contract:demoarqbpi-micro:jar:3.1.0-SNAPSHOT].\n' +
            '[INFO] The following API problems caused the build to fail:\n' +
            '[INFO] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<java.lang.Void> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::deleteAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[INFO] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<java.lang.Void> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::deleteAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[INFO] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.domain.AccountsDetails> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::getAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[INFO] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.domain.AccountsDetails> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::getAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[INFO] \n' +
            '[INFO] If you\'re using the semver-ignore extension, update your module\'s version to one compatible with the current changes (e.g. mvn package revapi:update-versions). If you want to explicitly ignore this change and provide a justification for it, add the following JSON snippet to your Revapi configuration under "revapi.ignore" path:\n' +
            '\n' +
            '{\n' +
            '  "code": "java.annotation.attributeValueChanged",\n' +
            '  "old": "parameter org.springframework.http.ResponseEntity<java.lang.Void> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::deleteAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException",\n' +
            '  "new": "parameter org.springframework.http.ResponseEntity<java.lang.Void> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::deleteAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException",\n' +
            '  "justification": <<<<< ADD YOUR EXPLANATION FOR THE NECESSITY OF THIS CHANGE >>>>>\n' +
            '  /*\n' +
            '  Additionally, the following attachments can be used to further identify the difference:\n' +
            '\n' +
            '  "annotationType": "org.springframework.web.bind.annotation.PathVariable",\n' +
            '  "annotation": "@org.springframework.web.bind.annotation.PathVariable(\\"account-ids\\")",\n' +
            '  "attribute": "value",\n' +
            '  "oldValue": "\\"account-id\\"",\n' +
            '  "newValue": "\\"account-ids\\"",\n' +
            '  "package": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api",\n' +
            '  "classQualifiedName": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi",\n' +
            '  "classSimpleName": "AccountsApi",\n' +
            '  "methodName": "deleteAccount",\n' +
            '  "parameterIndex": "0",\n' +
            '  "oldArchive": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.contract:demoarqbpi-micro:jar:3.0.0",\n' +
            '  "newArchive": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.contract:demoarqbpi-micro:jar:3.1.0-SNAPSHOT",\n' +
            '  "elementKind": "parameter",\n' +
            '  */\n' +
            '},\n' +
            '{\n' +
            '  "code": "java.annotation.attributeValueChanged",\n' +
            '  "old": "parameter org.springframework.http.ResponseEntity<java.lang.Void> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::deleteAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException",\n' +
            '  "new": "parameter org.springframework.http.ResponseEntity<java.lang.Void> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::deleteAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException",\n' +
            '  "justification": <<<<< ADD YOUR EXPLANATION FOR THE NECESSITY OF THIS CHANGE >>>>>\n' +
            '  /*\n' +
            '  Additionally, the following attachments can be used to further identify the difference:\n' +
            '\n' +
            '  "annotationType": "org.springframework.web.bind.annotation.PathVariable",\n' +
            '  "annotation": "@org.springframework.web.bind.annotation.PathVariable(\\"account-ids\\")",\n' +
            '  "attribute": "value",\n' +
            '  "oldValue": "\\"account-id\\"",\n' +
            '  "newValue": "\\"account-ids\\"",\n' +
            '  "package": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api",\n' +
            '  "classQualifiedName": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi",\n' +
            '  "classSimpleName": "AccountsApi",\n' +
            '  "methodName": "deleteAccount",\n' +
            '  "parameterIndex": "0",\n' +
            '  "oldArchive": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.contract:demoarqbpi-micro:jar:3.0.0",\n' +
            '  "newArchive": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.contract:demoarqbpi-micro:jar:3.1.0-SNAPSHOT",\n' +
            '  "elementKind": "parameter",\n' +
            '  */\n' +
            '},\n' +
            '{\n' +
            '  "code": "java.annotation.attributeValueChanged",\n' +
            '  "old": "parameter org.springframework.http.ResponseEntity<com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.domain.AccountsDetails> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::getAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException",\n' +
            '  "new": "parameter org.springframework.http.ResponseEntity<com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.domain.AccountsDetails> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::getAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException",\n' +
            '  "justification": <<<<< ADD YOUR EXPLANATION FOR THE NECESSITY OF THIS CHANGE >>>>>\n' +
            '  /*\n' +
            '  Additionally, the following attachments can be used to further identify the difference:\n' +
            '\n' +
            '  "annotationType": "org.springframework.web.bind.annotation.PathVariable",\n' +
            '  "annotation": "@org.springframework.web.bind.annotation.PathVariable(\\"account-ids\\")",\n' +
            '  "attribute": "value",\n' +
            '  "oldValue": "\\"account-id\\"",\n' +
            '  "newValue": "\\"account-ids\\"",\n' +
            '  "package": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api",\n' +
            '  "classQualifiedName": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi",\n' +
            '  "classSimpleName": "AccountsApi",\n' +
            '  "methodName": "getAccount",\n' +
            '  "parameterIndex": "0",\n' +
            '  "oldArchive": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.contract:demoarqbpi-micro:jar:3.0.0",\n' +
            '  "newArchive": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.contract:demoarqbpi-micro:jar:3.1.0-SNAPSHOT",\n' +
            '  "elementKind": "parameter",\n' +
            '  */\n' +
            '},\n' +
            '{\n' +
            '  "code": "java.annotation.attributeValueChanged",\n' +
            '  "old": "parameter org.springframework.http.ResponseEntity<com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.domain.AccountsDetails> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::getAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException",\n' +
            '  "new": "parameter org.springframework.http.ResponseEntity<com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.domain.AccountsDetails> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::getAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException",\n' +
            '  "justification": <<<<< ADD YOUR EXPLANATION FOR THE NECESSITY OF THIS CHANGE >>>>>\n' +
            '  /*\n' +
            '  Additionally, the following attachments can be used to further identify the difference:\n' +
            '\n' +
            '  "annotationType": "org.springframework.web.bind.annotation.PathVariable",\n' +
            '  "annotation": "@org.springframework.web.bind.annotation.PathVariable(\\"account-ids\\")",\n' +
            '  "attribute": "value",\n' +
            '  "oldValue": "\\"account-id\\"",\n' +
            '  "newValue": "\\"account-ids\\"",\n' +
            '  "package": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api",\n' +
            '  "classQualifiedName": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi",\n' +
            '  "classSimpleName": "AccountsApi",\n' +
            '  "methodName": "getAccount",\n' +
            '  "parameterIndex": "0",\n' +
            '  "oldArchive": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.contract:demoarqbpi-micro:jar:3.0.0",\n' +
            '  "newArchive": "com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.contract:demoarqbpi-micro:jar:3.1.0-SNAPSHOT",\n' +
            '  "elementKind": "parameter",\n' +
            '  */\n' +
            '},\n' +
            '\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] BUILD FAILURE\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] Total time:  01:40 min\n' +
            '[INFO] Finished at: 2019-10-31T12:05:11Z\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[ERROR] Failed to execute goal org.revapi:revapi-maven-plugin:0.10.5:check (default) on project demoarqbpi-micro: The following API problems caused the build to fail:\n' +
            '[ERROR] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<java.lang.Void> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::deleteAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[ERROR] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<java.lang.Void> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::deleteAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[ERROR] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.domain.AccountsDetails> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::getAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[ERROR] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.domain.AccountsDetails> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::getAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[ERROR] \n' +
            '[ERROR] Consult the plugin output above for suggestions on how to ignore the found problems.\n' +
            '[ERROR] -> [Help 1]\n' +
            '[ERROR] \n' +
            '[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n' +
            '[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n' +
            '[ERROR] \n' +
            '[ERROR] For more information about the errors and possible solutions, please read the following articles:\n' +
            '[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException'

    String expectedRevapiFailCause =
        '[ERROR] Failed to execute goal org.revapi:revapi-maven-plugin:0.10.5:check (default) on project demoarqbpi-micro: The following API problems caused the build to fail:\n' +
            '[ERROR] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<java.lang.Void> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::deleteAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[ERROR] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<java.lang.Void> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::deleteAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[ERROR] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.domain.AccountsDetails> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::getAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[ERROR] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.domain.AccountsDetails> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::getAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)'

    String logMvnBuildSuccess =
        '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] BUILD SUCCESS\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] Total time:  01:40 min\n' +
            '[INFO] Finished at: 2019-10-31T12:05:11Z\n' +
            '[INFO] ------------------------------------------------------------------------\n'

    @Test
    void testGiven_BuildSuccess_returns_NoCauses() {
        def causes = new MavenGoalExecutionFailureDueToRevapiValidationLogParser().parseErrors(logMvnBuildSuccess)
        assert causes.size() == 0, 'We should find no errors'
    }

    @Test
    void testGiven_BuildFailure_returns_OneCause() {
        def causes = new MavenGoalExecutionFailureDueToRevapiValidationLogParser().parseErrors(logMvnRevapiFail)
        println "Cause(s):\n ${causes}"
        assert causes.size() == 1, 'We should find one error'
        assert expectedRevapiFailCause == causes[0], 'We have not got the expected error message'
    }

}
