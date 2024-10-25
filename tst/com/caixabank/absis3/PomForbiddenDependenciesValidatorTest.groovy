package com.caixabank.absis3

import org.junit.Test

class PomForbiddenDependenciesValidatorTest extends GroovyTestCase {

    def bffRestrictions = [
        /com\.caixabank\.absis\.ads\.transaction:ads-.*-lib/,
        /com\.caixabank\.absis\.arch\.backend\.ads:adsconnector-common-lib/,
        /com\.caixabank\.absis\.arch\.backend\.ads:adsconnector-lib-starter/,
        /com\.caixabank\.absis\.arch\.backend\.absis2:a2connector-spring-boot-starter/
    ]

	def dependencyTreeA2 = 'com.caixabank.absis.apps.bff.cbk.demo:demoarqbff-bff:jar:1.9.0-SNAPSHOT\n'+
		'    com.caixabank.absis.arch.backend.ase:se-restchecklist-connector-spring-boot-starter:jar:1.1.0:compile\n'+
		'    com.caixabank.absis.arch.backend.absis2:a2connector-spring-boot-starter:jar:5.3.4.RELEASE:compile\n'+
		'       org.springframework.boot:spring-boot-starter:jar:2.6.6:compile\n'+
		'          org.springframework.boot:spring-boot-starter-logging:jar:2.6.6:compile\n'+
		'             org.apache.logging.log4j:log4j-to-slf4j:jar:2.17.2:compile\n'+
		'                org.apache.logging.log4j:log4j-api:jar:2.17.2:compile\n'+
		'             org.slf4j:jul-to-slf4j:jar:1.7.36:compile\n'+
		'          org.yaml:snakeyaml:jar:1.29:compile\n'+
		'       org.springframework:spring-web:jar:5.3.18:compile\n'+
		'          org.springframework:spring-beans:jar:5.3.18:compile\n'+
		'       io.github.openfeign:feign-annotation-error-decoder:jar:1.3.0:compile\n'+
		'       org.springframework.boot:spring-boot-starter-validation:jar:2.6.6:compile\n'+
		'          org.apache.tomcat.embed:tomcat-embed-el:jar:9.0.60:compile\n'+
		'          org.hibernate.validator:hibernate-validator:jar:6.2.3.Final:compile\n'+
		'             jakarta.validation:jakarta.validation-api:jar:2.0.2:compile\n'+
		'             org.jboss.logging:jboss-logging:jar:3.4.3.Final:compile\n'+
		'             com.fasterxml:classmate:jar:1.5.1:compile\n'+
		'    com.caixabank.absis.apps.dataservice.demo.contract:arqrun-micro:jar:2.13.0:compile\n';
		
	def dependencyTreeADS = 'com.caixabank.absis.apps.bff.cbk.demo:demoarqbff-bff:jar:1.9.0-SNAPSHOT\n'+
		'    com.caixabank.absis.arch.backend.ase:se-restchecklist-connector-spring-boot-starter:jar:1.1.0:compile\n'+
		'       org.springframework.boot:spring-boot-starter:jar:2.6.6:compile\n'+
		'          org.springframework.boot:spring-boot-starter-logging:jar:2.6.6:compile\n'+
		'             org.apache.logging.log4j:log4j-to-slf4j:jar:2.17.2:compile\n'+
		'                org.apache.logging.log4j:log4j-api:jar:2.17.2:compile\n'+
		'             com.caixabank.absis.ads.transaction:ads-ads0000b-lib:jar:0.12.0:compile\n'+
		'          org.yaml:snakeyaml:jar:1.29:compile\n'+
		'       com.caixabank.absis.arch.backend.ads:adsconnector-common-lib:jar:1.8.0-RC3:compile\n'+
		'          org.springframework:spring-beans:jar:5.3.18:compile\n'+
		'       org.springframework.boot:spring-boot-starter-validation:jar:2.6.6:compile\n'+
		'       com.caixabank.absis.ads.transaction:ads-ads0000a-lib:jar:0.20.0:compile\n'+
		'          org.apache.tomcat.embed:tomcat-embed-el:jar:9.0.60:compile\n'+
		'          org.hibernate.validator:hibernate-validator:jar:6.2.3.Final:compile\n'+
		'             com.caixabank.absis.arch.backend.ads:adsconnector-lib-starter:jar:1.8.0-RC3:compile\n'+
		'             jakarta.validation:jakarta.validation-api:jar:2.0.2:compile\n'+
		'             com.fasterxml:classmate:jar:1.5.1:compile\n'+
		'    com.caixabank.absis.apps.dataservice.demo.contract:arqrun-micro:jar:2.13.0:compile\n';
	
    @Test
    void testGiven_BFFMultipleModuleDependencyList_withADSDependencies_returns_allForbiddenDependencies() {

        PomForbiddenDependenciesValidator validator = new PomForbiddenDependenciesValidator(bffRestrictions)
        def forbiddenDependencies = validator.validate("com.caixabank.absis.apps.bff.cbk.demo:demoarqbff-bff", dependencyTreeADS)

        assert forbiddenDependencies == [
            'com.caixabank.absis.ads.transaction:ads-ads0000b-lib:jar:0.12.0:compile',
            'com.caixabank.absis.arch.backend.ads:adsconnector-common-lib:jar:1.8.0-RC3:compile',
            'com.caixabank.absis.ads.transaction:ads-ads0000a-lib:jar:0.20.0:compile',
            'com.caixabank.absis.arch.backend.ads:adsconnector-lib-starter:jar:1.8.0-RC3:compile'],
            'We did not get the expected dependencies'

    }

    @Test
    void testGiven_BFFMultipleModuleDependencyList_withADSDependenciesAndWhitelist_returns_allForbiddenDependencyButWhitelisted() {

        def whitelist = '{"com\\\\.caixabank\\\\.absis\\\\.apps\\\\.bff\\\\.cbk\\\\.demo:demoarqbff-bff":["com\\\\.caixabank\\\\.absis\\\\.ads\\\\.transaction:ads-ads0000b-lib"]}'

        PomForbiddenDependenciesValidator validator = new PomForbiddenDependenciesValidator(bffRestrictions, whitelist)
        def forbiddenDependencies = validator.validate(
            "com.caixabank.absis.apps.bff.cbk.demo:demoarqbff-bff",
            dependencyTreeADS
        )

        assert forbiddenDependencies == [
            'com.caixabank.absis.arch.backend.ads:adsconnector-common-lib:jar:1.8.0-RC3:compile',
            'com.caixabank.absis.ads.transaction:ads-ads0000a-lib:jar:0.20.0:compile',
            'com.caixabank.absis.arch.backend.ads:adsconnector-lib-starter:jar:1.8.0-RC3:compile'],
            'We did not get the expected dependencies'

    }

    @Test
    void testGiven_BFFMultipleModuleDependencyList_withA2Dependencies_returns_allForbiddenDependencies() {

        PomForbiddenDependenciesValidator validator = new PomForbiddenDependenciesValidator(bffRestrictions)
        def forbiddenDependencies = validator.validate("com.caixabank.absis.apps.bff.cbk.demo:demoarqbff-bff", dependencyTreeA2)

        assert forbiddenDependencies == [
            'com.caixabank.absis.arch.backend.absis2:a2connector-spring-boot-starter:jar:5.3.4.RELEASE:compile'],
            'We did not get the expected dependencies'

    }

}
