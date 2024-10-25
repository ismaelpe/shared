package com.caixabank.absis3

import org.junit.Test


class MavenUtilsTest extends GroovyTestCase {

    static final String singleModuleLog = '[INFO] Scanning for projects...\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/mavenplugins/absis-dependencies-plugin/1.17.0-SNAPSHOT/maven-metadata.xml\n' +
        'Progress (1): 818 B\n' +
        '                   \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/mavenplugins/absis-endpoints-plugin/1.17.0-SNAPSHOT/maven-metadata.xml (815 B at 14 kB/s)\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/mavenplugins/codegen-postprocess-plugin/1.17.0-SNAPSHOT/maven-metadata.xml\n' +
        'Progress (1): 819 B\n' +
        '                   \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/mavenplugins/codegen-postprocess-plugin/1.17.0-SNAPSHOT/maven-metadata.xml (819 B at 13 kB/s)\n' +
        '[INFO] \n' +
        '[INFO] -------< com.caixabank.absis.apps.dataservice.demo:arqrun-micro >-------\n' +
        '[INFO] Building demo-arqrun-micro 2.9.0-SNAPSHOT\n' +
        '[INFO] --------------------------------[ jar ]---------------------------------\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/common/arch-common-lib/1.17.0-SNAPSHOT/maven-metadata.xml\n' +
        'Progress (1): 1.0 kB\n' +
        '                    \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/common/arch-common-lib/1.17.0-SNAPSHOT/maven-metadata.xml (1.0 kB at 12 kB/s)\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/absis-annotations-lib/1.17.0-SNAPSHOT/maven-metadata.xml\n' +
        'Progress (1): 1.0 kB\n' +
        '                    \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/jms/absis-jms-lib/1.17.0-SNAPSHOT/maven-metadata.xml (1.0 kB at 4.0 kB/s)\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/jms/absis-jms-lib/1.17.0-SNAPSHOT/absis-jms-lib-1.17.0-20210423.101258-84.pom\n' +
        'Progress (1): 970 B\n' +
        '                   \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/jms/absis-jms-lib/1.17.0-SNAPSHOT/absis-jms-lib-1.17.0-20210423.101258-84.pom (970 B at 14 kB/s)\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/common/absis-spring-cloud-stream-starter/1.17.0-SNAPSHOT/absis-spring-cloud-stream-starter-1.17.0-20210423.101411-84.jar\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-arch-compensation-common-lib/1.17.0-SNAPSHOT/absis-arch-compensation-common-lib-1.17.0-20210423.100919-84.jar\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-arch-compensation-starter/1.17.0-SNAPSHOT/absis-arch-compensation-starter-1.17.0-20210423.101216-84.jar\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/absis-dataservices-lib/1.17.0-SNAPSHOT/absis-dataservices-lib-1.17.0-20210423.101348-84.jar\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/common/absis-dataservice-spring-boot-starter/1.17.0-SNAPSHOT/absis-dataservice-spring-boot-starter-1.17.0-20210423.101357-84.jar\n' +
        'Progress (1): 4.1/524 kB\n' +
        'Progress (1): 8.2/524 kB\n' +
        'Progress (1): 11/524 kB \n' +
        'Progress (1): 15/524 kB\n' +
        'Progress (1): 19/524 kB\n' +
        'Progress (1): 23/524 kB\n' +
        'Progress (1): 27/524 kB\n' +
        'Progress (2): 27/524 kB | 3.9/462 kB\n' +
        'Progress (3): 27/524 kB | 3.9/462 kB | 3.9/508 kB\n' +
        'Progress (4): 27/524 kB | 3.9/462 kB | 3.9/508 kB | 3.9/413 kB\n' +
        'Progress (4): 30/524 kB | 3.9/462 kB | 3.9/508 kB | 3.9/413 kB\n' +
        'Progress (4): 33/524 kB | 3.9/462 kB | 3.9/508 kB | 3.9/413 kB\n' +
        'Progress (5): 33/524 kB | 3.9/462 kB | 3.9/508 kB | 3.9/413 kB | 3.9/739 kB\n' +

        'Progress (5): 524 kB | 462 kB | 508 kB | 413 kB | 425/739 kB\n' +
        'Progress (5): 524 kB | 462 kB | 508 kB | 413 kB | 427/739 kB\n' +
        'Progress (5): 524 kB | 462 kB | 508 kB | 413 kB | 431/739 kB\n' +
        '                                                            \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-arch-compensation-common-lib/1.17.0-SNAPSHOT/absis-arch-compensation-common-lib-1.17.0-20210423.100919-84.jar (462 kB at 492 kB/s)\n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/common/absis-dataservice-spring-boot-starter/1.17.0-SNAPSHOT/absis-dataservice-spring-boot-starter-1.17.0-20210423.101357-84.jar (508 kB at 540 kB/s)\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/dop-local-api/1.17.0-SNAPSHOT/dop-local-api-1.17.0-20210423.101113-84.jar\n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/common/absis-spring-cloud-stream-starter/1.17.0-SNAPSHOT/absis-spring-cloud-stream-starter-1.17.0-20210423.101411-84.jar (524 kB at 553 kB/s)\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-arch-compensation-api-lib/1.17.0-SNAPSHOT/absis-arch-compensation-api-lib-1.17.0-20210423.100926-85.jar\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/dop-local-jpa-impl/1.17.0-SNAPSHOT/dop-local-jpa-impl-1.17.0-20210423.101144-84.jar\n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-arch-compensation-starter/1.17.0-SNAPSHOT/absis-arch-compensation-starter-1.17.0-20210423.101216-84.jar (413 kB at 433 kB/s)\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-arch-compensation-impl-lib/1.17.0-SNAPSHOT/absis-arch-compensation-impl-lib-1.17.0-20210423.101125-84.jar\n' +
        'Progress (1): 433/739 kB\n' +
        'Progress (1): 438/739 kB\n' +
        'Progress (1): 442/739 kB\n' +
        'Progress (1): 446/739 kB\n' +
        'Progress (1): 450/739 kB\n' +
        'Progress (1): 452/739 kB\n' +
        'Progress (5): 476 kB | 434 kB | 459/503 kB | 503/507 kB | 16/501 kB\n' +
        'Progress (5): 476 kB | 434 kB | 459/503 kB | 507 kB | 16/501 kB    \n' +
        'Progress (5): 476 kB | 434 kB | 463/503 kB | 507 kB | 16/501 kB\n' +
        'Progress (5): 476 kB | 434 kB | 467/503 kB | 507 kB | 16/501 kB\n' +
        'Progress (5): 476 kB | 434 kB | 471/503 kB | 507 kB | 16/501 kB\n' +
        '                                                               \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/dop-local-jpa-impl/1.17.0-SNAPSHOT/dop-local-jpa-impl-1.17.0-20210423.101144-84.jar (476 kB at 294 kB/s)\n' +
        'Progress (4): 434 kB | 475/503 kB | 507 kB | 16/501 kB\n' +
        '                                                      \n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/servicemanager/service-manager-common-lib/1.17.0-SNAPSHOT/service-manager-common-lib-1.17.0-20210423.101018-84.jar\n' +
        'Progress (4): 434 kB | 479/503 kB | 507 kB | 16/501 kB\n' +
        'Progress (4): 434 kB | 483/503 kB | 507 kB | 16/501 kB\n' +
        'Progress (4): 434 kB | 487/503 kB | 507 kB | 16/501 kB\n' +
        'Progress (4): 434 kB | 503 kB | 507 kB | 72/501 kB\n' +
        '                                                  \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-arch-compensation-api-lib/1.17.0-SNAPSHOT/absis-arch-compensation-api-lib-1.17.0-20210423.100926-85.jar (434 kB at 264 kB/s)\n' +
        'Progress (3): 503 kB | 507 kB | 75/501 kB\n' +
        '                                         \n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-arch-compensation-method-impl-lib/1.17.0-SNAPSHOT/absis-arch-compensation-method-impl-lib-1.17.0-20210423.101234-83.jar\n' +
        'Progress (3): 503 kB | 507 kB | 79/501 kB\n' +
        'Progress (3): 503 kB | 507 kB | 81/501 kB\n' +
        'Progress (4): 503 kB | 507 kB | 81/501 kB | 0/1.4 MB\n' +
        '                                                    \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-arch-compensation-impl-lib/1.17.0-SNAPSHOT/absis-arch-compensation-impl-lib-1.17.0-20210423.101125-84.jar (507 kB at 307 kB/s)\n' +
        'Progress (3): 503 kB | 81/501 kB | 0/1.4 MB\n' +
        '                                           \n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-arch-compensation-method-api-lib/1.17.0-SNAPSHOT/absis-arch-compensation-method-api-lib-1.17.0-20210423.101228-83.jar\n' +
        'Progress (3): 503 kB | 81/501 kB | 0/1.4 MB\n' +
        'Progress (3): 503 kB | 85/501 kB | 0/1.4 MB\n' +
        'Progress (3): 503 kB | 88/501 kB | 0/1.4 MB\n' +
        'Progress (3): 503 kB | 92/501 kB | 0/1.4 MB\n' +
        'Progress (3): 503 kB | 96/501 kB | 0/1.4 MB\n' +
        'Progress (3): 503 kB | 99/501 kB | 0/1.4 MB\n' +
        'Progress (3): 503 kB | 101/501 kB | 0/1.4 MB\n' +
        '                                            \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/dop-local-api/1.17.0-SNAPSHOT/dop-local-api-1.17.0-20210423.101113-84.jar (503 kB at 299 kB/s)\n' +
        'Progress (2): 106/501 kB | 0/1.4 MB\n' +
        '                                   \n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/servicemanager/service-manager-spring-boot-starter/1.17.0-SNAPSHOT/service-manager-spring-boot-starter-1.17.0-20210423.101210-84.jar\n' +
        'Progress (2): 109/501 kB | 0/1.4 MB\n' +
        'Progress (2): 109/501 kB | 0/1.4 MB\n' +
        'Progress (2): 109/501 kB | 0/1.4 MB\n' +
        'Progress (2): 109/501 kB | 0/1.4 MB\n' +
        'Progress (5): 473/501 kB | 0.4/1.4 MB | 436 kB | 380/475 kB | 250/646 kB\n' +
        'Progress (5): 473/501 kB | 0.4/1.4 MB | 436 kB | 380/475 kB | 255/646 kB\n' +
        'Progress (5): 477/501 kB | 0.4/1.4 MB | 436 kB | 380/475 kB | 255/646 kB\n' +
        '                                                                        \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-arch-compensation-method-api-lib/1.17.0-SNAPSHOT/absis-arch-compensation-method-api-lib-1.17.0-20210423.101228-83.jar (436 kB at 202 kB/s)\n' +
        'Progress (4): 481/501 kB | 0.4/1.4 MB | 380/475 kB | 255/646 kB\n' +
        'Progress (4): 481/501 kB | 0.4/1.4 MB | 380/475 kB | 259/646 kB\n' +
        'Progress (4): 481/501 kB | 0.4/1.4 MB | 380/475 kB | 261/646 kB\n' +
        'Progress (4): 481/501 kB | 0.4/1.4 MB | 380/475 kB | 261/646 kB\n' +
        'Progress (4): 485/501 kB | 0.4/1.4 MB | 380/475 kB | 261/646 kB\n' +
        '                                                               \n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/common/absis-plugin-lib/1.17.0-SNAPSHOT/absis-plugin-lib-1.17.0-20210423.100949-85.jar\n' +
        'Progress (4): 485/501 kB | 0.4/1.4 MB | 380/475 kB | 264/646 kB\n' +
        'Progress (5): 501 kB | 0.5/1.4 MB | 475 kB | 293/646 kB | 25/535 kB\n' +
        'Progress (5): 501 kB | 0.5/1.4 MB | 475 kB | 293/646 kB | 29/535 kB\n' +
        'Progress (5): 501 kB | 0.5/1.4 MB | 475 kB | 293/646 kB | 33/535 kB\n' +
        'Progress (5): 501 kB | 0.5/1.4 MB | 475 kB | 293/646 kB | 37/535 kB\n' +
        '                                                                   \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-compensation-method-starter/1.17.0-SNAPSHOT/absis-compensation-method-starter-1.17.0-20210423.101250-84.jar (501 kB at 226 kB/s)\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-compensation-auto-producer-starter/1.17.0-SNAPSHOT/absis-compensation-auto-producer-starter-1.17.0-20210423.101323-84.jar\n' +
        'Progress (4): 0.5/1.4 MB | 475 kB | 295/646 kB | 37/535 kB\n' +
        'Progress (4): 0.5/1.4 MB | 475 kB | 298/646 kB | 37/535 kB\n' +
        '                                                          \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-arch-compensation-method-impl-lib/1.17.0-SNAPSHOT/absis-arch-compensation-method-impl-lib-1.17.0-20210423.101234-83.jar (475 kB at 213 kB/s)\n' +
        'Downloading from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/jms/absis-jms-producer-starter/1.17.0-SNAPSHOT/absis-jms-producer-starter-1.17.0-20210423.101317-84.jar\n' +
        'Progress (3): 0.5/1.4 MB | 302/646 kB | 37/535 kB\n' +
        'Progress (3): 1.4/1.4 MB | 461/467 kB | 474 kB\n' +
        'Progress (3): 1.4/1.4 MB | 464/467 kB | 474 kB\n' +
        'Progress (3): 1.4 MB | 464/467 kB | 474 kB    \n' +
        'Progress (3): 1.4 MB | 467 kB | 474 kB    \n' +
        '                                      \n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/jms/absis-jms-producer-starter/1.17.0-SNAPSHOT/absis-jms-producer-starter-1.17.0-20210423.101317-84.jar (474 kB at 149 kB/s)\n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/compensation/absis-compensation-auto-producer-starter/1.17.0-SNAPSHOT/absis-compensation-auto-producer-starter-1.17.0-20210423.101323-84.jar (467 kB at 145 kB/s)\n' +
        'Downloaded from nexus-pro-public-group: http://eibcmasp03.lacaixa.es:8081/nexus/repository/maven-public/com/caixabank/absis/arch/core/servicemanager/service-manager-common-lib/1.17.0-SNAPSHOT/service-manager-common-lib-1.17.0-20210423.101018-84.jar (1.4 MB at 441 kB/s)\n' +
        '[INFO] \n' +
        '[INFO] --- maven-dependency-plugin:2.8:resolve (default-cli) @ arqrun-micro ---\n' +
        '[INFO] \n' +
        '[INFO] The following files have been resolved:\n' +
        '[INFO]    org.jboss:jandex:jar:2.1.3.Final:compile\n' +
        '[INFO]    net.logstash.logback:logstash-logback-encoder:jar:5.3:compile\n' +
        '[INFO]    capital.scalable:spring-auto-restdocs-core:jar:2.0.3:test\n' +
        '[INFO]    org.skyscreamer:jsonassert:jar:1.5.0:test\n' +
        '[INFO]    io.rest-assured:rest-assured-common:jar:3.3.0:test\n' +
        '[INFO]    commons-configuration:commons-configuration:jar:1.8:runtime\n' +
        '[INFO]    org.apache.logging.log4j:log4j-api:jar:2.13.3:compile\n' +
        '[INFO]    javax.xml.bind:jaxb-api:jar:2.3.1:compile\n' +
        '[INFO]    com.caixabank.absis.arch.core.compensation:absis-arch-compensation-impl-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    io.micrometer:micrometer-core:jar:1.5.5:compile\n' +
        '[INFO]    com.caixabank.absis.arch.common:absis-security-common-starter:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    ch.qos.logback.contrib:logback-json-core:jar:0.1.5:compile\n' +
        '[INFO]    io.rest-assured:spring-mock-mvc:jar:3.3.0:test\n' +
        '[INFO]    org.junit.platform:junit-platform-surefire-provider:jar:1.3.2:test\n' +
        '[INFO]    org.opentest4j:opentest4j:jar:1.2.0:test\n' +
        '[INFO]    io.reactivex:rxjava:jar:1.3.8:compile\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-stream-binder-kafka:jar:3.0.8.RELEASE:compile\n' +
        '[INFO]    com.caixabank.absis.arch.core.compensation:absis-arch-compensation-starter:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    com.caixabank.absis.arch.core.compensation:absis-arch-compensation-method-api-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-starter-test:jar:2.3.4.RELEASE:test\n' +
        '[INFO]    io.netty:netty:jar:3.10.6.Final:compile\n' +
        '[INFO]    org.apache.commons:commons-compress:jar:1.19:compile\n' +
        '[INFO]    com.caixabank.absis.arch.common:absis-dataservice-spring-boot-starter:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    javax.cache:cache-api:jar:1.1.1:compile\n' +
        '[INFO]    com.caixabank.absis.ads.transaction:ads-ads0000b-lib:jar:0.12.0:compile\n' +
        '[INFO]    org.springframework.security:spring-security-crypto:jar:5.3.4.RELEASE:compile\n' +
        '[INFO]    javax.activation:javax.activation-api:jar:1.2.0:compile\n' +
        '[INFO]    bouncycastle:bcprov-jdk15:jar:140:compile\n' +
        '[INFO]    io.github.openfeign.form:feign-form:jar:3.8.0:compile\n' +
        '[INFO]    org.springframework:spring-context:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    io.confluent:common-config:jar:5.2.1:compile\n' +
        '[INFO]    com.caixabank.absis.arch.core.compensation:absis-compensation-method-starter:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    jakarta.xml.bind:jakarta.xml.bind-api:jar:2.3.3:compile\n' +
        '[INFO]    org.apiguardian:apiguardian-api:jar:1.0.0:test\n' +
        '[INFO]    io.github.openfeign:feign-hystrix:jar:10.5.1:compile\n' +
        '[INFO]    org.springframework.integration:spring-integration-core:jar:5.3.2.RELEASE:compile\n' +
        '[INFO]    com.fasterxml.jackson.core:jackson-annotations:jar:2.11.2:compile\n' +
        '[INFO]    net.bytebuddy:byte-buddy-agent:jar:1.10.14:test\n' +
        '[INFO]    com.caixabank.absis.arch.common:arch-common-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-starter-logging:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    org.hamcrest:hamcrest-library:jar:2.2:test\n' +
        '[INFO]    org.springframework:spring-aspects:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    org.apache.avro:avro-compiler:jar:1.9.1:compile\n' +
        '[INFO]    commons-lang:commons-lang:jar:2.6:compile\n' +
        '[INFO]    net.bytebuddy:byte-buddy:jar:1.10.14:compile\n' +
        '[INFO]    org.springframework:spring-core:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    org.mockito:mockito-core:jar:2.23.4:test\n' +
        '[INFO]    javax.activation:activation:jar:1.1.1:test\n' +
        '[INFO]    org.ehcache:ehcache:jar:3.8.1:compile\n' +
        '[INFO]    com.fasterxml.jackson.module:jackson-module-parameter-names:jar:2.11.2:compile\n' +
        '[INFO]    com.caixabank.absis.arch.backend.ads:adsconnector-common-lib:jar:1.8.0-RC3:compile\n' +
        '[INFO]    com.caixabank.absis.arch.common:feign-common-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    org.apache.tomcat.embed:tomcat-embed-websocket:jar:9.0.38:compile\n' +
        '[INFO]    com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:jar:2.10.4:compile\n' +
        '[INFO]    org.apache.zookeeper:zookeeper:jar:3.4.13:compile\n' +
        '[INFO]    org.hamcrest:hamcrest-core:jar:2.2:test\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-bus:jar:2.2.3.RELEASE:compile\n' +
        '[INFO]    org.jvnet.staxex:stax-ex:jar:1.8.1:compile\n' +
        '[INFO]    jakarta.validation:jakarta.validation-api:jar:2.0.2:compile\n' +
        '[INFO]    io.rest-assured:spring-commons:jar:3.3.0:test\n' +
        '[INFO]    org.springframework.security:spring-security-oauth2-jose:jar:5.3.4.RELEASE:compile\n' +
        '[INFO]    io.github.openfeign:feign-httpclient:jar:10.5.1:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-starter-aop:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    javax.persistence:javax.persistence-api:jar:2.2:compile\n' +
        '[INFO]    com.fasterxml.jackson.datatype:jackson-datatype-jsr310:jar:2.11.2:compile\n' +
        '[INFO]    io.swagger.core.v3:swagger-annotations:jar:2.1.2:compile\n' +
        '[INFO]    org.mapstruct:mapstruct:jar:1.4.1.Final:compile\n' +
        '[INFO]    io.prometheus:simpleclient_common:jar:0.8.1:compile\n' +
        '[INFO]    com.fasterxml.jackson.core:jackson-core:jar:2.10.4:compile\n' +
        '[INFO]    org.apache.logging.log4j:log4j-to-slf4j:jar:2.13.3:compile\n' +
        '[INFO]    org.springframework:spring-beans:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    org.springframework.security:spring-security-core:jar:5.3.4.RELEASE:compile\n' +
        '[INFO]    org.apache.httpcomponents:httpmime:jar:4.5.12:test\n' +
        '[INFO]    net.minidev:json-smart:jar:2.3:compile\n' +
        '[INFO]    ch.qos.logback.contrib:logback-jackson:jar:0.1.5:compile\n' +
        '[INFO]    org.hibernate.validator:hibernate-validator:jar:6.1.5.Final:compile\n' +
        '[INFO]    org.springframework:spring-expression:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-starter-cache:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    org.springframework:spring-jdbc:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-actuator-autoconfigure:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    org.rocksdb:rocksdbjni:jar:5.18.3:compile\n' +
        '[INFO]    org.reactivestreams:reactive-streams:jar:1.0.3:compile\n' +
        '[INFO]    org.springframework:spring-aop:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-function-context:jar:3.0.10.RELEASE:compile\n' +
        '[INFO]    javax.validation:validation-api:jar:2.0.1.Final:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-test:jar:2.3.4.RELEASE:test\n' +
        '[INFO]    antlr:antlr:jar:2.7.7:compile\n' +
        '[INFO]    io.confluent:kafka-streams-avro-serde:jar:5.2.1:compile\n' +
        '[INFO]    com.sun.istack:istack-commons-runtime:jar:3.0.8:compile\n' +
        '[INFO]    com.caixabank.absis.arch.core:absis-annotations-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    jakarta.activation:jakarta.activation-api:jar:1.2.2:compile\n' +
        '[INFO]    org.ow2.asm:asm:jar:5.0.4:compile\n' +
        '[INFO]    org.junit.jupiter:junit-jupiter:jar:5.6.2:test\n' +
        '[INFO]    com.github.tomakehurst:wiremock-standalone:jar:2.25.1:compile\n' +
        '[INFO]    org.springframework:spring-jcl:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    org.springframework.plugin:spring-plugin-core:jar:2.0.0.RELEASE:compile\n' +
        '[INFO]    org.springdoc:springdoc-openapi-webmvc-core:jar:1.4.0:compile\n' +
        '[INFO]    io.github.openfeign:feign-jackson:jar:10.5.1:compile\n' +
        '[INFO]    org.dom4j:dom4j:jar:2.1.3:compile\n' +
        '[INFO]    com.google.guava:guava:jar:23.6.1-jre:compile\n' +
        '[INFO]    jakarta.xml.soap:jakarta.xml.soap-api:jar:1.4.2:compile\n' +
        '[INFO]    javax.jms:javax.jms-api:jar:2.0.1:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-starter-data-jpa:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    org.slf4j:slf4j-api:jar:1.7.30:compile\n' +
        '[INFO]    org.messaginghub:pooled-jms:jar:1.1.2:compile\n' +
        '[INFO]    com.caixabank.absis.arch.common:absis-plugin-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    org.glassfish.jaxb:jaxb-runtime:jar:2.3.2:compile\n' +
        '[INFO]    org.assertj:assertj-core:jar:3.16.1:test\n' +
        '[INFO]    joda-time:joda-time:jar:2.10.1:compile\n' +
        '[INFO]    com.github.luben:zstd-jni:jar:1.4.4-7:compile\n' +
        '[INFO]    com.github.stephenc.jcip:jcip-annotations:jar:1.0-1:compile\n' +
        '[INFO]    com.caixabank.absis.arch.core:absis-dataservices-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    com.caixabank.absis.arch.common:absis-micro-common-spring-boot-starter:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    org.mockito:mockito-junit-jupiter:jar:2.23.4:test\n' +
        '[INFO]    org.apache.commons:commons-lang3:jar:3.9:compile\n' +
        '[INFO]    org.springframework:spring-context-support:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    commons-codec:commons-codec:jar:1.14:compile\n' +
        '[INFO]    org.apache.kafka:connect-api:jar:2.5.1:compile\n' +
        '[INFO]    com.fasterxml.jackson.datatype:jackson-datatype-jdk8:jar:2.11.2:compile\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-stream-binder-kafka-core:jar:3.0.8.RELEASE:compile\n' +
        '[INFO]    org.apache.tomcat.embed:tomcat-embed-core:jar:9.0.38:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-configuration-processor:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    com.zaxxer:HikariCP:jar:3.4.5:compile\n' +
        '[INFO]    org.springdoc:springdoc-openapi-ui:jar:1.4.0:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-starter-web:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    org.ccil.cowan.tagsoup:tagsoup:jar:1.2.1:test\n' +
        '[INFO]    org.bouncycastle:bcpkix-jdk15on:jar:1.59:compile\n' +
        '[INFO]    org.springdoc:springdoc-openapi-common:jar:1.4.0:compile\n' +
        '[INFO]    com.ibm.mq:mq-jms-spring-boot-starter:jar:2.3.5:compile\n' +
        '[INFO]    org.apache.geronimo.specs:geronimo-jms_2.0_spec:jar:1.0-alpha-2:compile\n' +
        '[INFO]    org.springframework:spring-jms:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-starter-tomcat:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    org.webjars:swagger-ui:jar:3.25.4:compile\n' +
        '[INFO]    com.101tec:zkclient:jar:0.10:compile\n' +
        '[INFO]    org.springframework.hateoas:spring-hateoas:jar:1.1.2.RELEASE:compile\n' +
        '[INFO]    io.github.classgraph:classgraph:jar:4.8.69:compile\n' +
        '[INFO]    org.hdrhistogram:HdrHistogram:jar:2.1.12:compile\n' +
        '[INFO]    io.rest-assured:json-path:jar:3.3.0:test\n' +
        '[INFO]    org.springframework.data:spring-data-jpa:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    org.javassist:javassist:jar:3.24.0-GA:compile\n' +
        '[INFO]    org.junit.platform:junit-platform-engine:jar:1.5.2:test\n' +
        '[INFO]    org.apache.kafka:kafka-streams:jar:2.5.1:compile\n' +
        '[INFO]    org.apache.sling:org.apache.sling.javax.activation:jar:0.1.0:test\n' +
        '[INFO]    org.springframework.boot:spring-boot-starter-jdbc:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-test-autoconfigure:jar:2.3.4.RELEASE:test\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-context:jar:2.2.5.RELEASE:compile\n' +
        '[INFO]    org.apache.kafka:connect-json:jar:2.5.1:compile\n' +
        '[INFO]    org.springframework.security:spring-security-rsa:jar:1.0.9.RELEASE:compile\n' +
        '[INFO]    org.jvnet.mimepull:mimepull:jar:1.9.13:compile\n' +
        '[INFO]    com.caixabank.absis.arch.core.compensation:dop-local-api:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    org.apache.kafka:kafka-clients:jar:2.5.1:compile\n' +
        '[INFO]    org.springframework.data:spring-data-commons:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    com.caixabank.absis.arch.jms:absis-jms-producer-starter:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    org.jboss.spec.javax.transaction:jboss-transaction-api_1.2_spec:jar:1.1.1.Final:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-autoconfigure:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    jakarta.persistence:jakarta.persistence-api:jar:2.2.3:compile\n' +
        '[INFO]    io.github.openfeign:feign-core:jar:10.5.1:compile\n' +
        '[INFO]    org.junit.platform:junit-platform-commons:jar:1.5.2:test\n' +
        '[INFO]    com.caixabank.absis.arch.common:absis-spring-cache-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    org.springframework.kafka:spring-kafka:jar:2.5.7.RELEASE:compile\n' +
        '[INFO]    com.caixabank.absis.arch.common:absis-context-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    org.hibernate.common:hibernate-commons-annotations:jar:5.1.0.Final:compile\n' +
        '[INFO]    javax.annotation:javax.annotation-api:jar:1.3.2:compile\n' +
        '[INFO]    ch.qos.logback:logback-classic:jar:1.2.3:compile\n' +
        '[INFO]    com.caixabank.absis.apps.dataservice.demo.contract:arqpru-micro:jar:1.0.0:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-devtools:jar:2.3.4.RELEASE:runtime\n' +
        '[INFO]    com.caixabank.absis.arch.jms:absis-jms-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    io.github.openfeign.form:feign-form-spring:jar:3.8.0:compile\n' +
        '[INFO]    org.hibernate:hibernate-core:jar:5.4.21.Final:compile\n' +
        '[INFO]    org.codehaus.groovy:groovy-xml:jar:2.5.13:test\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-config-client:jar:2.2.5.RELEASE:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    io.github.openfeign:feign-slf4j:jar:10.5.1:compile\n' +
        '[INFO]    org.apache.httpcomponents:httpcore:jar:4.4.13:compile\n' +
        '[INFO]    com.nimbusds:nimbus-jose-jwt:jar:8.19:compile\n' +
        '[INFO]    org.slf4j:jul-to-slf4j:jar:1.7.30:compile\n' +
        '[INFO]    org.yaml:snakeyaml:jar:1.26:compile\n' +
        '[INFO]    org.xmlunit:xmlunit-core:jar:2.7.0:test\n' +
        '[INFO]    com.google.code.findbugs:jsr305:jar:1.3.9:compile\n' +
        '[INFO]    jline:jline:jar:0.9.94:compile\n' +
        '[INFO]    com.fasterxml.jackson.core:jackson-databind:jar:2.11.2:compile\n' +
        '[INFO]    org.lz4:lz4-java:jar:1.7.1:compile\n' +
        '[INFO]    org.springframework.retry:spring-retry:jar:1.2.5.RELEASE:compile\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-commons:jar:2.2.5.RELEASE:compile\n' +
        '[INFO]    com.ibm.mq:com.ibm.mq.allclient:jar:9.1.0.0:compile\n' +
        '[INFO]    org.apache.commons:commons-text:jar:1.8:compile\n' +
        '[INFO]    org.springframework.restdocs:spring-restdocs-core:jar:2.0.5.RELEASE:test\n' +
        '[INFO]    org.webjars:webjars-locator-core:jar:0.45:compile\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-starter:jar:2.2.5.RELEASE:compile\n' +
        '[INFO]    org.codehaus.groovy:groovy:jar:2.5.13:test\n' +
        '[INFO]    ch.qos.logback:logback-core:jar:1.2.3:compile\n' +
        '[INFO]    com.caixabank.absis.arch.core.servicemanager:service-manager-common-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    io.micrometer:micrometer-registry-prometheus:jar:1.5.5:compile\n' +
        '[INFO]    com.caixabank.absis.arch.common:absis-security-common-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    org.apache.avro:avro:jar:1.9.1:compile\n' +
        '[INFO]    com.caixabank.absis.arch.common:feign-starter:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    org.junit.jupiter:junit-jupiter-params:jar:5.5.2:test\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-starter-openfeign:jar:2.2.5.RELEASE:compile\n' +
        '[INFO]    org.hamcrest:hamcrest:jar:2.2:test\n' +
        '[INFO]    com.caixabank.absis.arch.common:absis-micro-common-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    io.rest-assured:rest-assured:jar:3.3.0:test\n' +
        '[INFO]    com.caixabank.absis.arch.core.compensation:absis-compensation-auto-producer-starter:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    com.sun.xml.fastinfoset:FastInfoset:jar:1.2.16:compile\n' +
        '[INFO]    com.jayway.jsonpath:json-path:jar:2.4.0:compile\n' +
        '[INFO]    org.junit.jupiter:junit-jupiter-api:jar:5.5.2:test\n' +
        '[INFO]    com.caixabank.absis.ads.transaction:ads-ads0000a-lib:jar:0.20.0:compile\n' +
        '[INFO]    com.netflix.hystrix:hystrix-core:jar:1.5.18:compile\n' +
        '[INFO]    org.objenesis:objenesis:jar:3.0.1:test\n' +
        '[INFO]    io.projectreactor:reactor-core:jar:3.3.10.RELEASE:compile\n' +
        '[INFO]    org.springframework:spring-test:jar:5.2.9.RELEASE:test\n' +
        '[INFO]    org.springframework.boot:spring-boot-starter-actuator:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    net.jodah:typetools:jar:0.6.2:compile\n' +
        '[INFO]    com.oracle:ojdbc6:jar:11.2.0:runtime\n' +
        '[INFO]    org.springframework:spring-orm:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    org.apache.maven.surefire:common-java5:jar:2.22.0:test\n' +
        '[INFO]    org.springframework.integration:spring-integration-kafka:jar:3.2.1.RELEASE:compile\n' +
        '[INFO]    com.caixabank.absis.arch.common:absis-spring-cloud-stream-starter:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    io.confluent:common-utils:jar:5.2.1:compile\n' +
        '[INFO]    com.fasterxml:classmate:jar:1.5.1:compile\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-openfeign-core:jar:2.2.5.RELEASE:compile\n' +
        '[INFO]    org.junit.platform:junit-platform-launcher:jar:1.5.2:test\n' +
        '[INFO]    com.caixabank.absis.arch.core.compensation:absis-arch-compensation-common-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    org.glassfish:jakarta.el:jar:3.0.3:compile\n' +
        '[INFO]    io.prometheus:simpleclient:jar:0.8.1:compile\n' +
        '[INFO]    com.caixabank.absis.arch.common:absis-spring-cache-starter:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    ch.qos.logback.contrib:logback-json-classic:jar:0.1.5:compile\n' +
        '[INFO]    org.springframework:spring-messaging:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    com.caixabank.absis.arch.backend.ads:adsconnector-lib-starter:jar:1.8.0-RC3:compile\n' +
        '[INFO]    javax.servlet:javax.servlet-api:jar:4.0.1:provided\n' +
        '[INFO]    org.aspectj:aspectjweaver:jar:1.9.6:compile\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-stream-test-support:jar:3.0.8.RELEASE:test\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-netflix-archaius:jar:2.2.5.RELEASE:compile\n' +
        '[INFO]    org.codehaus.mojo:animal-sniffer-annotations:jar:1.14:compile\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-starter-stream-kafka:jar:3.0.8.RELEASE:compile\n' +
        '[INFO]    org.springframework.restdocs:spring-restdocs-mockmvc:jar:2.0.5.RELEASE:test\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-function-core:jar:3.0.10.RELEASE:compile\n' +
        '[INFO]    io.confluent:kafka-schema-registry-client:jar:5.2.1:compile\n' +
        '[INFO]    org.springframework.integration:spring-integration-jmx:jar:5.3.2.RELEASE:compile\n' +
        '[INFO]    net.minidev:accessors-smart:jar:1.2:compile\n' +
        '[INFO]    org.xerial.snappy:snappy-java:jar:1.1.7.3:compile\n' +
        '[INFO]    jakarta.annotation:jakarta.annotation-api:jar:1.3.5:compile\n' +
        '[INFO]    io.swagger.core.v3:swagger-models:jar:2.1.2:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-starter-json:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-starter-bus-kafka:jar:2.2.3.RELEASE:compile\n' +
        '[INFO]    com.sun.xml.messaging.saaj:saaj-impl:jar:1.5.1:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-starter-validation:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    org.apache.yetus:audience-annotations:jar:0.5.0:compile\n' +
        '[INFO]    org.apache.httpcomponents:httpclient:jar:4.5.12:compile\n' +
        '[INFO]    com.github.ben-manes.caffeine:caffeine:jar:2.6.2:compile\n' +
        '[INFO]    com.caixabank.absis.arch.core.servicemanager:service-manager-spring-boot-starter:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    com.sun.xml.bind:jaxb-osgi:jar:2.2.10:test\n' +
        '[INFO]    org.apache.commons:commons-pool2:jar:2.8.1:compile\n' +
        '[INFO]    com.caixabank.absis.arch.core.compensation:dop-local-jpa-impl:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    io.confluent:kafka-avro-serializer:jar:5.2.1:compile\n' +
        '[INFO]    io.swagger.core.v3:swagger-integration:jar:2.1.2:compile\n' +
        '[INFO]    org.codehaus.groovy:groovy-json:jar:2.5.13:test\n' +
        '[INFO]    io.rest-assured:xml-path:jar:3.3.0:test\n' +
        '[INFO]    com.caixabank.absis.arch.core:absis-header-interceptor-starter:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    org.glassfish.jaxb:txw2:jar:2.3.3:compile\n' +
        '[INFO]    org.apache.velocity:velocity-engine-core:jar:2.0:compile\n' +
        '[INFO]    commons-io:commons-io:jar:2.5:compile\n' +
        '[INFO]    org.apache.maven.surefire:surefire-logger-api:jar:2.22.0:test\n' +
        '[INFO]    com.netflix.archaius:archaius-core:jar:0.7.6:compile\n' +
        '[INFO]    jakarta.transaction:jakarta.transaction-api:jar:1.3.3:compile\n' +
        '[INFO]    org.springframework:spring-webmvc:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    org.checkerframework:checker-compat-qual:jar:2.0.0:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-starter-hateoas:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    org.jboss.logging:jboss-logging:jar:3.4.1.Final:compile\n' +
        '[INFO]    org.springframework:spring-tx:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-stream:jar:3.0.8.RELEASE:compile\n' +
        '[INFO]    com.google.j2objc:j2objc-annotations:jar:1.1:compile\n' +
        '[INFO]    org.hibernate:hibernate-jcache:jar:5.4.21.Final:compile\n' +
        '[INFO]    com.caixabank.absis.arch.core.compensation:absis-arch-compensation-method-impl-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO]    io.swagger.core.v3:swagger-core:jar:2.1.2:compile\n' +
        '[INFO]    org.bouncycastle:bcprov-jdk15on:jar:1.59:compile\n' +
        '[INFO]    commons-fileupload:commons-fileupload:jar:1.4:compile\n' +
        '[INFO]    com.google.errorprone:error_prone_annotations:jar:2.1.3:compile\n' +
        '[INFO]    com.h2database:h2:jar:1.4.200:runtime\n' +
        '[INFO]    com.caixabank.absis.arch.common:absis-test-lib:jar:1.17.0-SNAPSHOT:test\n' +
        '[INFO]    org.junit.jupiter:junit-jupiter-engine:jar:5.5.2:test\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-netflix-ribbon:jar:2.2.5.RELEASE:compile\n' +
        '[INFO]    org.springframework:spring-web:jar:5.2.9.RELEASE:compile\n' +
        '[INFO]    org.springframework.cloud:spring-cloud-stream-binder-kafka-streams:jar:3.0.8.RELEASE:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-actuator:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    org.springframework.boot:spring-boot-starter:jar:2.3.4.RELEASE:compile\n' +
        '[INFO]    org.latencyutils:LatencyUtils:jar:2.0.3:runtime\n' +
        '[INFO]    org.springframework.security:spring-security-oauth2-core:jar:5.3.4.RELEASE:compile\n' +
        '[INFO]    com.vaadin.external.google:android-json:jar:0.0.20131108.vaadin1:test\n' +
        '[INFO]    org.apache.maven.surefire:surefire-api:jar:2.22.0:test\n' +
        '[INFO]    com.caixabank.absis.arch.core.compensation:absis-arch-compensation-api-lib:jar:1.17.0-SNAPSHOT:compile\n' +
        '[INFO] \n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] BUILD SUCCESS\n' +
        '[INFO] ------------------------------------------------------------------------\n' +
        '[INFO] Total time:  20.384 s\n' +
        '[INFO] Finished at: 2021-04-28T13:54:21+02:00\n' +
        '[INFO] ------------------------------------------------------------------------\n'

    static final String multipleMavenLog =
            '[INFO] ------< com.caixabank.absis.arch.audit.dec:dec-plugin-common-lib >------\n' +
            '[INFO] Building dec-plugin-common-lib 2.10.0-SNAPSHOT                     [1/6]\n' +
            '[INFO] --------------------------------[ jar ]---------------------------------\n' +
            '[WARNING] The POM for com.sun.xml.bind:jaxb-osgi:jar:2.2.10 is invalid, transitive dependencies (if any) will not be available, enable debug logging for more details\n' +
            '[INFO]\n' +
            '[INFO] --- maven-dependency-plugin:2.8:resolve (default-cli) @ dec-plugin-common-lib ---\n' +
            '[INFO]\n' +
            '[INFO] The following files have been resolved:\n' +
            '[INFO]    org.jvnet.mimepull:mimepull:jar:1.9.13:compile\n' +
            '[INFO]    net.logstash.logback:logstash-logback-encoder:jar:5.3:compile\n' +
            '[INFO]    junit:junit:jar:4.13:compile\n' +
            '[INFO]    io.rest-assured:rest-assured-common:jar:3.3.0:test\n' +
            '[INFO]    com.caixabank.absis.arch.audit.dec:dec-api-lib:jar:2.10.0-RC1:compile\n' +
            '[INFO]    javax.xml.bind:jaxb-api:jar:2.3.1:compile\n' +
            '[INFO]    commons-beanutils:commons-beanutils:jar:1.9.3:compile\n' +
            '[INFO]    io.micrometer:micrometer-core:jar:1.5.5:compile\n' +
            '[INFO]    org.aspectj:aspectjrt:jar:1.9.6:compile\n' +
            '[INFO]    ch.qos.logback.contrib:logback-json-core:jar:0.1.5:compile\n' +
            '[INFO]    org.junit.platform:junit-platform-surefire-provider:jar:1.3.2:test\n' +
            '[INFO]    io.rest-assured:spring-mock-mvc:jar:3.3.0:test\n' +
            '[INFO]    org.springframework.boot:spring-boot-autoconfigure:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    io.github.openfeign:feign-core:jar:10.5.1:compile\n' +
            '[INFO]    org.opentest4j:opentest4j:jar:1.2.0:test\n' +
            '[INFO]    org.junit.platform:junit-platform-commons:jar:1.5.2:test\n' +
            '[INFO]    com.caixabank.absis.arch.common:arch-common-lib:jar:1.17.0-RC5:compile\n' +
            '[INFO]    com.googlecode.json-simple:json-simple:jar:1.1.1:compile\n' +
            '[INFO]    ch.qos.logback:logback-classic:jar:1.2.3:compile\n' +
            '[INFO]    org.apache.commons:commons-compress:jar:1.19:compile\n' +
            '[INFO]    javax.activation:javax.activation-api:jar:1.2.0:compile\n' +
            '[INFO]    commons-logging:commons-logging:jar:1.2:compile\n' +
            '[INFO]    bouncycastle:bcprov-jdk15:jar:140:compile\n' +
            '[INFO]    org.springframework:spring-context:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    org.codehaus.groovy:groovy-xml:jar:2.5.13:test\n' +
            '[INFO]    jakarta.xml.bind:jakarta.xml.bind-api:jar:2.3.3:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    io.github.openfeign:feign-slf4j:jar:10.5.1:compile\n' +
            '[INFO]    com.caixabank.absis.arch.audit.dec:dec-common-lib:jar:2.10.0-RC1:compile\n' +
            '[INFO]    org.apache.httpcomponents:httpcore:jar:4.4.13:compile\n' +
            '[INFO]    org.apiguardian:apiguardian-api:jar:1.0.0:test\n' +
            '[INFO]    com.fasterxml.jackson.core:jackson-annotations:jar:2.11.2:compile\n' +
            '[INFO]    net.bytebuddy:byte-buddy-agent:jar:1.10.14:test\n' +
            '[INFO]    com.fasterxml.jackson.core:jackson-databind:jar:2.11.2:compile\n' +
            '[INFO]    org.hamcrest:hamcrest-library:jar:2.2:test\n' +
            '[INFO]    com.google.code.gson:gson:jar:2.8.6:compile\n' +
            '[INFO]    com.ibm.mq:com.ibm.mq.allclient:jar:9.1.0.0:compile\n' +
            '[INFO]    commons-lang:commons-lang:jar:2.6:compile\n' +
            '[INFO]    org.apache.commons:commons-text:jar:1.8:compile\n' +
            '[INFO]    net.bytebuddy:byte-buddy:jar:1.10.14:test\n' +
            '[INFO]    org.codehaus.groovy:groovy:jar:2.5.13:test\n' +
            '[INFO]    org.springframework:spring-core:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    xml-apis:xml-apis:jar:1.3.03:compile\n' +
            '[INFO]    ch.qos.logback:logback-core:jar:1.2.3:compile\n' +
            '[INFO]    org.mockito:mockito-core:jar:2.23.4:test\n' +
            '[INFO]    javax.activation:activation:jar:1.1.1:test\n' +
            '[INFO]    com.caixabank.absis.arch.core:absis-annotations-lib:jar:1.17.0-RC5:compile\n' +
            '[INFO]    com.caixabank.absis.arch.common:absis-context-lib:jar:1.17.0-RC5:compile\n' +
            '[INFO]    org.apache.avro:avro:jar:1.9.1:compile\n' +
            '[INFO]    org.junit.jupiter:junit-jupiter-params:jar:5.5.2:test\n' +
            '[INFO]    org.hamcrest:hamcrest:jar:2.2:compile\n' +
            '[INFO]    com.sun.xml.fastinfoset:FastInfoset:jar:1.2.16:compile\n' +
            '[INFO]    io.rest-assured:rest-assured:jar:3.3.0:test\n' +
            '[INFO]    org.junit.jupiter:junit-jupiter-api:jar:5.5.2:test\n' +
            '[INFO]    org.hamcrest:hamcrest-core:jar:2.2:compile\n' +
            '[INFO]    org.objenesis:objenesis:jar:3.0.1:test\n' +
            '[INFO]    org.springframework:spring-test:jar:5.2.9.RELEASE:test\n' +
            '[INFO]    org.jvnet.staxex:stax-ex:jar:1.8.1:compile\n' +
            '[INFO]    io.rest-assured:spring-commons:jar:3.3.0:test\n' +
            '[INFO]    org.apache.maven.surefire:common-java5:jar:2.22.0:test\n' +
            '[INFO]    io.github.openfeign:feign-httpclient:jar:10.5.1:compile\n' +
            '[INFO]    com.fasterxml.jackson.datatype:jackson-datatype-jsr310:jar:2.11.2:compile\n' +
            '[INFO]    org.mapstruct:mapstruct:jar:1.4.1.Final:compile\n' +
            '[INFO]    org.junit.platform:junit-platform-launcher:jar:1.5.2:test\n' +
            '[INFO]    com.fasterxml.jackson.core:jackson-core:jar:2.10.4:compile\n' +
            '[INFO]    org.springframework:spring-beans:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    ch.qos.logback.contrib:logback-json-classic:jar:0.1.5:compile\n' +
            '[INFO]    org.springframework:spring-messaging:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    org.apache.httpcomponents:httpmime:jar:4.5.12:test\n' +
            '[INFO]    ch.qos.logback.contrib:logback-jackson:jar:0.1.5:compile\n' +
            '[INFO]    org.springframework:spring-expression:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    org.springframework:spring-aop:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    javax.validation:validation-api:jar:2.0.1.Final:compile\n' +
            '[INFO]    jakarta.annotation:jakarta.annotation-api:jar:1.3.5:compile\n' +
            '[INFO]    org.aspectj:aspectjtools:jar:1.9.6:compile\n' +
            '[INFO]    com.sun.istack:istack-commons-runtime:jar:3.0.8:compile\n' +
            '[INFO]    jakarta.activation:jakarta.activation-api:jar:1.2.2:compile\n' +
            '[INFO]    com.sun.xml.messaging.saaj:saaj-impl:jar:1.5.1:compile\n' +
            '[INFO]    org.apache.httpcomponents:httpclient:jar:4.5.12:compile\n' +
            '[INFO]    org.springframework:spring-jcl:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    com.sun.xml.bind:jaxb-osgi:jar:2.2.10:test\n' +
            '[INFO]    jakarta.xml.soap:jakarta.xml.soap-api:jar:1.4.2:compile\n' +
            '[INFO]    javax.jms:javax.jms-api:jar:2.0.1:compile\n' +
            '[INFO]    org.slf4j:slf4j-api:jar:1.7.30:compile\n' +
            '[INFO]    com.caixabank.absis.arch.lib.cbk:absisclo-lib:jar:1.2.0:compile\n' +
            '[INFO]    org.glassfish.jaxb:jaxb-runtime:jar:2.3.2:compile\n' +
            '[INFO]    org.assertj:assertj-core:jar:3.16.1:test\n' +
            '[INFO]    org.codehaus.groovy:groovy-json:jar:2.5.13:test\n' +
            '[INFO]    io.rest-assured:xml-path:jar:3.3.0:test\n' +
            '[INFO]    org.glassfish.jaxb:txw2:jar:2.3.3:compile\n' +
            '[INFO]    commons-io:commons-io:jar:2.5:compile\n' +
            '[INFO]    org.apache.maven.surefire:surefire-logger-api:jar:2.22.0:test\n' +
            '[INFO]    org.mockito:mockito-junit-jupiter:jar:2.23.4:test\n' +
            '[INFO]    commons-digester:commons-digester:jar:1.8:compile\n' +
            '[INFO]    org.springframework:spring-webmvc:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    org.apache.commons:commons-lang3:jar:3.9:compile\n' +
            '[INFO]    xerces:xercesImpl:jar:2.8.0:compile\n' +
            '[INFO]    commons-codec:commons-codec:jar:1.14:compile\n' +
            '[INFO]    org.springframework:spring-tx:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    org.bouncycastle:bcprov-jdk15on:jar:1.59:compile\n' +
            '[INFO]    org.ccil.cowan.tagsoup:tagsoup:jar:1.2.1:test\n' +
            '[INFO]    org.bouncycastle:bcpkix-jdk15on:jar:1.59:compile\n' +
            '[INFO]    org.junit.jupiter:junit-jupiter-engine:jar:5.5.2:test\n' +
            '[INFO]    org.springframework:spring-web:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    org.springframework:spring-jms:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    commons-collections:commons-collections:jar:3.2.2:compile\n' +
            '[INFO]    org.latencyutils:LatencyUtils:jar:2.0.3:runtime\n' +
            '[INFO]    org.hdrhistogram:HdrHistogram:jar:2.1.12:compile\n' +
            '[INFO]    io.rest-assured:json-path:jar:3.3.0:test\n' +
            '[INFO]    org.junit.platform:junit-platform-engine:jar:1.5.2:test\n' +
            '[INFO]    org.apache.maven.surefire:surefire-api:jar:2.22.0:test\n' +
            '[INFO]    org.apache.sling:org.apache.sling.javax.activation:jar:0.1.0:test\n' +
            '[INFO]\n' +
            '[INFO]\n' +
            '[INFO] --< com.caixabank.absis.arch.audit.dec:dec-plugin-spring-boot-starter >--\n' +
            '[INFO] Building dec-plugin-spring-boot-starter 2.10.0-SNAPSHOT            [2/6]\n' +
            '[INFO] --------------------------------[ jar ]---------------------------------\n' +
            '[INFO]\n' +
            '[INFO] --- maven-dependency-plugin:2.8:resolve (default-cli) @ dec-plugin-spring-boot-starter ---\n' +
            '[INFO]\n' +
            '[INFO] The following files have been resolved:\n' +
            '[INFO]    net.logstash.logback:logstash-logback-encoder:jar:5.3:compile\n' +
            '[INFO]    junit:junit:jar:4.13:compile\n' +
            '[INFO]    org.skyscreamer:jsonassert:jar:1.5.0:test\n' +
            '[INFO]    commons-configuration:commons-configuration:jar:1.8:runtime\n' +
            '[INFO]    io.rest-assured:rest-assured-common:jar:3.3.0:test\n' +
            '[INFO]    com.caixabank.absis.arch.audit.dec:dec-api-lib:jar:2.10.0-RC1:compile\n' +
            '[INFO]    org.apache.logging.log4j:log4j-api:jar:2.13.3:compile\n' +
            '[INFO]    javax.xml.bind:jaxb-api:jar:2.3.1:compile\n' +
            '[INFO]    com.caixabank.absis.arch.common:absis-micro-common-spring-boot-starter:jar:1.17.0-RC5:compile\n' +
            '[INFO]    commons-beanutils:commons-beanutils:jar:1.9.3:compile\n' +
            '[INFO]    io.micrometer:micrometer-core:jar:1.5.5:compile\n' +
            '[INFO]    org.aspectj:aspectjrt:jar:1.9.6:compile\n' +
            '[INFO]    ch.qos.logback.contrib:logback-json-core:jar:0.1.5:compile\n' +
            '[INFO]    org.junit.platform:junit-platform-surefire-provider:jar:1.3.2:test\n' +
            '[INFO]    io.rest-assured:spring-mock-mvc:jar:3.3.0:test\n' +
            '[INFO]    com.caixabank.absis.arch.common:feign-common-lib:jar:1.17.0-RC5:compile\n' +
            '[INFO]    org.opentest4j:opentest4j:jar:1.2.0:test\n' +
            '[INFO]    io.reactivex:rxjava:jar:1.3.8:compile\n' +
            '[INFO]    com.caixabank.absis.arch.common:arch-common-lib:jar:1.17.0-RC5:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-starter-test:jar:2.3.4.RELEASE:test\n' +
            '[INFO]    org.apache.commons:commons-compress:jar:1.19:compile\n' +
            '[INFO]    javax.cache:cache-api:jar:1.1.1:compile\n' +
            '[INFO]    org.springframework.security:spring-security-crypto:jar:5.3.4.RELEASE:compile\n' +
            '[INFO]    javax.activation:javax.activation-api:jar:1.2.0:compile\n' +
            '[INFO]    bouncycastle:bcprov-jdk15:jar:140:compile\n' +
            '[INFO]    io.github.openfeign.form:feign-form:jar:3.8.0:compile\n' +
            '[INFO]    org.springframework:spring-context:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    jakarta.xml.bind:jakarta.xml.bind-api:jar:2.3.3:compile\n' +
            '[INFO]    com.caixabank.absis.arch.audit.dec:dec-common-lib:jar:2.10.0-RC1:compile\n' +
            '[INFO]    org.apiguardian:apiguardian-api:jar:1.0.0:test\n' +
            '[INFO]    io.github.openfeign:feign-hystrix:jar:10.5.1:compile\n' +
            '[INFO]    net.bytebuddy:byte-buddy-agent:jar:1.10.14:test\n' +
            '[INFO]    com.fasterxml.jackson.core:jackson-annotations:jar:2.11.2:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-starter-logging:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    org.hamcrest:hamcrest-library:jar:2.2:test\n' +
            '[INFO]    commons-lang:commons-lang:jar:2.6:compile\n' +
            '[INFO]    net.bytebuddy:byte-buddy:jar:1.10.14:test\n' +
            '[INFO]    org.springframework:spring-core:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    xml-apis:xml-apis:jar:1.3.03:compile\n' +
            '[INFO]    org.mockito:mockito-core:jar:2.23.4:test\n' +
            '[INFO]    javax.activation:activation:jar:1.1.1:test\n' +
            '[INFO]    com.caixabank.absis.arch.common:absis-context-lib:jar:1.17.0-RC5:compile\n' +
            '[INFO]    com.fasterxml.jackson.module:jackson-module-parameter-names:jar:2.11.2:compile\n' +
            '[INFO]    com.caixabank.absis.arch.common:absis-security-common-lib:jar:1.17.0-RC5:compile\n' +
            '[INFO]    org.apache.tomcat.embed:tomcat-embed-websocket:jar:9.0.38:compile\n' +
            '[INFO]    com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:jar:2.10.4:compile\n' +
            '[INFO]    org.hamcrest:hamcrest-core:jar:2.2:test\n' +
            '[INFO]    org.jvnet.staxex:stax-ex:jar:1.8.1:compile\n' +
            '[INFO]    jakarta.validation:jakarta.validation-api:jar:2.0.2:compile\n' +
            '[INFO]    io.rest-assured:spring-commons:jar:3.3.0:test\n' +
            '[INFO]    org.springframework.security:spring-security-oauth2-jose:jar:5.3.4.RELEASE:compile\n' +
            '[INFO]    io.github.openfeign:feign-httpclient:jar:10.5.1:compile\n' +
            '[INFO]    com.fasterxml.jackson.datatype:jackson-datatype-jsr310:jar:2.11.2:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-starter-aop:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    io.swagger.core.v3:swagger-annotations:jar:2.1.2:compile\n' +
            '[INFO]    org.mapstruct:mapstruct:jar:1.4.1.Final:compile\n' +
            '[INFO]    com.fasterxml.jackson.core:jackson-core:jar:2.10.4:compile\n' +
            '[INFO]    org.apache.logging.log4j:log4j-to-slf4j:jar:2.13.3:compile\n' +
            '[INFO]    org.springframework:spring-beans:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    org.springframework.security:spring-security-core:jar:5.3.4.RELEASE:compile\n' +
            '[INFO]    org.apache.httpcomponents:httpmime:jar:4.5.12:test\n' +
            '[INFO]    net.minidev:json-smart:jar:2.3:compile\n' +
            '[INFO]    ch.qos.logback.contrib:logback-jackson:jar:0.1.5:compile\n' +
            '[INFO]    org.hibernate.validator:hibernate-validator:jar:6.1.5.Final:compile\n' +
            '[INFO]    org.springframework:spring-expression:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-starter-cache:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-actuator-autoconfigure:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    org.springframework:spring-aop:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    javax.validation:validation-api:jar:2.0.1.Final:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-test:jar:2.3.4.RELEASE:test\n' +
            '[INFO]    com.sun.istack:istack-commons-runtime:jar:3.0.8:compile\n' +
            '[INFO]    jakarta.activation:jakarta.activation-api:jar:1.2.2:compile\n' +
            '[INFO]    org.ow2.asm:asm:jar:5.0.4:compile\n' +
            '[INFO]    org.junit.jupiter:junit-jupiter:jar:5.6.2:test\n' +
            '[INFO]    com.caixabank.absis.arch.common:absis-plugin-lib:jar:1.17.0-RC5:compile\n' +
            '[INFO]    org.springframework:spring-jcl:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    com.github.tomakehurst:wiremock-standalone:jar:2.25.1:test\n' +
            '[INFO]    org.springframework.plugin:spring-plugin-core:jar:2.0.0.RELEASE:compile\n' +
            '[INFO]    org.springdoc:springdoc-openapi-webmvc-core:jar:1.4.0:compile\n' +
            '[INFO]    io.github.openfeign:feign-jackson:jar:10.5.1:compile\n' +
            '[INFO]    com.google.guava:guava:jar:23.6.1-jre:compile\n' +
            '[INFO]    com.caixabank.absis.arch.common:absis-spring-cache-lib:jar:1.17.0-RC5:compile\n' +
            '[INFO]    jakarta.xml.soap:jakarta.xml.soap-api:jar:1.4.2:compile\n' +
            '[INFO]    javax.jms:javax.jms-api:jar:2.0.1:compile\n' +
            '[INFO]    org.slf4j:slf4j-api:jar:1.7.30:compile\n' +
            '[INFO]    com.caixabank.absis.arch.common:feign-starter:jar:1.17.0-RC5:compile\n' +
            '[INFO]    org.glassfish.jaxb:jaxb-runtime:jar:2.3.2:compile\n' +
            '[INFO]    org.assertj:assertj-core:jar:3.16.1:test\n' +
            '[INFO]    com.github.stephenc.jcip:jcip-annotations:jar:1.0-1:compile\n' +
            '[INFO]    org.mockito:mockito-junit-jupiter:jar:2.23.4:test\n' +
            '[INFO]    commons-digester:commons-digester:jar:1.8:compile\n' +
            '[INFO]    org.apache.commons:commons-lang3:jar:3.9:compile\n' +
            '[INFO]    org.springframework:spring-context-support:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    commons-codec:commons-codec:jar:1.14:compile\n' +
            '[INFO]    com.fasterxml.jackson.datatype:jackson-datatype-jdk8:jar:2.11.2:compile\n' +
            '[INFO]    org.apache.tomcat.embed:tomcat-embed-core:jar:9.0.38:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-configuration-processor:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    org.springdoc:springdoc-openapi-ui:jar:1.4.0:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-starter-web:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    org.ccil.cowan.tagsoup:tagsoup:jar:1.2.1:test\n' +
            '[INFO]    org.bouncycastle:bcpkix-jdk15on:jar:1.59:compile\n' +
            '[INFO]    org.springdoc:springdoc-openapi-common:jar:1.4.0:compile\n' +
            '[INFO]    org.springframework:spring-jms:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-starter-tomcat:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    commons-collections:commons-collections:jar:3.2.2:compile\n' +
            '[INFO]    org.webjars:swagger-ui:jar:3.25.4:compile\n' +
            '[INFO]    org.springframework.hateoas:spring-hateoas:jar:1.1.2.RELEASE:compile\n' +
            '[INFO]    io.github.classgraph:classgraph:jar:4.8.69:compile\n' +
            '[INFO]    org.hdrhistogram:HdrHistogram:jar:2.1.12:compile\n' +
            '[INFO]    io.rest-assured:json-path:jar:3.3.0:test\n' +
            '[INFO]    org.junit.platform:junit-platform-engine:jar:1.5.2:test\n' +
            '[INFO]    org.apache.sling:org.apache.sling.javax.activation:jar:0.1.0:test\n' +
            '[INFO]    org.springframework.boot:spring-boot-test-autoconfigure:jar:2.3.4.RELEASE:test\n' +
            '[INFO]    org.springframework.cloud:spring-cloud-context:jar:2.2.5.RELEASE:compile\n' +
            '[INFO]    org.springframework.security:spring-security-rsa:jar:1.0.9.RELEASE:compile\n' +
            '[INFO]    org.jvnet.mimepull:mimepull:jar:1.9.13:compile\n' +
            '[INFO]    com.caixabank.absis.arch.common:absis-spring-cache-starter:jar:1.17.0-RC5:compile\n' +
            '[INFO]    org.springframework.data:spring-data-commons:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    io.github.openfeign:feign-core:jar:10.5.1:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-autoconfigure:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    com.caixabank.absis.arch.core.servicemanager:service-manager-common-lib:jar:1.17.0-RC5:compile\n' +
            '[INFO]    org.junit.platform:junit-platform-commons:jar:1.5.2:test\n' +
            '[INFO]    com.googlecode.json-simple:json-simple:jar:1.1.1:compile\n' +
            '[INFO]    ch.qos.logback:logback-classic:jar:1.2.3:compile\n' +
            '[INFO]    commons-logging:commons-logging:jar:1.2:compile\n' +
            '[INFO]    io.github.openfeign.form:feign-form-spring:jar:3.8.0:compile\n' +
            '[INFO]    org.codehaus.groovy:groovy-xml:jar:2.5.13:test\n' +
            '[INFO]    org.springframework.cloud:spring-cloud-config-client:jar:2.2.5.RELEASE:compile\n' +
            '[INFO]    io.github.openfeign:feign-slf4j:jar:10.5.1:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    org.apache.httpcomponents:httpcore:jar:4.4.13:compile\n' +
            '[INFO]    com.nimbusds:nimbus-jose-jwt:jar:8.19:compile\n' +
            '[INFO]    org.slf4j:jul-to-slf4j:jar:1.7.30:compile\n' +
            '[INFO]    org.yaml:snakeyaml:jar:1.26:compile\n' +
            '[INFO]    org.junit.vintage:junit-vintage-engine:jar:5.6.2:test\n' +
            '[INFO]    org.xmlunit:xmlunit-core:jar:2.7.0:test\n' +
            '[INFO]    com.google.code.findbugs:jsr305:jar:1.3.9:compile\n' +
            '[INFO]    com.fasterxml.jackson.core:jackson-databind:jar:2.11.2:compile\n' +
            '[INFO]    org.springframework.cloud:spring-cloud-commons:jar:2.2.5.RELEASE:compile\n' +
            '[INFO]    com.google.code.gson:gson:jar:2.8.6:compile\n' +
            '[INFO]    com.ibm.mq:com.ibm.mq.allclient:jar:9.1.0.0:compile\n' +
            '[INFO]    org.apache.commons:commons-text:jar:1.8:compile\n' +
            '[INFO]    org.springframework.restdocs:spring-restdocs-core:jar:2.0.5.RELEASE:compile\n' +
            '[INFO]    org.webjars:webjars-locator-core:jar:0.45:compile\n' +
            '[INFO]    org.springframework.cloud:spring-cloud-starter:jar:2.2.5.RELEASE:compile\n' +
            '[INFO]    org.codehaus.groovy:groovy:jar:2.5.13:test\n' +
            '[INFO]    ch.qos.logback:logback-core:jar:1.2.3:compile\n' +
            '[INFO]    com.caixabank.absis.arch.core:absis-annotations-lib:jar:1.17.0-RC5:compile\n' +
            '[INFO]    org.apache.avro:avro:jar:1.9.1:compile\n' +
            '[INFO]    org.junit.jupiter:junit-jupiter-params:jar:5.5.2:test\n' +
            '[INFO]    org.hamcrest:hamcrest:jar:2.2:test\n' +
            '[INFO]    org.springframework.cloud:spring-cloud-starter-openfeign:jar:2.2.5.RELEASE:compile\n' +
            '[INFO]    com.sun.xml.fastinfoset:FastInfoset:jar:1.2.16:compile\n' +
            '[INFO]    io.rest-assured:rest-assured:jar:3.3.0:test\n' +
            '[INFO]    com.jayway.jsonpath:json-path:jar:2.4.0:compile\n' +
            '[INFO]    org.junit.jupiter:junit-jupiter-api:jar:5.5.2:test\n' +
            '[INFO]    org.objenesis:objenesis:jar:3.0.1:test\n' +
            '[INFO]    com.netflix.hystrix:hystrix-core:jar:1.5.18:compile\n' +
            '[INFO]    org.springframework:spring-test:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-starter-actuator:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    org.apache.maven.surefire:common-java5:jar:2.22.0:test\n' +
            '[INFO]    com.fasterxml:classmate:jar:1.5.1:compile\n' +
            '[INFO]    org.springframework.cloud:spring-cloud-openfeign-core:jar:2.2.5.RELEASE:compile\n' +
            '[INFO]    org.junit.platform:junit-platform-launcher:jar:1.5.2:test\n' +
            '[INFO]    org.glassfish:jakarta.el:jar:3.0.3:compile\n' +
            '[INFO]    ch.qos.logback.contrib:logback-json-classic:jar:0.1.5:compile\n' +
            '[INFO]    org.springframework:spring-messaging:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    javax.servlet:javax.servlet-api:jar:4.0.1:compile\n' +
            '[INFO]    org.aspectj:aspectjweaver:jar:1.9.6:compile\n' +
            '[INFO]    org.springframework.cloud:spring-cloud-netflix-archaius:jar:2.2.5.RELEASE:compile\n' +
            '[INFO]    org.codehaus.mojo:animal-sniffer-annotations:jar:1.14:compile\n' +
            '[INFO]    org.springframework.restdocs:spring-restdocs-mockmvc:jar:2.0.5.RELEASE:compile\n' +
            '[INFO]    net.minidev:accessors-smart:jar:1.2:compile\n' +
            '[INFO]    io.swagger.core.v3:swagger-models:jar:2.1.2:compile\n' +
            '[INFO]    jakarta.annotation:jakarta.annotation-api:jar:1.3.5:compile\n' +
            '[INFO]    org.aspectj:aspectjtools:jar:1.9.6:compile\n' +
            '[INFO]    com.caixabank.absis.arch.audit.dec:dec-plugin-common-lib:jar:2.10.0-SNAPSHOT:compile\n' +
            '[INFO]    com.caixabank.absis.arch.common:absis-security-common-starter:jar:1.17.0-RC5:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-starter-json:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    com.sun.xml.messaging.saaj:saaj-impl:jar:1.5.1:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-starter-validation:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    org.apache.httpcomponents:httpclient:jar:4.5.12:compile\n' +
            '[INFO]    com.github.ben-manes.caffeine:caffeine:jar:2.6.2:compile\n' +
            '[INFO]    com.sun.xml.bind:jaxb-osgi:jar:2.2.10:test\n' +
            '[INFO]    io.swagger.core.v3:swagger-integration:jar:2.1.2:compile\n' +
            '[INFO]    com.caixabank.absis.arch.lib.cbk:absisclo-lib:jar:1.2.0:compile\n' +
            '[INFO]    org.codehaus.groovy:groovy-json:jar:2.5.13:test\n' +
            '[INFO]    io.rest-assured:xml-path:jar:3.3.0:test\n' +
            '[INFO]    org.glassfish.jaxb:txw2:jar:2.3.3:compile\n' +
            '[INFO]    commons-io:commons-io:jar:2.5:compile\n' +
            '[INFO]    org.apache.maven.surefire:surefire-logger-api:jar:2.22.0:test\n' +
            '[INFO]    com.netflix.archaius:archaius-core:jar:0.7.6:compile\n' +
            '[INFO]    org.checkerframework:checker-compat-qual:jar:2.0.0:compile\n' +
            '[INFO]    org.springframework:spring-webmvc:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    xerces:xercesImpl:jar:2.8.0:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-starter-hateoas:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    org.jboss.logging:jboss-logging:jar:3.4.1.Final:compile\n' +
            '[INFO]    org.springframework:spring-tx:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    com.google.j2objc:j2objc-annotations:jar:1.1:compile\n' +
            '[INFO]    io.swagger.core.v3:swagger-core:jar:2.1.2:compile\n' +
            '[INFO]    com.caixabank.absis.arch.common:absis-micro-common-lib:jar:1.17.0-RC5:compile\n' +
            '[INFO]    org.bouncycastle:bcprov-jdk15on:jar:1.59:compile\n' +
            '[INFO]    commons-fileupload:commons-fileupload:jar:1.4:compile\n' +
            '[INFO]    com.google.errorprone:error_prone_annotations:jar:2.1.3:compile\n' +
            '[INFO]    org.junit.jupiter:junit-jupiter-engine:jar:5.5.2:test\n' +
            '[INFO]    org.springframework.cloud:spring-cloud-netflix-ribbon:jar:2.2.5.RELEASE:compile\n' +
            '[INFO]    org.springframework:spring-web:jar:5.2.9.RELEASE:compile\n' +
            '[INFO]    org.springframework.boot:spring-boot-actuator:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    org.latencyutils:LatencyUtils:jar:2.0.3:runtime\n' +
            '[INFO]    org.springframework.boot:spring-boot-starter:jar:2.3.4.RELEASE:compile\n' +
            '[INFO]    org.springframework.security:spring-security-oauth2-core:jar:5.3.4.RELEASE:compile\n' +
            '[INFO]    com.vaadin.external.google:android-json:jar:0.0.20131108.vaadin1:test\n' +
            '[INFO]    org.apache.maven.surefire:surefire-api:jar:2.22.0:test\n' +
            '[INFO]\n' +
            '[INFO]\n' +
            '[INFO] --< com.caixabank.absis.arch.audit.dec:dec-key-generation-plugin-spring-boot-starter >--\n' +
            '[INFO] Building dec-key-generation-plugin-spring-boot-starter 2.10.0-SNAPSHOT [3/6]\n' +
            '[INFO] --------------------------------[ jar ]---------------------------------'


    @Test
    public void testExtractSingleModuleDependenciesFromLog() {

        def dependencies = MavenUtils.extractDependenciesFromLog(singleModuleLog)
        assert dependencies.size() == 1, 'We were expecting a single module'
        assert dependencies[0].size() == 290, 'We were expecting 290 dependencies'
        assert dependencies[0][0] == 'org.jboss:jandex:jar:2.1.3.Final:compile', 'It seems that the first dependency is not what we expected'
        assert dependencies[0][289] == 'com.caixabank.absis.arch.core.compensation:absis-arch-compensation-api-lib:jar:1.17.0-SNAPSHOT:compile', 'It seems that the last dependency is not what we expected'

    }

    @Test
    public void testExtractMultipleModuleDependenciesFromLog() {

        def dependencies = MavenUtils.extractDependenciesFromLog(multipleMavenLog)
        assert dependencies.size() == 2, 'We were expecting a two modules'
        assert dependencies[0].size() == 112, 'We were expecting 112 dependencies in the first module'
        assert dependencies[0][0] == 'org.jvnet.mimepull:mimepull:jar:1.9.13:compile', 'It seems that the first dependency is not what we expected'
        assert dependencies[0][111] == 'org.apache.sling:org.apache.sling.javax.activation:jar:0.1.0:test', 'It seems that the last dependency is not what we expected'
        assert dependencies[1].size() == 213, 'We were expecting 213 dependencies in the second module'
        assert dependencies[1][0] == 'net.logstash.logback:logstash-logback-encoder:jar:5.3:compile', 'It seems that the first dependency is not what we expected'
        assert dependencies[1][212] == 'org.apache.maven.surefire:surefire-api:jar:2.22.0:test', 'It seems that the last dependency is not what we expected'

    }

}
