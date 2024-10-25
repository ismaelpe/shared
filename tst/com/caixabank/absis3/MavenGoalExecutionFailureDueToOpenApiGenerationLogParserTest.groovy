package com.caixabank.absis3

import org.junit.Test

class MavenGoalExecutionFailureDueToOpenApiGenerationLogParserTest extends GroovyTestCase {

    static String logMvnOpenApiGenerationSSLExceptionWhenContactingContractServer =
                '[INFO] Scanning for projects...\n' +
                '[INFO] \n' +
                '[INFO] -------< com.caixabank.absis.apps.dataservice.demo:arqpru-micro >-------\n' +
                '[INFO] Building arqpru-micro 2.5.0-RC1\n' +
                '[INFO] --------------------------------[ jar ]---------------------------------\n' +
                '[WARNING] The POM for com.sun.xml.bind:jaxb-osgi:jar:2.2.10 is invalid, transitive dependencies (if any) will not be available, enable debug logging for more details\n' +
                '[INFO] \n' +
                '[INFO] --- jacoco-maven-plugin:0.8.6:prepare-agent (default) @ arqpru-micro ---\n' +
                '[INFO] argLine set to -javaagent:/root/.m2/repository/org/jacoco/org.jacoco.agent/0.8.6/org.jacoco.agent-0.8.6-runtime.jar=destfile=/home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/target/jacoco.exec,append=true\n' +
                '[INFO] \n' +
                '[INFO] --- codegen-postprocess-plugin:1.16.1:delete-model (delete-model) @ arqpru-micro ---\n' +
                '[INFO] Source directory is:/home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/src/main/java\n' +
                '[INFO] \twith apiPackage:com.caixabank.absis.apps.dataservice.demo.arqpru.api.domain\n' +
                '[INFO] \tsearch model api on:/home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/src/main/java/com/caixabank/absis/apps/dataservice/demo/arqpru/api/domain\n' +
                '[INFO] \tsearch interface api on:/home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/src/main/java/com/caixabank/absis/apps/dataservice/demo/arqpru/api\n' +
                '[INFO] Deleting Model file:/home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/src/main/java/com/caixabank/absis/apps/dataservice/demo/arqpru/api/domain/ArqpruMicroTOPage.java\n' +
                '[INFO] Deleting Model file:/home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/src/main/java/com/caixabank/absis/apps/dataservice/demo/arqpru/api/domain/ArqpruMicroTO.java\n' +
                '[INFO] Deleting Model file:/home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/src/main/java/com/caixabank/absis/apps/dataservice/demo/arqpru/api/domain/Sort.java\n' +
                '[INFO] Deleting API file:/home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/src/main/java/com/caixabank/absis/apps/dataservice/demo/arqpru/api/ArqpruMicroApi.java\n' +
                '[INFO] \n' +
                '[INFO] --- openapi-generator-maven-plugin:4.3.1:generate (default) @ arqpru-micro ---\n' +
                '[ERROR] unable to read\n' +
                'javax.net.ssl.SSLException: Connection reset\n' +
                '    at sun.security.ssl.Alert.createSSLException (Alert.java:127)\n' +
                '    at sun.security.ssl.TransportContext.fatal (TransportContext.java:320)\n' +
                '    at sun.security.ssl.TransportContext.fatal (TransportContext.java:263)\n' +
                '    at sun.security.ssl.TransportContext.fatal (TransportContext.java:258)\n' +
                '    at sun.security.ssl.SSLTransport.decode (SSLTransport.java:137)\n' +
                '    at sun.security.ssl.SSLSocketImpl.decode (SSLSocketImpl.java:1151)\n' +
                '    at sun.security.ssl.SSLSocketImpl.readHandshakeRecord (SSLSocketImpl.java:1062)\n' +
                '    at sun.security.ssl.SSLSocketImpl.startHandshake (SSLSocketImpl.java:402)\n' +
                '    at sun.net.www.protocol.https.HttpsClient.afterConnect (HttpsClient.java:567)\n' +
                '    at sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect (AbstractDelegateHttpsURLConnection.java:185)\n' +
                '    at sun.net.www.protocol.https.HttpsURLConnectionImpl.connect (HttpsURLConnectionImpl.java:168)\n' +
                '    at io.swagger.v3.parser.util.RemoteUrl.urlToString (RemoteUrl.java:149)\n' +
                '    at io.swagger.v3.parser.util.RefUtils.readExternalRef (RefUtils.java:204)\n' +
                '    at io.swagger.v3.parser.ResolverCache.loadRef (ResolverCache.java:119)\n' +
                '    at io.swagger.v3.parser.processors.ExternalRefProcessor.processRefToExternalResponse (ExternalRefProcessor.java:334)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processReferenceResponse (ResponseProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processResponse (ResponseProcessor.java:41)\n' +
                '    at io.swagger.v3.parser.processors.OperationProcessor.processOperation (OperationProcessor.java:56)\n' +
                '    at io.swagger.v3.parser.processors.PathsProcessor.processPaths (PathsProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.OpenAPIResolver.resolve (OpenAPIResolver.java:49)\n' +
                '    at io.swagger.v3.parser.OpenAPIV3Parser.readLocation (OpenAPIV3Parser.java:67)\n' +
                '    at io.swagger.parser.OpenAPIParser.readLocation (OpenAPIParser.java:16)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toContext (CodegenConfigurator.java:461)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toClientOptInput (CodegenConfigurator.java:507)\n' +
                '    at org.openapitools.codegen.plugin.CodeGenMojo.execute (CodeGenMojo.java:724)\n' +
                '    at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo (DefaultBuildPluginManager.java:137)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:210)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:156)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:148)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:117)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:81)\n' +
                '    at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build (SingleThreadedBuilder.java:56)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleStarter.execute (LifecycleStarter.java:128)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:305)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:192)\n' +
                '    at org.apache.maven.DefaultMaven.execute (DefaultMaven.java:105)\n' +
                '    at org.apache.maven.cli.MavenCli.execute (MavenCli.java:956)\n' +
                '    at org.apache.maven.cli.MavenCli.doMain (MavenCli.java:288)\n' +
                '    at org.apache.maven.cli.MavenCli.main (MavenCli.java:192)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke (NativeMethodAccessorImpl.java:62)\n' +
                '    at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java:43)\n' +
                '    at java.lang.reflect.Method.invoke (Method.java:566)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (Launcher.java:282)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launch (Launcher.java:225)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (Launcher.java:406)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.main (Launcher.java:347)\n' +
                'Caused by: java.net.SocketException: Connection reset\n' +
                '    at java.net.SocketInputStream.read (SocketInputStream.java:186)\n' +
                '    at java.net.SocketInputStream.read (SocketInputStream.java:140)\n' +
                '    at sun.security.ssl.SSLSocketInputRecord.read (SSLSocketInputRecord.java:448)\n' +
                '    at sun.security.ssl.SSLSocketInputRecord.decode (SSLSocketInputRecord.java:165)\n' +
                '    at sun.security.ssl.SSLTransport.decode (SSLTransport.java:108)\n' +
                '    at sun.security.ssl.SSLSocketImpl.decode (SSLSocketImpl.java:1151)\n' +
                '    at sun.security.ssl.SSLSocketImpl.readHandshakeRecord (SSLSocketImpl.java:1062)\n' +
                '    at sun.security.ssl.SSLSocketImpl.startHandshake (SSLSocketImpl.java:402)\n' +
                '    at sun.net.www.protocol.https.HttpsClient.afterConnect (HttpsClient.java:567)\n' +
                '    at sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect (AbstractDelegateHttpsURLConnection.java:185)\n' +
                '    at sun.net.www.protocol.https.HttpsURLConnectionImpl.connect (HttpsURLConnectionImpl.java:168)\n' +
                '    at io.swagger.v3.parser.util.RemoteUrl.urlToString (RemoteUrl.java:149)\n' +
                '    at io.swagger.v3.parser.util.RefUtils.readExternalRef (RefUtils.java:204)\n' +
                '    at io.swagger.v3.parser.ResolverCache.loadRef (ResolverCache.java:119)\n' +
                '    at io.swagger.v3.parser.processors.ExternalRefProcessor.processRefToExternalResponse (ExternalRefProcessor.java:334)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processReferenceResponse (ResponseProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processResponse (ResponseProcessor.java:41)\n' +
                '    at io.swagger.v3.parser.processors.OperationProcessor.processOperation (OperationProcessor.java:56)\n' +
                '    at io.swagger.v3.parser.processors.PathsProcessor.processPaths (PathsProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.OpenAPIResolver.resolve (OpenAPIResolver.java:49)\n' +
                '    at io.swagger.v3.parser.OpenAPIV3Parser.readLocation (OpenAPIV3Parser.java:67)\n' +
                '    at io.swagger.parser.OpenAPIParser.readLocation (OpenAPIParser.java:16)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toContext (CodegenConfigurator.java:461)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toClientOptInput (CodegenConfigurator.java:507)\n' +
                '    at org.openapitools.codegen.plugin.CodeGenMojo.execute (CodeGenMojo.java:724)\n' +
                '    at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo (DefaultBuildPluginManager.java:137)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:210)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:156)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:148)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:117)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:81)\n' +
                '    at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build (SingleThreadedBuilder.java:56)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleStarter.execute (LifecycleStarter.java:128)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:305)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:192)\n' +
                '    at org.apache.maven.DefaultMaven.execute (DefaultMaven.java:105)\n' +
                '    at org.apache.maven.cli.MavenCli.execute (MavenCli.java:956)\n' +
                '    at org.apache.maven.cli.MavenCli.doMain (MavenCli.java:288)\n' +
                '    at org.apache.maven.cli.MavenCli.main (MavenCli.java:192)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke (NativeMethodAccessorImpl.java:62)\n' +
                '    at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java:43)\n' +
                '    at java.lang.reflect.Method.invoke (Method.java:566)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (Launcher.java:282)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launch (Launcher.java:225)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (Launcher.java:406)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.main (Launcher.java:347)\n' +
                '[WARNING] Exception while reading:\n' +
                'java.lang.RuntimeException: Unable to load URL ref: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml path: /home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/contract\n' +
                '    at io.swagger.v3.parser.util.RefUtils.readExternalRef (RefUtils.java:239)\n' +
                '    at io.swagger.v3.parser.ResolverCache.loadRef (ResolverCache.java:119)\n' +
                '    at io.swagger.v3.parser.processors.ExternalRefProcessor.processRefToExternalResponse (ExternalRefProcessor.java:334)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processReferenceResponse (ResponseProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processResponse (ResponseProcessor.java:41)\n' +
                '    at io.swagger.v3.parser.processors.OperationProcessor.processOperation (OperationProcessor.java:56)\n' +
                '    at io.swagger.v3.parser.processors.PathsProcessor.processPaths (PathsProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.OpenAPIResolver.resolve (OpenAPIResolver.java:49)\n' +
                '    at io.swagger.v3.parser.OpenAPIV3Parser.readLocation (OpenAPIV3Parser.java:67)\n' +
                '    at io.swagger.parser.OpenAPIParser.readLocation (OpenAPIParser.java:16)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toContext (CodegenConfigurator.java:461)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toClientOptInput (CodegenConfigurator.java:507)\n' +
                '    at org.openapitools.codegen.plugin.CodeGenMojo.execute (CodeGenMojo.java:724)\n' +
                '    at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo (DefaultBuildPluginManager.java:137)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:210)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:156)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:148)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:117)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:81)\n' +
                '    at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build (SingleThreadedBuilder.java:56)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleStarter.execute (LifecycleStarter.java:128)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:305)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:192)\n' +
                '    at org.apache.maven.DefaultMaven.execute (DefaultMaven.java:105)\n' +
                '    at org.apache.maven.cli.MavenCli.execute (MavenCli.java:956)\n' +
                '    at org.apache.maven.cli.MavenCli.doMain (MavenCli.java:288)\n' +
                '    at org.apache.maven.cli.MavenCli.main (MavenCli.java:192)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke (NativeMethodAccessorImpl.java:62)\n' +
                '    at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java:43)\n' +
                '    at java.lang.reflect.Method.invoke (Method.java:566)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (Launcher.java:282)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launch (Launcher.java:225)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (Launcher.java:406)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.main (Launcher.java:347)\n' +
                'Caused by: javax.net.ssl.SSLException: Connection reset\n' +
                '    at sun.security.ssl.Alert.createSSLException (Alert.java:127)\n' +
                '    at sun.security.ssl.TransportContext.fatal (TransportContext.java:320)\n' +
                '    at sun.security.ssl.TransportContext.fatal (TransportContext.java:263)\n' +
                '    at sun.security.ssl.TransportContext.fatal (TransportContext.java:258)\n' +
                '    at sun.security.ssl.SSLTransport.decode (SSLTransport.java:137)\n' +
                '    at sun.security.ssl.SSLSocketImpl.decode (SSLSocketImpl.java:1151)\n' +
                '    at sun.security.ssl.SSLSocketImpl.readHandshakeRecord (SSLSocketImpl.java:1062)\n' +
                '    at sun.security.ssl.SSLSocketImpl.startHandshake (SSLSocketImpl.java:402)\n' +
                '    at sun.net.www.protocol.https.HttpsClient.afterConnect (HttpsClient.java:567)\n' +
                '    at sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect (AbstractDelegateHttpsURLConnection.java:185)\n' +
                '    at sun.net.www.protocol.https.HttpsURLConnectionImpl.connect (HttpsURLConnectionImpl.java:168)\n' +
                '    at io.swagger.v3.parser.util.RemoteUrl.urlToString (RemoteUrl.java:149)\n' +
                '    at io.swagger.v3.parser.util.RefUtils.readExternalRef (RefUtils.java:204)\n' +
                '    at io.swagger.v3.parser.ResolverCache.loadRef (ResolverCache.java:119)\n' +
                '    at io.swagger.v3.parser.processors.ExternalRefProcessor.processRefToExternalResponse (ExternalRefProcessor.java:334)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processReferenceResponse (ResponseProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processResponse (ResponseProcessor.java:41)\n' +
                '    at io.swagger.v3.parser.processors.OperationProcessor.processOperation (OperationProcessor.java:56)\n' +
                '    at io.swagger.v3.parser.processors.PathsProcessor.processPaths (PathsProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.OpenAPIResolver.resolve (OpenAPIResolver.java:49)\n' +
                '    at io.swagger.v3.parser.OpenAPIV3Parser.readLocation (OpenAPIV3Parser.java:67)\n' +
                '    at io.swagger.parser.OpenAPIParser.readLocation (OpenAPIParser.java:16)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toContext (CodegenConfigurator.java:461)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toClientOptInput (CodegenConfigurator.java:507)\n' +
                '    at org.openapitools.codegen.plugin.CodeGenMojo.execute (CodeGenMojo.java:724)\n' +
                '    at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo (DefaultBuildPluginManager.java:137)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:210)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:156)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:148)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:117)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:81)\n' +
                '    at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build (SingleThreadedBuilder.java:56)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleStarter.execute (LifecycleStarter.java:128)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:305)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:192)\n' +
                '    at org.apache.maven.DefaultMaven.execute (DefaultMaven.java:105)\n' +
                '    at org.apache.maven.cli.MavenCli.execute (MavenCli.java:956)\n' +
                '    at org.apache.maven.cli.MavenCli.doMain (MavenCli.java:288)\n' +
                '    at org.apache.maven.cli.MavenCli.main (MavenCli.java:192)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke (NativeMethodAccessorImpl.java:62)\n' +
                '    at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java:43)\n' +
                '    at java.lang.reflect.Method.invoke (Method.java:566)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (Launcher.java:282)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launch (Launcher.java:225)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (Launcher.java:406)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.main (Launcher.java:347)\n' +
                'Caused by: java.net.SocketException: Connection reset\n' +
                '    at java.net.SocketInputStream.read (SocketInputStream.java:186)\n' +
                '    at java.net.SocketInputStream.read (SocketInputStream.java:140)\n' +
                '    at sun.security.ssl.SSLSocketInputRecord.read (SSLSocketInputRecord.java:448)\n' +
                '    at sun.security.ssl.SSLSocketInputRecord.decode (SSLSocketInputRecord.java:165)\n' +
                '    at sun.security.ssl.SSLTransport.decode (SSLTransport.java:108)\n' +
                '    at sun.security.ssl.SSLSocketImpl.decode (SSLSocketImpl.java:1151)\n' +
                '    at sun.security.ssl.SSLSocketImpl.readHandshakeRecord (SSLSocketImpl.java:1062)\n' +
                '    at sun.security.ssl.SSLSocketImpl.startHandshake (SSLSocketImpl.java:402)\n' +
                '    at sun.net.www.protocol.https.HttpsClient.afterConnect (HttpsClient.java:567)\n' +
                '    at sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect (AbstractDelegateHttpsURLConnection.java:185)\n' +
                '    at sun.net.www.protocol.https.HttpsURLConnectionImpl.connect (HttpsURLConnectionImpl.java:168)\n' +
                '    at io.swagger.v3.parser.util.RemoteUrl.urlToString (RemoteUrl.java:149)\n' +
                '    at io.swagger.v3.parser.util.RefUtils.readExternalRef (RefUtils.java:204)\n' +
                '    at io.swagger.v3.parser.ResolverCache.loadRef (ResolverCache.java:119)\n' +
                '    at io.swagger.v3.parser.processors.ExternalRefProcessor.processRefToExternalResponse (ExternalRefProcessor.java:334)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processReferenceResponse (ResponseProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processResponse (ResponseProcessor.java:41)\n' +
                '    at io.swagger.v3.parser.processors.OperationProcessor.processOperation (OperationProcessor.java:56)\n' +
                '    at io.swagger.v3.parser.processors.PathsProcessor.processPaths (PathsProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.OpenAPIResolver.resolve (OpenAPIResolver.java:49)\n' +
                '    at io.swagger.v3.parser.OpenAPIV3Parser.readLocation (OpenAPIV3Parser.java:67)\n' +
                '    at io.swagger.parser.OpenAPIParser.readLocation (OpenAPIParser.java:16)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toContext (CodegenConfigurator.java:461)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toClientOptInput (CodegenConfigurator.java:507)\n' +
                '    at org.openapitools.codegen.plugin.CodeGenMojo.execute (CodeGenMojo.java:724)\n' +
                '    at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo (DefaultBuildPluginManager.java:137)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:210)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:156)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:148)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:117)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:81)\n' +
                '    at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build (SingleThreadedBuilder.java:56)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleStarter.execute (LifecycleStarter.java:128)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:305)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:192)\n' +
                '    at org.apache.maven.DefaultMaven.execute (DefaultMaven.java:105)\n' +
                '    at org.apache.maven.cli.MavenCli.execute (MavenCli.java:956)\n' +
                '    at org.apache.maven.cli.MavenCli.doMain (MavenCli.java:288)\n' +
                '    at org.apache.maven.cli.MavenCli.main (MavenCli.java:192)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke (NativeMethodAccessorImpl.java:62)\n' +
                '    at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java:43)\n' +
                '    at java.lang.reflect.Method.invoke (Method.java:566)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (Launcher.java:282)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launch (Launcher.java:225)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (Launcher.java:406)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.main (Launcher.java:347)\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/400\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/401\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/403\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/404\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/500\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/400\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/401\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/403\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/404\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/500\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/400\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/401\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/403\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/404\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/500\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/400\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/401\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/403\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/404\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/500\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/400\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/401\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/403\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/404\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/500\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/400\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/401\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/403\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/404\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/500\n' +
                '[WARNING] /home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/contract/swagger-micro-contract.yaml [0:0]: unexpected error in Open-API generation\n' +
                'org.openapitools.codegen.SpecValidationException: There were issues with the specification. The option can be disabled via validateSpec (Maven/Gradle) or --skip-validate-spec (CLI).\n' +
                ' | Error count: 1, Warning count: 0\n' +
                'Errors: \n' +
                '\t-Unable to load URL ref: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml path: /home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/contract\n' +
                '\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toContext (CodegenConfigurator.java:480)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toClientOptInput (CodegenConfigurator.java:507)\n' +
                '    at org.openapitools.codegen.plugin.CodeGenMojo.execute (CodeGenMojo.java:724)\n' +
                '    at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo (DefaultBuildPluginManager.java:137)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:210)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:156)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:148)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:117)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:81)\n' +
                '    at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build (SingleThreadedBuilder.java:56)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleStarter.execute (LifecycleStarter.java:128)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:305)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:192)\n' +
                '    at org.apache.maven.DefaultMaven.execute (DefaultMaven.java:105)\n' +
                '    at org.apache.maven.cli.MavenCli.execute (MavenCli.java:956)\n' +
                '    at org.apache.maven.cli.MavenCli.doMain (MavenCli.java:288)\n' +
                '    at org.apache.maven.cli.MavenCli.main (MavenCli.java:192)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke (NativeMethodAccessorImpl.java:62)\n' +
                '    at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java:43)\n' +
                '    at java.lang.reflect.Method.invoke (Method.java:566)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (Launcher.java:282)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launch (Launcher.java:225)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (Launcher.java:406)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.main (Launcher.java:347)\n' +
                '[ERROR] \n' +
                'org.openapitools.codegen.SpecValidationException: There were issues with the specification. The option can be disabled via validateSpec (Maven/Gradle) or --skip-validate-spec (CLI).\n' +
                ' | Error count: 1, Warning count: 0\n' +
                '[WARNING] Exception while reading:\n' +
                'java.lang.RuntimeException: Unable to load URL ref: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml path: /home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/contract\n' +
                '    at io.swagger.v3.parser.util.RefUtils.readExternalRef (RefUtils.java:239)\n' +
                '    at io.swagger.v3.parser.ResolverCache.loadRef (ResolverCache.java:119)\n' +
                '    at io.swagger.v3.parser.processors.ExternalRefProcessor.processRefToExternalResponse (ExternalRefProcessor.java:334)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processReferenceResponse (ResponseProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processResponse (ResponseProcessor.java:41)\n' +
                '    at io.swagger.v3.parser.processors.OperationProcessor.processOperation (OperationProcessor.java:56)\n' +
                '    at io.swagger.v3.parser.processors.PathsProcessor.processPaths (PathsProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.OpenAPIResolver.resolve (OpenAPIResolver.java:49)\n' +
                '    at io.swagger.v3.parser.OpenAPIV3Parser.readLocation (OpenAPIV3Parser.java:67)\n' +
                '    at io.swagger.parser.OpenAPIParser.readLocation (OpenAPIParser.java:16)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toContext (CodegenConfigurator.java:461)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toClientOptInput (CodegenConfigurator.java:507)\n' +
                '    at org.openapitools.codegen.plugin.CodeGenMojo.execute (CodeGenMojo.java:724)\n' +
                '    at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo (DefaultBuildPluginManager.java:137)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:210)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:156)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:148)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:117)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:81)\n' +
                '    at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build (SingleThreadedBuilder.java:56)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleStarter.execute (LifecycleStarter.java:128)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:305)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:192)\n' +
                '    at org.apache.maven.DefaultMaven.execute (DefaultMaven.java:105)\n' +
                '    at org.apache.maven.cli.MavenCli.execute (MavenCli.java:956)\n' +
                '    at org.apache.maven.cli.MavenCli.doMain (MavenCli.java:288)\n' +
                '    at org.apache.maven.cli.MavenCli.main (MavenCli.java:192)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke (NativeMethodAccessorImpl.java:62)\n' +
                '    at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java:43)\n' +
                '    at java.lang.reflect.Method.invoke (Method.java:566)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (Launcher.java:282)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launch (Launcher.java:225)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (Launcher.java:406)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.main (Launcher.java:347)\n' +
                'Caused by: javax.net.ssl.SSLException: Connection reset II\n' +
                '    at sun.security.ssl.Alert.createSSLException (Alert.java:127)\n' +
                '    at sun.security.ssl.TransportContext.fatal (TransportContext.java:320)\n' +
                '    at sun.security.ssl.TransportContext.fatal (TransportContext.java:263)\n' +
                '    at sun.security.ssl.TransportContext.fatal (TransportContext.java:258)\n' +
                '    at sun.security.ssl.SSLTransport.decode (SSLTransport.java:137)\n' +
                '    at sun.security.ssl.SSLSocketImpl.decode (SSLSocketImpl.java:1151)\n' +
                '    at sun.security.ssl.SSLSocketImpl.readHandshakeRecord (SSLSocketImpl.java:1062)\n' +
                '    at sun.security.ssl.SSLSocketImpl.startHandshake (SSLSocketImpl.java:402)\n' +
                '    at sun.net.www.protocol.https.HttpsClient.afterConnect (HttpsClient.java:567)\n' +
                '    at sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect (AbstractDelegateHttpsURLConnection.java:185)\n' +
                '    at sun.net.www.protocol.https.HttpsURLConnectionImpl.connect (HttpsURLConnectionImpl.java:168)\n' +
                '    at io.swagger.v3.parser.util.RemoteUrl.urlToString (RemoteUrl.java:149)\n' +
                '    at io.swagger.v3.parser.util.RefUtils.readExternalRef (RefUtils.java:204)\n' +
                '    at io.swagger.v3.parser.ResolverCache.loadRef (ResolverCache.java:119)\n' +
                '    at io.swagger.v3.parser.processors.ExternalRefProcessor.processRefToExternalResponse (ExternalRefProcessor.java:334)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processReferenceResponse (ResponseProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processResponse (ResponseProcessor.java:41)\n' +
                '    at io.swagger.v3.parser.processors.OperationProcessor.processOperation (OperationProcessor.java:56)\n' +
                '    at io.swagger.v3.parser.processors.PathsProcessor.processPaths (PathsProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.OpenAPIResolver.resolve (OpenAPIResolver.java:49)\n' +
                '    at io.swagger.v3.parser.OpenAPIV3Parser.readLocation (OpenAPIV3Parser.java:67)\n' +
                '    at io.swagger.parser.OpenAPIParser.readLocation (OpenAPIParser.java:16)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toContext (CodegenConfigurator.java:461)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toClientOptInput (CodegenConfigurator.java:507)\n' +
                '    at org.openapitools.codegen.plugin.CodeGenMojo.execute (CodeGenMojo.java:724)\n' +
                '    at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo (DefaultBuildPluginManager.java:137)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:210)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:156)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:148)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:117)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:81)\n' +
                '    at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build (SingleThreadedBuilder.java:56)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleStarter.execute (LifecycleStarter.java:128)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:305)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:192)\n' +
                '    at org.apache.maven.DefaultMaven.execute (DefaultMaven.java:105)\n' +
                '    at org.apache.maven.cli.MavenCli.execute (MavenCli.java:956)\n' +
                '    at org.apache.maven.cli.MavenCli.doMain (MavenCli.java:288)\n' +
                '    at org.apache.maven.cli.MavenCli.main (MavenCli.java:192)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke (NativeMethodAccessorImpl.java:62)\n' +
                '    at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java:43)\n' +
                '    at java.lang.reflect.Method.invoke (Method.java:566)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (Launcher.java:282)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launch (Launcher.java:225)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (Launcher.java:406)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.main (Launcher.java:347)\n' +
                'Caused by: java.net.SocketException: Connection reset II\n' +
                '    at java.net.SocketInputStream.read (SocketInputStream.java:186)\n' +
                '    at java.net.SocketInputStream.read (SocketInputStream.java:140)\n' +
                '    at sun.security.ssl.SSLSocketInputRecord.read (SSLSocketInputRecord.java:448)\n' +
                '    at sun.security.ssl.SSLSocketInputRecord.decode (SSLSocketInputRecord.java:165)\n' +
                '    at sun.security.ssl.SSLTransport.decode (SSLTransport.java:108)\n' +
                '    at sun.security.ssl.SSLSocketImpl.decode (SSLSocketImpl.java:1151)\n' +
                '    at sun.security.ssl.SSLSocketImpl.readHandshakeRecord (SSLSocketImpl.java:1062)\n' +
                '    at sun.security.ssl.SSLSocketImpl.startHandshake (SSLSocketImpl.java:402)\n' +
                '    at sun.net.www.protocol.https.HttpsClient.afterConnect (HttpsClient.java:567)\n' +
                '    at sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect (AbstractDelegateHttpsURLConnection.java:185)\n' +
                '    at sun.net.www.protocol.https.HttpsURLConnectionImpl.connect (HttpsURLConnectionImpl.java:168)\n' +
                '    at io.swagger.v3.parser.util.RemoteUrl.urlToString (RemoteUrl.java:149)\n' +
                '    at io.swagger.v3.parser.util.RefUtils.readExternalRef (RefUtils.java:204)\n' +
                '    at io.swagger.v3.parser.ResolverCache.loadRef (ResolverCache.java:119)\n' +
                '    at io.swagger.v3.parser.processors.ExternalRefProcessor.processRefToExternalResponse (ExternalRefProcessor.java:334)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processReferenceResponse (ResponseProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.processors.ResponseProcessor.processResponse (ResponseProcessor.java:41)\n' +
                '    at io.swagger.v3.parser.processors.OperationProcessor.processOperation (OperationProcessor.java:56)\n' +
                '    at io.swagger.v3.parser.processors.PathsProcessor.processPaths (PathsProcessor.java:84)\n' +
                '    at io.swagger.v3.parser.OpenAPIResolver.resolve (OpenAPIResolver.java:49)\n' +
                '    at io.swagger.v3.parser.OpenAPIV3Parser.readLocation (OpenAPIV3Parser.java:67)\n' +
                '    at io.swagger.parser.OpenAPIParser.readLocation (OpenAPIParser.java:16)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toContext (CodegenConfigurator.java:461)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toClientOptInput (CodegenConfigurator.java:507)\n' +
                '    at org.openapitools.codegen.plugin.CodeGenMojo.execute (CodeGenMojo.java:724)\n' +
                '    at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo (DefaultBuildPluginManager.java:137)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:210)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:156)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:148)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:117)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:81)\n' +
                '    at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build (SingleThreadedBuilder.java:56)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleStarter.execute (LifecycleStarter.java:128)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:305)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:192)\n' +
                '    at org.apache.maven.DefaultMaven.execute (DefaultMaven.java:105)\n' +
                '    at org.apache.maven.cli.MavenCli.execute (MavenCli.java:956)\n' +
                '    at org.apache.maven.cli.MavenCli.doMain (MavenCli.java:288)\n' +
                '    at org.apache.maven.cli.MavenCli.main (MavenCli.java:192)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke (NativeMethodAccessorImpl.java:62)\n' +
                '    at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java:43)\n' +
                '    at java.lang.reflect.Method.invoke (Method.java:566)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (Launcher.java:282)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launch (Launcher.java:225)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (Launcher.java:406)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.main (Launcher.java:347)\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/400\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/401\n' +
                '[WARNING] Failed to get the schema name: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml#/components/responses/403\n' +
                'Errors: \n' +
                '\t-Unable to load URL ref: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml path: /home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/contract\n' +
                '\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toContext (CodegenConfigurator.java:480)\n' +
                '    at org.openapitools.codegen.config.CodegenConfigurator.toClientOptInput (CodegenConfigurator.java:507)\n' +
                '    at org.openapitools.codegen.plugin.CodeGenMojo.execute (CodeGenMojo.java:724)\n' +
                '    at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo (DefaultBuildPluginManager.java:137)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:210)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:156)\n' +
                '    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:148)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:117)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject (LifecycleModuleBuilder.java:81)\n' +
                '    at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build (SingleThreadedBuilder.java:56)\n' +
                '    at org.apache.maven.lifecycle.internal.LifecycleStarter.execute (LifecycleStarter.java:128)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:305)\n' +
                '    at org.apache.maven.DefaultMaven.doExecute (DefaultMaven.java:192)\n' +
                '    at org.apache.maven.DefaultMaven.execute (DefaultMaven.java:105)\n' +
                '    at org.apache.maven.cli.MavenCli.execute (MavenCli.java:956)\n' +
                '    at org.apache.maven.cli.MavenCli.doMain (MavenCli.java:288)\n' +
                '    at org.apache.maven.cli.MavenCli.main (MavenCli.java:192)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)\n' +
                '    at jdk.internal.reflect.NativeMethodAccessorImpl.invoke (NativeMethodAccessorImpl.java:62)\n' +
                '    at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke (DelegatingMethodAccessorImpl.java:43)\n' +
                '    at java.lang.reflect.Method.invoke (Method.java:566)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced (Launcher.java:282)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.launch (Launcher.java:225)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode (Launcher.java:406)\n' +
                '    at org.codehaus.plexus.classworlds.launcher.Launcher.main (Launcher.java:347)\n' +
                '[INFO] ------------------------------------------------------------------------\n' +
                '[INFO] BUILD FAILURE\n' +
                '[INFO] ------------------------------------------------------------------------\n' +
                '[INFO] Total time:  03:54 min\n' +
                '[INFO] Finished at: 2021-04-23T11:27:22Z\n' +
                '[INFO] ------------------------------------------------------------------------\n' +
                '[ERROR] Failed to execute goal org.openapitools:openapi-generator-maven-plugin:4.3.1:generate (default) on project arqpru-micro: Code generation failed. See above for the full exception. -> [Help 1]\n' +
                '[ERROR] \n' +
                '[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n' +
                '[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n' +
                '[ERROR] \n' +
                '[ERROR] For more information about the errors and possible solutions, please read the following articles:\n' +
                '[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException\n'

    String logMvnBuildSuccess =
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] BUILD SUCCESS\n' +
            '[INFO] ------------------------------------------------------------------------\n' +
            '[INFO] Total time:  01:40 min\n' +
            '[INFO] Finished at: 2019-10-31T12:05:11Z\n' +
            '[INFO] ------------------------------------------------------------------------\n'

    @Test
    void testGiven_BuildSuccess_returns_NoCauses() {
        def causes = new MavenGoalExecutionFailureDueToOpenApiGenerationLogParser().parseErrors(logMvnBuildSuccess)
        assert causes.size() == 0, 'We should find no errors'
    }

    @Test
    void testGiven_mvnDeploy_SSLExceptionDueToConnectionReset_Failure_returns_causes() {
        def causes = new MavenGoalExecutionFailureDueToOpenApiGenerationLogParser().parseErrors(logMvnOpenApiGenerationSSLExceptionWhenContactingContractServer)
        println "Cause(s):\n ${causes}"
        assert causes.size() == 2, 'We should find two errors'
        assert '[WARNING] Exception while reading:\n' +
            'java.lang.RuntimeException: Unable to load URL ref: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml path: /home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/contract\n' +
            'Caused by: javax.net.ssl.SSLException: Connection reset\n' +
            'Caused by: java.net.SocketException: Connection reset' == causes[0], 'We have not got the expected error message'
        assert '[WARNING] Exception while reading:\n' +
            'java.lang.RuntimeException: Unable to load URL ref: https://contractserver-micro-server-1.pro.int.srv.caixabank.com/resources/arch-global/v1/exceptions.yml path: /home/jenkins/workspace/absis3/services/apps/cbk/data-service/demo/arqpru-micro/contract\n' +
            'Caused by: javax.net.ssl.SSLException: Connection reset II\n' +
            'Caused by: java.net.SocketException: Connection reset II' == causes[1], 'We have not got the expected error message'
    }

}
