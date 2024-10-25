package com.caixabank.absis3

import org.junit.Test

class MavenGoalExecutionFailureErrorDecoderTest extends GroovyTestCase {

    String logMvnRevapiValidationFailure =
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

    String logMvnBuildTestsFailure =
        '[INFO] -------------------------------------------------------\n' +
        '[INFO]  T E S T S\n' +
        '[INFO] -------------------------------------------------------\n' +
        '[INFO] Running com.caixabank.absis.arch.monitoring.integration.MonitoringIT\n' +
        '{"@timestamp":"2021-03-22T08:46:40.616+00:00","@version":"1","message":"Neither @ContextConfiguration nor @ContextHierarchy found for test class [com.caixabank.absis.arch.monitoring.integration.MonitoringIT], using DelegatingSmartContextLoader","logger_name":"org.springframework.test.context.support.DefaultTestContextBootstrapper","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:40.636+00:00","@version":"1","message":"Could not detect default resource locations for test class [com.caixabank.absis.arch.monitoring.integration.MonitoringIT]: no resource found for suffixes {-context.xml, Context.groovy}.","logger_name":"org.springframework.test.context.support.AbstractContextLoader","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:40.638+00:00","@version":"1","message":"Could not detect default configuration classes for test class [com.caixabank.absis.arch.monitoring.integration.MonitoringIT]: MonitoringIT does not declare any static, non-private, non-final, nested classes annotated with @Configuration.","logger_name":"org.springframework.test.context.support.AnnotationConfigContextLoaderUtils","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:40.708+00:00","@version":"1","message":"Loaded default TestExecutionListener class names from location [META-INF/spring.factories]: [org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener, org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener, org.springframework.boot.test.autoconfigure.restdocs.RestDocsTestExecutionListener, org.springframework.boot.test.autoconfigure.web.client.MockRestServiceServerResetTestExecutionListener, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrintOnlyOnFailureTestExecutionListener, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverTestExecutionListener, org.springframework.boot.test.autoconfigure.webservices.client.MockWebServiceServerTestExecutionListener, org.springframework.test.context.web.ServletTestExecutionListener, org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener, org.springframework.test.context.support.DependencyInjectionTestExecutionListener, org.springframework.test.context.support.DirtiesContextTestExecutionListener, org.springframework.test.context.transaction.TransactionalTestExecutionListener, org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener, org.springframework.test.context.event.EventPublishingTestExecutionListener]","logger_name":"org.springframework.test.context.support.DefaultTestContextBootstrapper","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:40.741+00:00","@version":"1","message":"Using TestExecutionListeners: [org.springframework.test.context.web.ServletTestExecutionListener@552b7481, org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener@475b796d, org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener@24244ea, org.springframework.test.context.support.DependencyInjectionTestExecutionListener@69c7fb94, org.springframework.test.context.support.DirtiesContextTestExecutionListener@67c61551, org.springframework.test.context.event.EventPublishingTestExecutionListener@7bca98d5, org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener@6bbff652, org.springframework.boot.test.autoconfigure.restdocs.RestDocsTestExecutionListener@3d72abea, org.springframework.boot.test.autoconfigure.web.client.MockRestServiceServerResetTestExecutionListener@6a495d88, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrintOnlyOnFailureTestExecutionListener@45ea6c24, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverTestExecutionListener@51f31fad, org.springframework.boot.test.autoconfigure.webservices.client.MockWebServiceServerTestExecutionListener@7976d382]","logger_name":"org.springframework.test.context.support.DefaultTestContextBootstrapper","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:41.586+00:00","@version":"1","message":"Configuring proxy proxyserv.svb.lacaixa.es8080","logger_name":"com.caixabank.absis.arch.monitoring.integration.MonitoringIT","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:41.938+00:00","@version":"1","message":"Sending MonitoringData{, monitoredMethodTime=38} to https://k8sgateway.dev.icp-1.absis.cloud.lacaixa.es/arch-service/new-monitoring-micro-server-1-dev","logger_name":"com.caixabank.absis.arch.monitoring.integration.MonitoringIT","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:42.894+00:00","@version":"1","message":"Unknown channel option \'SO_TIMEOUT\' for channel \'[id: 0xbaf6f9a4]\'","logger_name":"io.netty.bootstrap.Bootstrap","thread_name":"main","level":"WARN","level_value":30000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:53.184+00:00","@version":"1","message":"[id: 0xbaf6f9a4, L:/172.17.0.2:48764 - R:proxyserv.svb.lacaixa.es/10.119.252.228:8080] The connection observed an error","logger_name":"reactor.netty.http.client.HttpClientConnect","thread_name":"reactor-http-epoll-1","level":"WARN","level_value":30000,"stack_trace":"io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms\\n\\tat io.netty.handler.ssl.SslHandler$5.run(SslHandler.java:2062)\\n\\tSuppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: \\nError has been observed at the following site(s):\\n\\t|_ checkpoint ⇢ Request to POST https://k8sgateway.dev.icp-1.absis.cloud.lacaixa.es/arch-service/new-monitoring-micro-server-1-dev/api/send [DefaultWebClient]\\nStack trace:\\n\\t\\tat io.netty.handler.ssl.SslHandler$5.run(SslHandler.java:2062)\\n\\t\\tat io.netty.util.concurrent.PromiseTask.runTask(PromiseTask.java:98)\\n\\t\\tat io.netty.util.concurrent.ScheduledFutureTask.run(ScheduledFutureTask.java:170)\\n\\t\\tat io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:164)\\n\\t\\tat io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:472)\\n\\t\\tat io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:384)\\n\\t\\tat io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:989)\\n\\t\\tat io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)\\n\\t\\tat io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)\\n\\t\\tat java.base/java.lang.Thread.run(Thread.java:834)\\n","clase":"generic"}\n' +
        '[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 13.41 s <<< FAILURE! - in com.caixabank.absis.arch.monitoring.integration.MonitoringIT\n' +
        '[ERROR] testSendMessageToMonitoring  Time elapsed: 12.145 s  <<< ERROR!\n' +
        'reactor.core.Exceptions$ReactiveException: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms\n' +
        ' at com.caixabank.absis.arch.monitoring.integration.MonitoringIT.testSendMessageToMonitoring(MonitoringIT.java:97)\n' +
        'Caused by: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms\n' +
        '\n' +
        '[INFO] Running com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT\n' +
        '[ERROR] Tests run: 31, Failures: 0, Errors: 1, Skipped: 6, Time elapsed: 302.052 s <<< FAILURE! - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT\n' +
        '[ERROR] testPostCheckjson  Time elapsed: 256.14 s  <<< ERROR!\n' +
        'javax.net.ssl.SSLException: Connection reset\n' +
        ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)\n' +
        'Caused by: java.net.SocketException: Connection reset\n' +
        ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)\n' +
        '\n' +
        '[INFO] \n' +
        '[INFO] Results:\n' +
        '[INFO] \n' +
        '[ERROR] Errors: \n' +
        '[ERROR]   MonitoringIT.testSendMessageToMonitoring:97 » Reactive io.netty.handler.ssl.Ss...\n' +
        '[ERROR]   SeconnectorMicroIT.testPostCheckjson:97 » SSL Connection reset\n' +
        '[INFO] \n' +
        '[ERROR] Tests run: 2, Failures: 0, Errors: 2, Skipped: 0\n' +
        '[INFO] \n' +
        '[INFO] \n' +
        '[INFO] --- maven-failsafe-plugin:2.22.1:verify (integration-test) @ monitoring-micro-server ---\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] Reactor Summary for monitoring-micro 1.13.0-SNAPSHOT:\n' +
        '[INFO] \n' +
        '[INFO] monitoring-api-lib ................................. SUCCESS [ 12.714 s]\n' +
        '[INFO] monitoring-impl-lib ................................ SUCCESS [  4.104 s]\n' +
        '[INFO] monitoring-micro-server ............................ FAILURE [ 28.497 s]\n' +
        '[INFO] monitoring-micro ................................... SKIPPED\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] BUILD FAILURE\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] Total time:  09:33 min\n' +
        '[INFO] Finished at: 2021-04-07T16:28:34Z\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.22.2:test (default-test) on project service-manager-spring-boot-starter: There are test failures.\n' +
        '[ERROR] \n' +
        '[ERROR] Please refer to /home/jenkins/workspace/absis3/services/arch/core/absis3core-lib/absis-arch-plugin-orchestration/service-manager-spring-boot-starter/target/surefire-reports for the individual test results.\n' +
        '[ERROR] Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.\n' +
        '[ERROR] -> [Help 1]'

    String logMvnIntegrationTestsFailure =
        '[INFO] -------------------------------------------------------\n' +
        '[INFO]  T E S T S\n' +
        '[INFO] -------------------------------------------------------\n' +
        '[INFO] Running com.caixabank.absis.arch.monitoring.integration.MonitoringIT\n' +
        '{"@timestamp":"2021-03-22T08:46:40.616+00:00","@version":"1","message":"Neither @ContextConfiguration nor @ContextHierarchy found for test class [com.caixabank.absis.arch.monitoring.integration.MonitoringIT], using DelegatingSmartContextLoader","logger_name":"org.springframework.test.context.support.DefaultTestContextBootstrapper","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:40.636+00:00","@version":"1","message":"Could not detect default resource locations for test class [com.caixabank.absis.arch.monitoring.integration.MonitoringIT]: no resource found for suffixes {-context.xml, Context.groovy}.","logger_name":"org.springframework.test.context.support.AbstractContextLoader","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:40.638+00:00","@version":"1","message":"Could not detect default configuration classes for test class [com.caixabank.absis.arch.monitoring.integration.MonitoringIT]: MonitoringIT does not declare any static, non-private, non-final, nested classes annotated with @Configuration.","logger_name":"org.springframework.test.context.support.AnnotationConfigContextLoaderUtils","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:40.708+00:00","@version":"1","message":"Loaded default TestExecutionListener class names from location [META-INF/spring.factories]: [org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener, org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener, org.springframework.boot.test.autoconfigure.restdocs.RestDocsTestExecutionListener, org.springframework.boot.test.autoconfigure.web.client.MockRestServiceServerResetTestExecutionListener, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrintOnlyOnFailureTestExecutionListener, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverTestExecutionListener, org.springframework.boot.test.autoconfigure.webservices.client.MockWebServiceServerTestExecutionListener, org.springframework.test.context.web.ServletTestExecutionListener, org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener, org.springframework.test.context.support.DependencyInjectionTestExecutionListener, org.springframework.test.context.support.DirtiesContextTestExecutionListener, org.springframework.test.context.transaction.TransactionalTestExecutionListener, org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener, org.springframework.test.context.event.EventPublishingTestExecutionListener]","logger_name":"org.springframework.test.context.support.DefaultTestContextBootstrapper","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:40.741+00:00","@version":"1","message":"Using TestExecutionListeners: [org.springframework.test.context.web.ServletTestExecutionListener@552b7481, org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener@475b796d, org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener@24244ea, org.springframework.test.context.support.DependencyInjectionTestExecutionListener@69c7fb94, org.springframework.test.context.support.DirtiesContextTestExecutionListener@67c61551, org.springframework.test.context.event.EventPublishingTestExecutionListener@7bca98d5, org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener@6bbff652, org.springframework.boot.test.autoconfigure.restdocs.RestDocsTestExecutionListener@3d72abea, org.springframework.boot.test.autoconfigure.web.client.MockRestServiceServerResetTestExecutionListener@6a495d88, org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrintOnlyOnFailureTestExecutionListener@45ea6c24, org.springframework.boot.test.autoconfigure.web.servlet.WebDriverTestExecutionListener@51f31fad, org.springframework.boot.test.autoconfigure.webservices.client.MockWebServiceServerTestExecutionListener@7976d382]","logger_name":"org.springframework.test.context.support.DefaultTestContextBootstrapper","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:41.586+00:00","@version":"1","message":"Configuring proxy proxyserv.svb.lacaixa.es8080","logger_name":"com.caixabank.absis.arch.monitoring.integration.MonitoringIT","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:41.938+00:00","@version":"1","message":"Sending MonitoringData{, monitoredMethodTime=38} to https://k8sgateway.dev.icp-1.absis.cloud.lacaixa.es/arch-service/new-monitoring-micro-server-1-dev","logger_name":"com.caixabank.absis.arch.monitoring.integration.MonitoringIT","thread_name":"main","level":"INFO","level_value":20000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:42.894+00:00","@version":"1","message":"Unknown channel option \'SO_TIMEOUT\' for channel \'[id: 0xbaf6f9a4]\'","logger_name":"io.netty.bootstrap.Bootstrap","thread_name":"main","level":"WARN","level_value":30000,"clase":"generic"}\n' +
        '{"@timestamp":"2021-03-22T08:46:53.184+00:00","@version":"1","message":"[id: 0xbaf6f9a4, L:/172.17.0.2:48764 - R:proxyserv.svb.lacaixa.es/10.119.252.228:8080] The connection observed an error","logger_name":"reactor.netty.http.client.HttpClientConnect","thread_name":"reactor-http-epoll-1","level":"WARN","level_value":30000,"stack_trace":"io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms\\n\\tat io.netty.handler.ssl.SslHandler$5.run(SslHandler.java:2062)\\n\\tSuppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: \\nError has been observed at the following site(s):\\n\\t|_ checkpoint ⇢ Request to POST https://k8sgateway.dev.icp-1.absis.cloud.lacaixa.es/arch-service/new-monitoring-micro-server-1-dev/api/send [DefaultWebClient]\\nStack trace:\\n\\t\\tat io.netty.handler.ssl.SslHandler$5.run(SslHandler.java:2062)\\n\\t\\tat io.netty.util.concurrent.PromiseTask.runTask(PromiseTask.java:98)\\n\\t\\tat io.netty.util.concurrent.ScheduledFutureTask.run(ScheduledFutureTask.java:170)\\n\\t\\tat io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:164)\\n\\t\\tat io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:472)\\n\\t\\tat io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:384)\\n\\t\\tat io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:989)\\n\\t\\tat io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)\\n\\t\\tat io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)\\n\\t\\tat java.base/java.lang.Thread.run(Thread.java:834)\\n","clase":"generic"}\n' +
        '[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 13.41 s <<< FAILURE! - in com.caixabank.absis.arch.monitoring.integration.MonitoringIT\n' +
        '[ERROR] testSendMessageToMonitoring  Time elapsed: 12.145 s  <<< ERROR!\n' +
        'reactor.core.Exceptions$ReactiveException: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms\n' +
        ' at com.caixabank.absis.arch.monitoring.integration.MonitoringIT.testSendMessageToMonitoring(MonitoringIT.java:97)\n' +
        'Caused by: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms\n' +
        '\n' +
        '[INFO] Running com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT\n' +
        '[ERROR] Tests run: 31, Failures: 0, Errors: 1, Skipped: 6, Time elapsed: 302.052 s <<< FAILURE! - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT\n' +
        '[ERROR] testPostCheckjson  Time elapsed: 256.14 s  <<< ERROR!\n' +
        'javax.net.ssl.SSLException: Connection reset\n' +
        ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)\n' +
        'Caused by: java.net.SocketException: Connection reset\n' +
        ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)\n' +
        '\n' +
        '[INFO] \n' +
        '[INFO] Results:\n' +
        '[INFO] \n' +
        '[ERROR] Errors: \n' +
        '[ERROR]   MonitoringIT.testSendMessageToMonitoring:97 » Reactive io.netty.handler.ssl.Ss...\n' +
        '[ERROR]   SeconnectorMicroIT.testPostCheckjson:97 » SSL Connection reset\n' +
        '[INFO] \n' +
        '[ERROR] Tests run: 2, Failures: 0, Errors: 2, Skipped: 0\n' +
        '[INFO] \n' +
        '[INFO] \n' +
        '[INFO] --- maven-failsafe-plugin:2.22.1:verify (integration-test) @ monitoring-micro-server ---\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] Reactor Summary for monitoring-micro 1.13.0-SNAPSHOT:\n' +
        '[INFO] \n' +
        '[INFO] monitoring-api-lib ................................. SUCCESS [ 12.714 s]\n' +
        '[INFO] monitoring-impl-lib ................................ SUCCESS [  4.104 s]\n' +
        '[INFO] monitoring-micro-server ............................ FAILURE [ 28.497 s]\n' +
        '[INFO] monitoring-micro ................................... SKIPPED\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] BUILD FAILURE\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] Total time:  53.286 s\n' +
        '[INFO] Finished at: 2021-03-22T08:46:53Z\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[ERROR] Failed to execute goal org.apache.maven.plugins:maven-failsafe-plugin:2.22.1:verify (integration-test) on project monitoring-micro-server: There are test failures.\n' +
        '[ERROR] \n' +
        '[ERROR] Please refer to /home/jenkins/workspace/absis3/services/arch/monitoring/monitoring-micro/monitoring-micro-server/target/failsafe-reports for the individual test results.' +
        '[ERROR] Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.\n' +
        '[ERROR] -> [Help 1]'

    String logMvnOpenApiGenerationNexusDownloadFailure =
            '[INFO] Scanning for projects...\n' +
            '[INFO] \n' +
            '[INFO] --< com.caixabank.absis.apps.dataservice.demo.contract:arqpru-micro >---\n' +
            '[INFO] Building arqpru-micro 2.8.2\n' +
            '[INFO] --------------------------------[ jar ]---------------------------------\n' +
            '[WARNING] The POM for com.sun.xml.bind:jaxb-osgi:jar:2.2.10 is invalid, transitive dependencies (if any) will not be available, enable debug logging for more details\n' +
            '[INFO] \n' +
            '[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ arqpru-micro ---\n' +
            '[INFO] \n' +
            '[INFO] --- jacoco-maven-plugin:0.8.6:prepare-agent (default) @ arqpru-micro ---\n' +
            '[INFO] argLine set to -javaagent:/root/.m2/repository/org/jacoco/org.jacoco.agent/0.8.6/org.jacoco.agent-0.8.6-runtime.jar=destfile=/home/jenkins/workspace/absis3/services/arch/alm/dev2/job-create-Release@tmp/target/jacoco.exec,append=true\n' +
            '[INFO] \n' +
            '[INFO] --- codegen-postprocess-plugin:1.16.1:delete-model (delete-model) @ arqpru-micro ---\n' +
            '[INFO] Source directory is:/home/jenkins/workspace/absis3/services/arch/alm/dev2/job-create-Release@tmp/src/main/java\n' +
            '[INFO] \twith apiPackage:com.caixabank.absis.apps.dataservice.demo.arqpru.api.domain\n' +
            '[INFO] \tsearch model api on:/home/jenkins/workspace/absis3/services/arch/alm/dev2/job-create-Release@tmp/src/main/java/com/caixabank/absis/apps/dataservice/demo/arqpru/api/domain\n' +
            '[INFO] \tsearch interface api on:/home/jenkins/workspace/absis3/services/arch/alm/dev2/job-create-Release@tmp/src/main/java/com/caixabank/absis/apps/dataservice/demo/arqpru/api\n' +
            '[ERROR] java.nio.file.NoSuchFileException: /home/jenkins/workspace/absis3/services/arch/alm/dev2/job-create-Release@tmp/src/main/java/com/caixabank/absis/apps/dataservice/demo/arqpru/api/domain\n' +
            '[ERROR] java.nio.file.NoSuchFileException: /home/jenkins/workspace/absis3/services/arch/alm/dev2/job-create-Release@tmp/src/main/java/com/caixabank/absis/apps/dataservice/demo/arqpru/api\n' +
            '[INFO] \n' +
            '[INFO] --- openapi-generator-maven-plugin:4.3.1:generate (default) @ arqpru-micro ---\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] BUILD FAILURE\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] Total time:  07:22 min\n' +
            '[INFO] Finished at: 2021-04-27T09:35:24Z\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[ERROR] Failed to execute goal org.openapitools:openapi-generator-maven-plugin:4.3.1:generate (default) on project arqpru-micro: Execution default of goal org.openapitools:openapi-generator-maven-plugin:4.3.1:generate failed: Plugin org.openapitools:openapi-generator-maven-plugin:4.3.1 or one of its dependencies could not be resolved: Failed to collect dependencies at org.openapitools:openapi-generator-maven-plugin:jar:4.3.1 -> com.caixabank.absis.arch.mavenplugins:absis-openapi-generator:jar:1.16.1 -> org.openapitools:openapi-generator:jar:4.3.1 -> io.swagger.parser.v3:swagger-parser:jar:2.0.19 -> io.swagger.parser.v3:swagger-parser-v2-converter:jar:2.0.19 -> io.swagger:swagger-compat-spec-parser:jar:1.0.50 -> com.github.java-json-tools:json-schema-validator:jar:2.2.8 -> com.github.java-json-tools:json-schema-core:jar:1.2.8 -> com.github.fge:uri-template:jar:0.9: Failed to read artifact descriptor for com.github.fge:uri-template:jar:0.9: Could not transfer artifact com.github.fge:uri-template:pom:0.9 from/to nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/): GET request of: com/github/fge/uri-template/0.9/uri-template-0.9.pom from nexus-pro-public-group failed: Premature end of Content-Length delimited message body (expected: 2,768; received: 1,028) -> [Help 1]\n' +
            '[ERROR] \n' +
            '[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n' +
            '[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n' +
            '[ERROR] \n' +
            '[ERROR] For more information about the errors and possible solutions, please read the following articles:\n' +
            '[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/PluginResolutionException'

    String logMvnRunRemoteITPRONexusDownloadFailure =
            '[INFO] Scanning for projects...\n' +
            '[ERROR] [ERROR] Some problems were encountered while processing the POMs:\n' +
            '[ERROR] Unresolveable build extension: Plugin org.apache.maven.plugins:maven-javadoc-plugin:3.1.1 or one of its dependencies could not be resolved: Failed to collect dependencies at org.apache.maven.plugins:maven-javadoc-plugin:jar:3.1.1 -> org.apache.maven.doxia:doxia-site-renderer:jar:1.7.4 -> org.codehaus.plexus:plexus-velocity:jar:1.2 @ \n' +
            ' @ \n' +
            '[ERROR] The build could not read 1 project -> [Help 1]\n' +
            '[ERROR]   \n' +
            '[ERROR]   The project com.caixabank.absis.apps.service.cbk.demo:demoarqalm-micro:1.231.0 (/home/jenkins/workspace/absis3/services/arch/alm/stage/job-deploy-to-PRO/pom.xml) has 1 error\n' +
            '[ERROR]     Unresolveable build extension: Plugin org.apache.maven.plugins:maven-javadoc-plugin:3.1.1 or one of its dependencies could not be resolved: Failed to collect dependencies at org.apache.maven.plugins:maven-javadoc-plugin:jar:3.1.1 -> org.apache.maven.doxia:doxia-site-renderer:jar:1.7.4 -> org.codehaus.plexus:plexus-velocity:jar:1.2: Failed to read artifact descriptor for org.codehaus.plexus:plexus-velocity:jar:1.2: Could not transfer artifact org.codehaus.plexus:plexus-velocity:pom:1.2 from/to nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/): GET request of: org/codehaus/plexus/plexus-velocity/1.2/plexus-velocity-1.2.pom from nexus-pro-public-group failed: Premature end of Content-Length delimited message body (expected: 2,821; received: 1,028) -> [Help 2]\n' +
            '[ERROR] \n' +
            '[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n' +
            '[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n' +
            '[ERROR] \n' +
            '[ERROR] For more information about the errors and possible solutions, please read the following articles:\n' +
            '[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/ProjectBuildingException\n' +
            '[ERROR] [Help 2] http://cwiki.apache.org/confluence/display/MAVEN/PluginManagerException'

    String logMvnVersionsSetNexusPluginDownloadsFailure =
            '[INFO] Scanning for projects...\n' +
            '[WARNING] The POM for org.jacoco:jacoco-maven-plugin:jar:0.8.6 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.jacoco:jacoco-maven-plugin:0.8.6: Plugin org.jacoco:jacoco-maven-plugin:0.8.6 or one of its dependencies could not be resolved: Could not find artifact org.jacoco:jacoco-maven-plugin:jar:0.8.6 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-compiler-plugin:jar:3.8.1 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-compiler-plugin:3.8.1: Plugin org.apache.maven.plugins:maven-compiler-plugin:3.8.1 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-compiler-plugin:jar:3.8.1 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-resources-plugin:jar:3.1.0 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-resources-plugin:3.1.0: Plugin org.apache.maven.plugins:maven-resources-plugin:3.1.0 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-resources-plugin:jar:3.1.0 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-javadoc-plugin:jar:3.1.1 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-javadoc-plugin:3.1.1: Plugin org.apache.maven.plugins:maven-javadoc-plugin:3.1.1 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-javadoc-plugin:jar:3.1.1 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-source-plugin:jar:2.1.1 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-source-plugin:2.1.1: Plugin org.apache.maven.plugins:maven-source-plugin:2.1.1 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-source-plugin:jar:2.1.1 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] Failed to retrieve plugin descriptor for com.caixabank.absis.arch.mavenplugins:absis-dependencies-plugin:1.16.1: Plugin com.caixabank.absis.arch.mavenplugins:absis-dependencies-plugin:1.16.1 or one of its dependencies could not be resolved: Failed to read artifact descriptor for com.caixabank.absis.arch.mavenplugins:absis-dependencies-plugin:jar:1.16.1\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-clean-plugin:jar:2.5 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-clean-plugin:2.5: Plugin org.apache.maven.plugins:maven-clean-plugin:2.5 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-clean-plugin:jar:2.5 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-jar-plugin:jar:3.1.2 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-jar-plugin:3.1.2: Plugin org.apache.maven.plugins:maven-jar-plugin:3.1.2 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-jar-plugin:jar:3.1.2 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-surefire-plugin:jar:2.22.2 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-surefire-plugin:2.22.2: Plugin org.apache.maven.plugins:maven-surefire-plugin:2.22.2 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-surefire-plugin:jar:2.22.2 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-install-plugin:jar:2.4 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-install-plugin:2.4: Plugin org.apache.maven.plugins:maven-install-plugin:2.4 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-install-plugin:jar:2.4 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-deploy-plugin:jar:2.7 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-deploy-plugin:2.7: Plugin org.apache.maven.plugins:maven-deploy-plugin:2.7 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-deploy-plugin:jar:2.7 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-site-plugin:jar:3.3 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-site-plugin:3.3: Plugin org.apache.maven.plugins:maven-site-plugin:3.3 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-site-plugin:jar:3.3 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-antrun-plugin:jar:1.3 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-antrun-plugin:1.3: Plugin org.apache.maven.plugins:maven-antrun-plugin:1.3 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-antrun-plugin:jar:1.3 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.sonarsource.scanner.maven:sonar-maven-plugin:jar:3.6.0.1398 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398: Plugin org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398 or one of its dependencies could not be resolved: Could not find artifact org.sonarsource.scanner.maven:sonar-maven-plugin:jar:3.6.0.1398 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.springframework.boot:spring-boot-maven-plugin:2.1.3.RELEASE: Plugin org.springframework.boot:spring-boot-maven-plugin:2.1.3.RELEASE or one of its dependencies could not be resolved: Failed to read artifact descriptor for org.springframework.boot:spring-boot-maven-plugin:jar:2.1.3.RELEASE\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-surefire-plugin:jar:2.22.2 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-surefire-plugin:2.22.2: Plugin org.apache.maven.plugins:maven-surefire-plugin:2.22.2 or one of its dependencies could not be resolved: Failure to find org.apache.maven.plugins:maven-surefire-plugin:jar:2.22.2 in http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/ was cached in the local repository, resolution will not be reattempted until the update interval of nexus-pro-public-group has elapsed or updates are forced\n' +
            '[WARNING] The POM for org.openapitools:openapi-generator-maven-plugin:jar:4.3.1 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.openapitools:openapi-generator-maven-plugin:4.3.1: Plugin org.openapitools:openapi-generator-maven-plugin:4.3.1 or one of its dependencies could not be resolved: Could not find artifact org.openapitools:openapi-generator-maven-plugin:jar:4.3.1 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] Failed to retrieve plugin descriptor for com.caixabank.absis.arch.mavenplugins:codegen-postprocess-plugin:1.16.1: Plugin com.caixabank.absis.arch.mavenplugins:codegen-postprocess-plugin:1.16.1 or one of its dependencies could not be resolved: Failed to read artifact descriptor for com.caixabank.absis.arch.mavenplugins:codegen-postprocess-plugin:jar:1.16.1\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-failsafe-plugin:jar:2.22.1 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-failsafe-plugin:2.22.1: Plugin org.apache.maven.plugins:maven-failsafe-plugin:2.22.1 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-failsafe-plugin:jar:2.22.1 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.codehaus.mojo:build-helper-maven-plugin:jar:3.0.0 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.codehaus.mojo:build-helper-maven-plugin:3.0.0: Plugin org.codehaus.mojo:build-helper-maven-plugin:3.0.0 or one of its dependencies could not be resolved: Could not find artifact org.codehaus.mojo:build-helper-maven-plugin:jar:3.0.0 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.jacoco:jacoco-maven-plugin:jar:0.8.6 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.jacoco:jacoco-maven-plugin:0.8.6: Plugin org.jacoco:jacoco-maven-plugin:0.8.6 or one of its dependencies could not be resolved: Failure to find org.jacoco:jacoco-maven-plugin:jar:0.8.6 in http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/ was cached in the local repository, resolution will not be reattempted until the update interval of nexus-pro-public-group has elapsed or updates are forced\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-jar-plugin:jar:3.1.2 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-jar-plugin:3.1.2: Plugin org.apache.maven.plugins:maven-jar-plugin:3.1.2 or one of its dependencies could not be resolved: Failure to find org.apache.maven.plugins:maven-jar-plugin:jar:3.1.2 in http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/ was cached in the local repository, resolution will not be reattempted until the update interval of nexus-pro-public-group has elapsed or updates are forced\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-assembly-plugin:jar:3.1.1 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-assembly-plugin:3.1.1: Plugin org.apache.maven.plugins:maven-assembly-plugin:3.1.1 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-assembly-plugin:jar:3.1.1 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-dependency-plugin:jar:2.8 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-dependency-plugin:2.8: Plugin org.apache.maven.plugins:maven-dependency-plugin:2.8 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-dependency-plugin:jar:2.8 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.apache.maven.plugins:maven-release-plugin:jar:2.5.3 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.maven.plugins:maven-release-plugin:2.5.3: Plugin org.apache.maven.plugins:maven-release-plugin:2.5.3 or one of its dependencies could not be resolved: Could not find artifact org.apache.maven.plugins:maven-release-plugin:jar:2.5.3 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.apache.avro:avro-maven-plugin:jar:1.9.1 is missing, no dependency information available\n' +
            '[WARNING] Failed to retrieve plugin descriptor for org.apache.avro:avro-maven-plugin:1.9.1: Plugin org.apache.avro:avro-maven-plugin:1.9.1 or one of its dependencies could not be resolved: Could not find artifact org.apache.avro:avro-maven-plugin:jar:1.9.1 in nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:2.8.1 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:2.7 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:2.6 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:2.5 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:2.4 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:2.3 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:2.2 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:2.1 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:2.0 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:1.3.1 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:1.3 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:1.2 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:1.1 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:1.0 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:1.0-alpha-3 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:1.0-alpha-2 is missing, no dependency information available\n' +
            '[WARNING] The POM for org.codehaus.mojo:versions-maven-plugin:jar:1.0-alpha-1 is missing, no dependency information available\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] BUILD FAILURE\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] Total time:  01:36 min\n' +
            '[INFO] Finished at: 2021-04-14T05:25:37Z\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[ERROR] Error resolving version for plugin \'org.codehaus.mojo:versions-maven-plugin\' from the repositories [local (/root/.m2/repository), nexus-pro-public-group (http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/)]: Plugin not found in any plugin repository -> [Help 1]\n' +
            '[ERROR] \n' +
            '[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n' +
            '[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n' +
            '[ERROR] \n' +
            '[ERROR] For more information about the errors and possible solutions, please read the following articles:\n' +
            '[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/PluginVersionResolutionException'

    String logMvnSonarFailureDueToConnectionTimeout =
            '[INFO] Scanning for projects...\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] Reactor Build Order:\n' +
            '[INFO] \n' +
            '[INFO] tauxconnector-lib-starter                                          [jar]\n' +
            '[INFO] tauxconnector-lib-sample-app                                       [jar]\n' +
            '[INFO] tauxconnector-lib                                                  [pom]\n' +
            '[INFO] \n' +
            '[INFO] --< com.caixabank.absis.arch.backend.connectors.taux:tauxconnector-lib >--\n' +
            '[INFO] Building tauxconnector-lib 1.6.0-RC1                               [1/3]\n' +
            '[INFO] --------------------------------[ pom ]---------------------------------\n' +
            '[WARNING] The POM for com.sun.xml.bind:jaxb-osgi:jar:2.2.10 is invalid, transitive dependencies (if any) will not be available, enable debug logging for more details\n' +
            '[INFO] \n' +
            '[INFO] --- sonar-maven-plugin:3.6.0.1398:sonar (default-cli) @ tauxconnector-lib ---\n' +
            '[INFO] User cache: /root/.sonar/cache\n' +
            '[INFO] SonarQube version: 7.9.2\n' +
            '[INFO] Default locale: "en", source code encoding: "UTF-8" (analysis is platform dependent)\n' +
            '[INFO] Load global settings\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] Reactor Summary for tauxconnector-lib 1.6.0-RC1:\n' +
            '[INFO] \n' +
            '[INFO] tauxconnector-lib-starter .......................... SKIPPED\n' +
            '[INFO] tauxconnector-lib-sample-app ....................... SKIPPED\n' +
            '[INFO] tauxconnector-lib .................................. FAILURE [01:14 min]\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] BUILD FAILURE\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] Total time:  01:16 min\n' +
            '[INFO] Finished at: 2021-05-18T09:39:27Z\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[ERROR] Failed to execute goal org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar (default-cli) on project tauxconnector-lib: Unable to load component class org.sonar.scanner.bootstrap.ScannerPluginInstaller: Unable to load component class org.sonar.scanner.bootstrap.PluginFiles: Unable to load component class org.sonar.scanner.bootstrap.GlobalConfiguration: Unable to load component class org.sonar.scanner.bootstrap.GlobalServerSettings: Fail to request https://sonar-a3mson-sonars.pro.ap.intranet.cloud.lacaixa.es/api/settings/values.protobuf: timeout -> [Help 1]\n' +
            '[ERROR] \n' +
            '[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n' +
            '[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n' +
            '[ERROR] \n' +
            '[ERROR] For more information about the errors and possible solutions, please read the following articles:\n' +
            '[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException\n' +
            '[ERROR] \n' +
            '[ERROR] After correcting the problems, you can resume the build with the command\n' +
            '[ERROR]   mvn <goals> -rf :tauxconnector-lib'

    String logMvnSonarFailureDueToConnectionResetAgainstNexus =
            '[INFO] Scanning for projects...\n' +
            '[INFO] \n' +
            '[INFO] -------< com.caixabank.absis.apps.dataservice.demo:arqpru-micro >-------\n' +
            '[INFO] Building arqpru-micro 2.5.0-RC1\n' +
            '[INFO] --------------------------------[ jar ]---------------------------------\n' +
            '[WARNING] The POM for com.sun.xml.bind:jaxb-osgi:jar:2.2.10 is invalid, transitive dependencies (if any) will not be available, enable debug logging for more details\n' +
            '[INFO] \n' +
            '[INFO] --- sonar-maven-plugin:3.6.0.1398:sonar (default-cli) @ arqpru-micro ---\n' +
            '[INFO] User cache: /root/.sonar/cache\n' +
            '[INFO] SonarQube version: 7.9.2\n' +
            '[INFO] Default locale: "en", source code encoding: "UTF-8"\n' +
            '[INFO] Load global settings\n' +
            '[INFO] Load global settings (done) | time=713ms\n' +
            '[INFO] Server id: 36B88DE2-AXgmhWF5HJmtkI_Hpas_\n' +
            '[INFO] User cache: /root/.sonar/cache\n' +
            '[INFO] Load/download plugins\n' +
            '[INFO] Load plugins index\n' +
            '[INFO] Load plugins index (done) | time=164ms\n' +
            '[INFO] Load/download plugins (done) | time=7467ms\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] BUILD FAILURE\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] Total time:  24.797 s\n' +
            '[INFO] Finished at: 2021-04-23T11:08:12Z\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[ERROR] Failed to execute goal org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar (default-cli) on project arqpru-micro: Fail to download plugin [python] into /root/.sonar/_tmp/fileCache5741722121292955825.tmp: Connection reset -> [Help 1]\n' +
            '[ERROR] \n' +
            '[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n' +
            '[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n' +
            '[ERROR] \n' +
            '[ERROR] For more information about the errors and possible solutions, please read the following articles:\n' +
            '[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException'

    String logMvnSonarFailureDueToConnectionStreamResetAgainstNexus =
            '[INFO] Scanning for projects...\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] Reactor Build Order:\n' +
            '[INFO] \n' +
            '[INFO] tauxconnector-lib-starter                                          [jar]\n' +
            '[INFO] tauxconnector-lib-sample-app                                       [jar]\n' +
            '[INFO] tauxconnector-lib                                                  [pom]\n' +
            '[INFO] \n' +
            '[INFO] --< com.caixabank.absis.arch.backend.connectors.taux:tauxconnector-lib >--\n' +
            '[INFO] Building tauxconnector-lib 1.6.0-RC2                               [1/3]\n' +
            '[INFO] --------------------------------[ pom ]---------------------------------\n' +
            '[WARNING] The POM for com.sun.xml.bind:jaxb-osgi:jar:2.2.10 is invalid, transitive dependencies (if any) will not be available, enable debug logging for more details\n' +
            '[INFO] \n' +
            '[INFO] --- sonar-maven-plugin:3.6.0.1398:sonar (default-cli) @ tauxconnector-lib ---\n' +
            '[INFO] User cache: /root/.sonar/cache\n' +
            '[INFO] SonarQube version: 7.9.2\n' +
            '[INFO] Default locale: "en", source code encoding: "UTF-8" (analysis is platform dependent)\n' +
            '[INFO] Load global settings\n' +
            '[INFO] Load global settings (done) | time=625ms\n' +
            '[INFO] Server id: 36B88DE2-AXgmhWF5HJmtkI_Hpas_\n' +
            '[INFO] User cache: /root/.sonar/cache\n' +
            '[INFO] Load/download plugins\n' +
            '[INFO] Load plugins index\n' +
            '[INFO] Load plugins index (done) | time=106ms\n' +
            '[INFO] Load/download plugins (done) | time=11971ms\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] Reactor Summary for tauxconnector-lib 1.6.0-RC2:\n' +
            '[INFO] \n' +
            '[INFO] tauxconnector-lib-starter .......................... SKIPPED\n' +
            '[INFO] tauxconnector-lib-sample-app ....................... SKIPPED\n' +
            '[INFO] tauxconnector-lib .................................. FAILURE [ 31.663 s]\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] BUILD FAILURE\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] Total time:  34.208 s\n' +
            '[INFO] Finished at: 2021-05-18T11:09:36Z\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[ERROR] Failed to execute goal org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar (default-cli) on project tauxconnector-lib: Fail to download plugin [ruby] into /root/.sonar/_tmp/fileCache1286250266747310805.tmp: stream was reset: CANCEL -> [Help 1]\n' +
            '[ERROR] \n' +
            '[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n' +
            '[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n' +
            '[ERROR] \n' +
            '[ERROR] For more information about the errors and possible solutions, please read the following articles:\n' +
            '[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException\n' +
            '[ERROR] \n' +
            '[ERROR] After correcting the problems, you can resume the build with the command\n' +
            '[ERROR]   mvn <goals> -rf :tauxconnector-lib'

    String logMvnBuildSuccess =
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] BUILD SUCCESS\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] Total time:  01:40 min\n' +
        '[INFO] Finished at: 2019-10-31T12:05:11Z\n' +
        '[INFO] ------------------------------------------------------------------------\n'

    @Test
    void testGiven_BuildSuccess_returns_UnknownError() {
        MavenGoalExecutionFailureError error = MavenGoalExecutionFailureErrorDecoder.getErrorFromLog(logMvnBuildSuccess)
        assert error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.UNKNOWN, 'We are not expecting any error'
        assert error.errors.size() == 0, 'We should find no errors'
    }

    @Test
    void testGiven_RevapiValidation_Failure_returns_RevapiValidationError() {
        MavenGoalExecutionFailureError error = MavenGoalExecutionFailureErrorDecoder.getErrorFromLog(logMvnRevapiValidationFailure)
        assert error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.REVAPI_VALIDATION, 'We are expecting a revapi validation error'
        assert error.errors.size() == 1, 'We should find one error'
        assert '[ERROR] Failed to execute goal org.revapi:revapi-maven-plugin:0.10.5:check (default) on project demoarqbpi-micro: The following API problems caused the build to fail:\n' +
            '[ERROR] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<java.lang.Void> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::deleteAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[ERROR] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<java.lang.Void> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::deleteAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[ERROR] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.domain.AccountsDetails> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::getAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)\n' +
            '[ERROR] java.annotation.attributeValueChanged: parameter org.springframework.http.ResponseEntity<com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.domain.AccountsDetails> com.caixabank.absis.apps.service.bpi.componentesplataformas.coreabsisbpi.demoarqbpi.api.AccountsApi::getAccount(===java.lang.String===) throws com.caixabank.absis.arch.common.exception.AbsisException: Attribute \'value\' of annotation \'org.springframework.web.bind.annotation.PathVariable\' changed value from \'"account-id"\' to \'"account-ids"\'. (breaks semantic versioning)' == error.errors[0], 'We have not got the expected error message'
    }

    @Test
    void testGiven_BuildTest_TwoDifferentSSLErrors_Failure_returns_BuildTestError() {
        MavenGoalExecutionFailureError error = MavenGoalExecutionFailureErrorDecoder.getErrorFromLog(logMvnBuildTestsFailure)
        assert error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.BUILD_TEST, 'We are expecting a build test error'
        assert error.errors.size() == 2, 'We should find two errors'
        assert '[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 13.41 s <<< FAILURE! - in com.caixabank.absis.arch.monitoring.integration.MonitoringIT\n' +
            '[ERROR] testSendMessageToMonitoring  Time elapsed: 12.145 s  <<< ERROR!\n' +
            'reactor.core.Exceptions$ReactiveException: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms\n' +
            ' at com.caixabank.absis.arch.monitoring.integration.MonitoringIT.testSendMessageToMonitoring(MonitoringIT.java:97)\n' +
            'Caused by: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms' == error.errors[0], 'We have not got the expected error message'
        assert '[ERROR] Tests run: 31, Failures: 0, Errors: 1, Skipped: 6, Time elapsed: 302.052 s <<< FAILURE! - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT\n' +
            '[ERROR] testPostCheckjson  Time elapsed: 256.14 s  <<< ERROR!\n' +
            'javax.net.ssl.SSLException: Connection reset\n' +
            ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)\n' +
            'Caused by: java.net.SocketException: Connection reset\n' +
            ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)' == error.errors[1], 'We have not got the expected error message'
    }

    @Test
    void testGiven_ITTest_TwoDifferentSSLErrors_Failure_returns_IntegrationTestError() {
        MavenGoalExecutionFailureError error = MavenGoalExecutionFailureErrorDecoder.getErrorFromLog(logMvnIntegrationTestsFailure)
        assert error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.INTEGRATION_TEST, 'We are expecting an integration test error'
        assert error.errors.size() == 2, 'We should find two errors'
        assert '[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 13.41 s <<< FAILURE! - in com.caixabank.absis.arch.monitoring.integration.MonitoringIT\n' +
            '[ERROR] testSendMessageToMonitoring  Time elapsed: 12.145 s  <<< ERROR!\n' +
            'reactor.core.Exceptions$ReactiveException: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms\n' +
            ' at com.caixabank.absis.arch.monitoring.integration.MonitoringIT.testSendMessageToMonitoring(MonitoringIT.java:97)\n' +
            'Caused by: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms' == error.errors[0], 'We have not got the expected error message'
        assert '[ERROR] Tests run: 31, Failures: 0, Errors: 1, Skipped: 6, Time elapsed: 302.052 s <<< FAILURE! - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT\n' +
            '[ERROR] testPostCheckjson  Time elapsed: 256.14 s  <<< ERROR!\n' +
            'javax.net.ssl.SSLException: Connection reset\n' +
            ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)\n' +
            'Caused by: java.net.SocketException: Connection reset\n' +
            ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)' == error.errors[1], 'We have not got the expected error message'
    }

    @Test
    void testGiven_Build_TwoDifferentSSLErrorsWhenGeneratingOpenApi_Failure_returns_OpenApiGenerationError() {
        MavenGoalExecutionFailureError error =
            MavenGoalExecutionFailureErrorDecoder.getErrorFromLog(
                MavenGoalExecutionFailureDueToOpenApiGenerationLogParserTest.logMvnOpenApiGenerationSSLExceptionWhenContactingContractServer
            )
        assert error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.OPENAPI_GENERATION, 'We are expecting an integration test error'
        assert error.errors.size() == 2, 'We should find two errors'
        assert '[WARNING] Exception while reading:\n' +
            'java.lang.RuntimeException: Unable to load URL ref: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml path: /home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/contract\n' +
            'Caused by: javax.net.ssl.SSLException: Connection reset\n' +
            'Caused by: java.net.SocketException: Connection reset' == error.errors[0], 'We have not got the expected error message'
        assert '[WARNING] Exception while reading:\n' +
            'java.lang.RuntimeException: Unable to load URL ref: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml path: /home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/contract\n' +
            'Caused by: javax.net.ssl.SSLException: Connection reset II\n' +
            'Caused by: java.net.SocketException: Connection reset II' == error.errors[1], 'We have not got the expected error message'
    }

    @Test
    void testGiven_RevapiCheck_NexusDownloadFailure_Failure_returns_NexusDownloadErrorWithFullLog() {

        MavenGoalExecutionFailureError error =
            MavenGoalExecutionFailureErrorDecoder.getErrorFromLog(logMvnOpenApiGenerationNexusDownloadFailure)
        assert error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.NEXUS_DOWNLOAD, 'We are expecting a Nexus download error'
        assert error.errors.size() == 1, 'We should find two errors'
        assert logMvnOpenApiGenerationNexusDownloadFailure == error.errors[0], 'We have not got the expected error message'

    }

    @Test
    void testGiven_RunRemoteITOnPRO_NexusDownload_Failure_returns_NexusDownloadErrorWithFullLog() {

        MavenGoalExecutionFailureError error =
            MavenGoalExecutionFailureErrorDecoder.getErrorFromLog(logMvnRunRemoteITPRONexusDownloadFailure)
        assert error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.NEXUS_DOWNLOAD, 'We are expecting a Nexus download error'
        assert error.errors.size() == 1, 'We should find one error'
        assert logMvnRunRemoteITPRONexusDownloadFailure == error.errors[0], 'We have not got the expected error message'

    }

    @Test
    void testGiven_VersionsSet_NexusPluginsDownload_Failure_returns_NexusDownloadErrorWithFullLog() {

        MavenGoalExecutionFailureError error =
            MavenGoalExecutionFailureErrorDecoder.getErrorFromLog(logMvnVersionsSetNexusPluginDownloadsFailure)
        assert error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.NEXUS_DOWNLOAD, 'We are expecting a Nexus download error'
        assert error.errors.size() == 1, 'We should find one error'
        assert logMvnVersionsSetNexusPluginDownloadsFailure == error.errors[0], 'We have not got the expected error message'

    }

    @Test
    void testGiven_SonarScan_Failure_DueToConnectionTimeoutAgainstSonar_returns_SonarPluginErrorWithFullLog() {

        MavenGoalExecutionFailureError error =
            MavenGoalExecutionFailureErrorDecoder.getErrorFromLog(logMvnSonarFailureDueToConnectionTimeout)
        assert error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.SONAR_SCAN_CONNECTIVITY_ISSUE, 'We are expecting a Sonar connectivity error'
        assert error.errors.size() == 1, 'We should find one error'
        assert logMvnSonarFailureDueToConnectionTimeout == error.errors[0], 'We have not got the expected error message'

    }

    @Test
    void testGiven_SonarScanPluginDownload_Failure_DueToConnectionReset_returns_SonarPluginErrorWithFullLog() {

        MavenGoalExecutionFailureError error =
            MavenGoalExecutionFailureErrorDecoder.getErrorFromLog(logMvnSonarFailureDueToConnectionResetAgainstNexus)
        assert error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.SONAR_SCAN_CONNECTIVITY_ISSUE, 'We are expecting a Nexus download error'
        assert error.errors.size() == 1, 'We should find one error'
        assert logMvnSonarFailureDueToConnectionResetAgainstNexus == error.errors[0], 'We have not got the expected error message'

    }

    @Test
    void testGiven_SonarScanPluginDownload_Failure_DueToConnectionStreamReset_returns_SonarPluginErrorWithFullLog() {

        MavenGoalExecutionFailureError error =
            MavenGoalExecutionFailureErrorDecoder.getErrorFromLog(logMvnSonarFailureDueToConnectionStreamResetAgainstNexus)
        assert error.mavenGoalExecutionFailureErrorType == MavenGoalExecutionFailureErrorType.SONAR_SCAN_CONNECTIVITY_ISSUE, 'We are expecting a Nexus download error'
        assert error.errors.size() == 1, 'We should find one error'
        assert logMvnSonarFailureDueToConnectionStreamResetAgainstNexus == error.errors[0], 'We have not got the expected error message'

    }

}
