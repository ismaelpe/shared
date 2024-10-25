package com.caixabank.absis3

import org.junit.Test

class MavenGoalExecutionFailureDueToTestsLogParserTest extends GroovyTestCase {

    String logMvnRunRemoteITSSLHandshakeTimeoutFail =
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
        '[INFO] \n' +
        '[INFO] Results:\n' +
        '[INFO] \n' +
        '[ERROR] Errors: \n' +
        '[ERROR]   MonitoringIT.testSendMessageToMonitoring:97 » Reactive io.netty.handler.ssl.Ss...\n' +
        '[INFO] \n' +
        '[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0\n' +
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
        '[ERROR] Please refer to /home/jenkins/workspace/absis3/services/arch/monitoring/monitoring-micro/monitoring-micro-server/target/failsafe-reports for the individual test results.\n' +
        '[ERROR] Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.\n' +
        '[ERROR] -> [Help 1]\n' +
        '[ERROR] \n' +
        '[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n' +
        '[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n' +
        '[ERROR] \n' +
        '[ERROR] For more information about the errors and possible solutions, please read the following articles:\n' +
        '[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException\n' +
        '[ERROR] \n' +
        '[ERROR] After correcting the problems, you can resume the build with the command\n' +
        '[ERROR]   mvn <goals> -rf :monitoring-micro-server\n'

    String logMvnRunRemoteITSSLConnectionResetFail =
        '[INFO] -------------------------------------------------------\n' +
        '[INFO]  T E S T S\n' +
        '[INFO] -------------------------------------------------------\n' +
        '[INFO] Running com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.GwExternoTestIT\n' +
        'Mar 23, 2021 5:11:22 PM org.jboss.logmanager.JBossLoggerFinder getLogger\n' +
        'ERROR: The LogManager accessed before the "java.util.logging.manager" system property was set to "org.jboss.logmanager.LogManager". Results may be unexpected.\n' +
        'HTTP/1.1 200 \n' +
        'Date: Tue, 23 Mar 2021 17:11:30 GMT\n' +
        'Content-Type: application/json\n' +
        'Transfer-Encoding: chunked\n' +
        'Connection: keep-alive\n' +
        'Vary: Accept-Encoding\n' +
        'x-absis-request-id: test-gen-737dd37f-9fea-4f20-b47d-39fb4b63963d\n' +
        'Strict-Transport-Security: max-age=15724800; includeSubDomains\n' +
        'Content-Encoding: gzip\n' +
        '\n' +
        'eyJhbGciOiJQQkVTMi1IUzI1NitBMTI4S1ciLCJlbmMiOiJBMTI4Q0JDLUhTMjU2Iiwia2lkIjoidjEiLCJjdHkiOiJKV1QiLCJ0dHkiOiJBVCIsInRpZCI6IkFUMUEiLCJqdGkiOiI2YmJiNGEwZi03ZTIwLTRmNTYtOWU3My05MGE0ZWZlYjAwMDAiLCJleHAiOiIxNjE2NTYyNjg4IiwiYXVkIjoiYXBpZ3d0LnByZS5zZXJ2ZWlzLmFic2lzY2xvdWQubGFjYWl4YS5lcyxhcGlnd3QucHJlLmFic2lzY2xvdWQubGFjYWl4YS5lcyxhcGkudHN0LmludGVybmFsLmNlci5jYWl4YWJhbmsuY29tIiwicDJjIjo4MTkyLCJwMnMiOiJxbmpwV1ZKVjk1OHd0Z09lIn0.GZdBMswOaH91v9MzHj5f8aPvdF0zrErcdbkobMtWkYB6AaLNpHZn5w.GMF3QpkwEzhTJY3BwNavsQ.RCReOM6IanYkpwjDMD5euIYsHpRU9C3rHVNug45yims3tm2eXD802ABN3M_HnYe6v5S86tF-5h2eBCWrEYXDpQ4K6qqgigrnduieuWRKgvewOkE28E-hAw2iKbyIlgPGQsT3pP34VNBoGsMFRnrTN8TrxGOs1UhYEF3vM59e6ZZh1uK_0AMZRhEvlV_Zk0RiCNVvzFVkknuqg5O-9X1wnbv-n-XDE7miinWrfHOFDGflcmWBfueCRaYf7brLF5vBQ-hTdJa_BN5xUDyh1PcSCV63cUK04TnmuDUKyJu5pJVZaYzw6XC7UOmDFxuSneIjU8KdojaLjfg3ztjb1Y6nwE6a9pgWAu_0U4Feq7qAmVrZhqRIWx9I7P7Yjo3ER8lXLaTO2ZWOp0lxFFyICogpGEiEXtFxf7-2w3puaO7iXUNKithSdvEYr41x5fUjvhyqgUEEwMwgonVH-JAjcHPv618xBqltTv_cn2p3DpAuqwvPN7J3V8AUVEA5LkBcHZfPMPVdNJDoWm6d_y1DYi68qqi-bksHzBZbb39Ij-I2cmgREvHrD8QfWeRUv_1uhT88AWxyQPFumiV_Fs6YlfLqg3EE2VvUV-ixN9L8ZUx6za8997B0KHELXox3EfnoPkAXvejZiY3AWEKJYWaHPUV072TYGcUzxr19sCElRTvqvdX-yPsyCDpH8btPcO-TJt6CpQ4amNdOMJYlBCsqeQCr8UMnwQ8_TVD2-BUtpE8ihmZcoO4uB2rhYAD7MHWEHE2DzF5-qEL7jLqveDjNxisw6PjDGT5gXyGOHUxof8tWf6YL7pd30V5Z7OWEE-3ea5xfDraQuBADh-DeXbSuz6JNhxDxKRfqkwzXkwxbZUZ88d6vkfoGFMlhu_JXFsVb7r97iNRHfmfhyDIEcDv1BxcHbIETRqscDPKL91bbMwckCs04JV59vZWTdj0ZhQr19u61ZNOt9wEBiT6k0F6F_foXZm4OdpoXzlGk80S3HH7yT7U8WzPgIepz8y_VHnLszK1RZztCk1q7-7z7nkI6gFDCF-HMxPnoiXhv8R_Ljp3pm3KbljPSeRIFoPkeg0QNOjyfRd_Uw2YETAoBOvqgHRpma-s6Ql1s7gOuwHM8VAHEPN7ZaM8ccv8GXzLxZzDC5EFlID23Skl7JZxDl4dbDK60mjbhiAZXOVxj97iOkf5ZRbhqkg4suSjUsSwFf5oUZ4ppqJpQ5dDn9RtTQ3R32EkyzhQGIfvN3VKyX8pGdrtVqxlXDu9g7tg70pZPLS3nSp0n.3fHwRY72JAzkfHMU6VYwRg\n' +
        '[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 9.376 s - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.GwExternoTestIT\n' +
        '[INFO] Running com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.TauxTestIT\n' +
        'HTTP/1.1 200 \n' +
        'Date: Tue, 23 Mar 2021 17:11:33 GMT\n' +
        'Content-Type: application/json\n' +
        'Transfer-Encoding: chunked\n' +
        'Connection: keep-alive\n' +
        'Vary: Accept-Encoding\n' +
        'x-absis-request-id: test-gen-65bb3d86-5b09-4c56-a439-cef034234089\n' +
        'Strict-Transport-Security: max-age=15724800; includeSubDomains\n' +
        'Content-Encoding: gzip\n' +
        '\n' +
        'true\n' +
        'HTTP/1.1 200 \n' +
        'Date: Tue, 23 Mar 2021 17:11:33 GMT\n' +
        'Content-Type: text/plain;charset=UTF-8\n' +
        'Transfer-Encoding: chunked\n' +
        'Connection: keep-alive\n' +
        'Vary: Accept-Encoding\n' +
        'x-absis-request-id: test-gen-65bb3d86-5b09-4c56-a439-cef034234089\n' +
        'Strict-Transport-Security: max-age=15724800; includeSubDomains\n' +
        'Content-Encoding: gzip\n' +
        '\n' +
        'Dipòsit subjecte a abonament Nòmines\n' +
        '[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.209 s - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.TauxTestIT\n' +
        '[INFO] Running com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT\n' +
        '[ERROR] Tests run: 31, Failures: 0, Errors: 1, Skipped: 6, Time elapsed: 302.052 s <<< FAILURE! - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT\n' +
        '[ERROR] testPostCheckjson  Time elapsed: 256.14 s  <<< ERROR!\n' +
        'javax.net.ssl.SSLException: Connection reset\n' +
        ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)\n' +
        'Caused by: java.net.SocketException: Connection reset\n' +
        ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)\n' +
        '\n' +
        '[INFO] Running com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.AdsTestIT\n' +
        '[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.71 s - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.AdsTestIT\n' +
        '[INFO] Running com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.CertificatesTestIT\n' +
        'HTTP/1.1 200 \n' +
        'Date: Tue, 23 Mar 2021 17:16:38 GMT\n' +
        'Content-Type: application/json\n' +
        'Transfer-Encoding: chunked\n' +
        'Connection: keep-alive\n' +
        'Vary: Accept-Encoding\n' +
        'x-absis-request-id: test-gen-8796d9c4-c812-4bac-99cc-dfb2a9ad305b\n' +
        'Strict-Transport-Security: max-age=15724800; includeSubDomains\n' +
        'Content-Encoding: gzip\n' +
        '\n' +
        '{\n' +
        'result": "OK Ssl\n' +
        '}\n' +
        '[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.198 s - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.CertificatesTestIT\n' +
        '[INFO] Running com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.DigitizationTestIT\n' +
        'HTTP/1.1 200 \n' +
        'Date: Tue, 23 Mar 2021 17:16:40 GMT\n' +
        'Content-Type: application/json\n' +
        'Transfer-Encoding: chunked\n' +
        'Connection: keep-alive\n' +
        'Vary: Accept-Encoding\n' +
        'x-absis-request-id: test-gen-9c28e24c-78d4-4dbc-af8b-6479d3d71130\n' +
        'Strict-Transport-Security: max-age=15724800; includeSubDomains\n' +
        'Content-Encoding: gzip\n' +
        '\n' +
        '{\n' +
        'id": "6228205818483407942\n' +
        '}\n' +
        'HTTP/1.1 204 \n' +
        'Date: Tue, 23 Mar 2021 17:16:42 GMT\n' +
        'Connection: keep-alive\n' +
        'x-absis-request-id: test-gen-5d050804-2d1a-435e-b788-0b4b2eb9f41b\n' +
        'Strict-Transport-Security: max-age=15724800; includeSubDomains\n' +
        'HTTP/1.1 200 \n' +
        'Date: Tue, 23 Mar 2021 17:16:43 GMT\n' +
        'Content-Type: text/plain\n' +
        'Transfer-Encoding: chunked\n' +
        'Connection: keep-alive\n' +
        'Vary: Accept-Encoding\n' +
        'x-absis-request-id: test-gen-231bc2df-26e9-4ec4-8473-2312532defdc\n' +
        'Strict-Transport-Security: max-age=15724800; includeSubDomains\n' +
        'Content-Encoding: gzip\n' +
        '\n' +
        'Test-Content-In-File\n' +
        '[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.267 s - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.DigitizationTestIT\n' +
        '[INFO] Running com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.ClienteTestIT\n' +
        'HTTP/1.1 200 \n' +
        'Date: Tue, 23 Mar 2021 17:16:44 GMT\n' +
        'Content-Type: application/json\n' +
        'Transfer-Encoding: chunked\n' +
        'Connection: keep-alive\n' +
        'Vary: Accept-Encoding\n' +
        'x-absis-request-id: test-gen-71320464-e2f6-4af4-9bbb-79bceb7a9328\n' +
        'Strict-Transport-Security: max-age=15724800; includeSubDomains\n' +
        'Content-Encoding: gzip\n' +
        '\n' +
        '{\n' +
        '   "id": 4,\n' +
        '   "nombre": "CBK Samuel",\n' +
        '   "apellidos": "Umtiti"' +
        '}\n' +
        '[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.112 s - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.ClienteTestIT\n' +
        '[INFO] Running com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.DecIT\n' +
        '[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.477 s - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.DecIT\n' +
        '[INFO] Running com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.JMSProducerIT\n' +
        '[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.785 s - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.JMSProducerIT\n' +
        '[INFO] \n' +
        '[INFO] Results:\n' +
        '[INFO] \n' +
        '[ERROR] Errors: \n' +
        '[ERROR]   SeconnectorMicroIT.testPostCheckjson:97 » SSL Connection reset\n' +
        '[INFO] \n' +
        '[ERROR] Tests run: 43, Failures: 0, Errors: 1, Skipped: 6\n' +
        '[INFO] \n' +
        '[INFO] \n' +
        '[INFO] --- maven-failsafe-plugin:2.22.1:verify (integration-test) @ demoarqcbk-micro ---\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] BUILD FAILURE\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] Total time:  06:27 min\n' +
        '[INFO] Finished at: 2021-03-23T17:16:47Z\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[ERROR] Failed to execute goal org.apache.maven.plugins:maven-failsafe-plugin:2.22.1:verify (integration-test) on project demoarqcbk-micro: There are test failures.\n' +
        '[ERROR] \n' +
        '[ERROR] Please refer to /home/jenkins/workspace/absis3/services/apps/cbk/service/demoArquitectura/demoarqcbk-micro/target/failsafe-reports for the individual test results.\n' +
        '[ERROR] Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.\n' +
        '[ERROR] -> [Help 1]\n' +
        '[ERROR] \n' +
        '[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n' +
        '[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n' +
        '[ERROR] \n' +
        '[ERROR] For more information about the errors and possible solutions, please read the following articles:\n' +
        '[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException\n'

    String logMvnRunRemoteITFailWithTwoDifferentSSLErrors =
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
        '[INFO] Total time:  06:27 min\n' +
        '[INFO] Finished at: 2021-03-23T17:16:47Z\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[ERROR] Failed to execute goal org.apache.maven.plugins:maven-failsafe-plugin:2.22.1:verify (integration-test) on project demoarqcbk-micro: There are test failures.\n' +
        '[ERROR] \n' +
        '[ERROR] Please refer to /home/jenkins/workspace/absis3/services/apps/cbk/service/demoArquitectura/demoarqcbk-micro/target/failsafe-reports for the individual test results.\n' +
        '[ERROR] Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.\n' +
        '[ERROR] -> [Help 1]\n' +
        '[ERROR] \n' +
        '[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n' +
        '[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n' +
        '[ERROR] \n' +
        '[ERROR] For more information about the errors and possible solutions, please read the following articles:\n' +
        '[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException\n'

    String logMvnBuildSuccess =
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] BUILD SUCCESS\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] Total time:  01:40 min\n' +
            '[INFO] Finished at: 2019-10-31T12:05:11Z\n' +
            '[INFO] ------------------------------------------------------------------------\n'

    @Test
    void testGiven_BuildSuccess_returns_NoCauses() {
        def causes = new MavenGoalExecutionFailureDueToTestsLogParser().parseErrors(logMvnBuildSuccess)
        assert causes.size() == 0, 'We should find no errors'
    }

    @Test
    void testGiven_ITTest_TwoDifferentSSLErrors_Failure_returns_causes() {
        def causes = new MavenGoalExecutionFailureDueToTestsLogParser().parseErrors(logMvnRunRemoteITFailWithTwoDifferentSSLErrors)
        println "Cause(s):\n ${causes}"
        assert causes.size() == 2, 'We should find two errors'
        assert '[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 13.41 s <<< FAILURE! - in com.caixabank.absis.arch.monitoring.integration.MonitoringIT\n' +
            '[ERROR] testSendMessageToMonitoring  Time elapsed: 12.145 s  <<< ERROR!\n' +
            'reactor.core.Exceptions$ReactiveException: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms\n' +
            ' at com.caixabank.absis.arch.monitoring.integration.MonitoringIT.testSendMessageToMonitoring(MonitoringIT.java:97)\n' +
            'Caused by: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms' == causes[0], 'We have not got the expected error message'
        assert '[ERROR] Tests run: 31, Failures: 0, Errors: 1, Skipped: 6, Time elapsed: 302.052 s <<< FAILURE! - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT\n' +
            '[ERROR] testPostCheckjson  Time elapsed: 256.14 s  <<< ERROR!\n' +
            'javax.net.ssl.SSLException: Connection reset\n' +
            ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)\n' +
            'Caused by: java.net.SocketException: Connection reset\n' +
            ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)' == causes[1], 'We have not got the expected error message'
    }

    @Test
    void testGiven_ITTest_SSLHandshakeTimeout_Failure_returns_OneCause() {
        def causes = new MavenGoalExecutionFailureDueToTestsLogParser().parseErrors(logMvnRunRemoteITSSLHandshakeTimeoutFail)
        println "Cause(s):\n ${causes}"
        assert causes.size() == 1, 'We should find one error'
        assert '[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 13.41 s <<< FAILURE! - in com.caixabank.absis.arch.monitoring.integration.MonitoringIT\n' +
            '[ERROR] testSendMessageToMonitoring  Time elapsed: 12.145 s  <<< ERROR!\n' +
            'reactor.core.Exceptions$ReactiveException: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms\n' +
            ' at com.caixabank.absis.arch.monitoring.integration.MonitoringIT.testSendMessageToMonitoring(MonitoringIT.java:97)\n' +
            'Caused by: io.netty.handler.ssl.SslHandshakeTimeoutException: handshake timed out after 10000ms' == causes[0], 'We have not got the expected error message'
    }

    @Test
    void testGiven_ITTest_SSLConnectionReset_Failure_returns_OneCause() {
        def causes = new MavenGoalExecutionFailureDueToTestsLogParser().parseErrors(logMvnRunRemoteITSSLConnectionResetFail)
        println "Cause(s):\n ${causes}"
        assert causes.size() == 1, 'We should find one error'
        assert '[ERROR] Tests run: 31, Failures: 0, Errors: 1, Skipped: 6, Time elapsed: 302.052 s <<< FAILURE! - in com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT\n' +
            '[ERROR] testPostCheckjson  Time elapsed: 256.14 s  <<< ERROR!\n' +
            'javax.net.ssl.SSLException: Connection reset\n' +
            ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)\n' +
            'Caused by: java.net.SocketException: Connection reset\n' +
            ' at com.caixabank.absis.apps.service.demoarqcbk.demoarqcbkmicro.integration.SeconnectorMicroIT.testPostCheckjson(SeconnectorMicroIT.java:97)' == causes[0], 'We have not got the expected error message'
    }

}
