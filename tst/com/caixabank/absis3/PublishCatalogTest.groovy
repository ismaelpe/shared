package com.caixabank.absis3

class PublishCatalogTest extends GroovyTestCase {

    void testResponse() {
        def responseCatalog = "{\"resultado\":\"OK\",\"descripcion\":\"DTOAbsis3API [type=SRV.MS, aplicacion=demo-pipeline-micro, nombreComponente=demo-pipeline-micro, major=3, minor=40, fix=0, buildCode=0, typeVersion=RC, sourceCode=https://git.svb.lacaixa.es/cbk/absis3/services/apps/service/demo/demo-pipeline-micro.git, readme=https://git.svb.lacaixa.es/cbk/absis3/services/apps/service/demo/demo-pipeline-micro.git, javaDoc=http://demo-pipeline-micro-3.tst.int.srv.caixabank.com/javadoc, nexus=http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-releases/com/caixabank/absis/demo-pipeline-micro/3.40.0-RC1/demo-pipeline-micro-3.40.0-RC1.jar, restDocs=http://demo-pipeline-micro-3.tst.int.srv.caixabank.com/restdocs, versionLog=http://demo-pipeline-micro-3.tst.int.srv.caixabank.com/version-log, clienteJava=, swagger=http://demo-pipeline-micro-3.tst.int.srv.caixabank.com/swagger, faq=, tutoriales=, glosario=, bestPractices=, configuracion=, listDependencias=[DTODepAbsis3API [version=1.0.0-SNAPSHOT, application=absis-micro-common-spring-boot-starter, tipo=ARQ.LIB, componente=absis-micro-common-spring-boot-starter], DTODepAbsis3API [version=1.0.0-SNAPSHOT, application=feign-starter, tipo=ARQ.LIB, componente=feign-starter]], listEndPoints=[]]\"}"
        def json = new groovy.json.JsonSlurper().parseText(responseCatalog)

        def value = json.get("descripcion")
        println "El tipo de json generado " + json.getClass() + " El value es de " + value.getClass()


        def responseCatalog1 = "{\"resultado\":\"OK\",\"descripcion\":\"La descripcion es de \"}"
        def json1 = new groovy.json.JsonSlurper().parseText(responseCatalog1)
        def value1 = json1.get("descripcion")

        println "El tipo de json1 generado " + json1.getClass() + " El value es de " + value1.getClass()
    }
}
