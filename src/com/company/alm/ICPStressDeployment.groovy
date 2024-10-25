package com.project.alm;


class ICPStressDeployment {
	
	private static String JSON_DEPLOYMENT = "deployment:\n  readinessProbe:\n    periodSeconds: 60\n    timeoutSeconds: 50\n    failureThreshold: 30\n  livenessProbe:\n    periodSeconds: 60\n    timeoutSeconds: 50\n    failureThreshold: 30\nlocal:\n  app:\n    enableNonMtls: true\n    ingress:\n      enabled: true\n      deploymentArea: absis\n      defineBodySize: true\n      maxBodySize: 30m\n      defineTimeout: true\n      sendTimeout: 100\n      readTimeout: 100\n      connectTimeout: 15\n      absis:\n        enabled: true\n      mtls:\n        enabled: false\n        needsSystemRoute: true\n        route: \n        needsSpecialVerifyDepth: false\n        verifyDepth: 2\n    envVars:\n      - name: ALM_APP_ID\n        value: JMETERAPP\n      - name: ALM_CENTER_ID\n        value: 1\n      - name: ALM_APP_TYPE\n        value: SRV.DS\n      - name: ALM_ENVIRONMENT\n        value: pre\n      - name: ALM_APP_DOMAIN\n        value: custodiagarantiasdocumentos\n      - name: ALM_APP_SUBDOMAIN\n        value: NO_SUBDOMAIN\n      - name: ALM_APP_COMPANY\n        value: CBK\n      - name: JAVA_OPTS\n        value: '-Dspring.cloud.config.failFast=true'\n      - name: nonProxyHosts\n        value: 'gelogasr01.lacaixa.es|*.cxb-ab3cor-pre|*.cxb-ab3app-pre|*.api.tst.internal.cer.project.com|*.api.pre.internal.cer.project.com|*.api.pro.internal.cer.project.com|*.apigwi.pre.serveis.absiscloud.lacaixa.es|*.apigwi.pro.serveis.absiscloud.lacaixa.es'\n      - name: http.additionalNonProxyHosts\n        value: 'gelogasr01.lacaixa.es,cxb-ab3cor-pre,cxb-ab3app-pre,api.tst.internal.cer.project.com,api.pre.internal.cer.project.com,api.pro.internal.cer.project.com,apigwi.pre.serveis.absiscloud.lacaixa.es,apigwi.pro.serveis.absiscloud.lacaixa.es'\n      - name: NO_PROXY\n        value: cxb-ab3cor-pre\n      - name: ARTIFACT_ID\n        value: <artifactId>\n      - name: VERSION_ARTIFACT\n        value: <version>\n      - name: GROUP_ID\n        value: <groupId>\n      - name: TEST_PLAN_CLASSIFIER\n        value: <classifier>\n      - name: ENVIRONMENT\n        value: <environment>\n      - name: CENTER\n        value: <center>\n      - name: CF_INSTANCE_INDEX\n        value: 1\n      - name: spring.cloud.config.failFast\n        value: true\n      - name: SPRING_PROFILES_ACTIVE\n        value: cloud,pre,icp,icppre,app,appcbk\n      - name: ALM_ICP_ENVIRONMENT\n        value: pre\n    secrets:\n      - name: cbk-apps-demo-arqrun-cbkdatasource-database\nabsis:\n  app:\n    loggingElkStack: alm0\n    replicas: 1\n    instance: JMETER1\n    name: JMETER\n  resources:\n    requests:\n      memory: 1024Mi\n      cpu: 500m\n    limits:\n       memory: 2048Mi\n       cpu: 2000m\n  apps:\n    envQualifier:\n      stable:\n        id: jmeterstress-g\n        colour: G\n        image: docker-registry.cloud.project.com/containers/ab3cor/jmeterapp1:2.1.2\n        version: 1.3.0\n        stable: false\n        new: false\n        replicas: 1\n        readinessProbePath: /actuator/health\n        livenessProbePath: /actuator/health\n        envVars:\n          TIMESTAMP: <timestamp>\n        requests_memory: 2048Mi\n        requests_cpu: 500m\n        limits_memory: 2048Mi\n        limits_cpu: 2000m\n  services:\n    envQualifier:\n      stable:\n        id: jmeter-micro-1\n        targetColour: G\n"

	String groupId
	String artifactId
	String version
	String classifier
	String timestamp
	String environment
	String center
	
	ICPStressDeployment(String groupId, String artifactId, String version, String classifier, String center, String environment, String timestamp){
		this.groupId = groupId
		this.artifactId = artifactId
		this.version = version
		this.classifier = classifier
		this.timestamp = timestamp
		this.environment = environment
		this.center = center
	}
	
	String getChartValuesApps() {
		return JSON_DEPLOYMENT
			.replace("<groupId>", groupId)
			.replace("<artifactId>", artifactId)
			.replace("<version>", version)
			.replace("<classifier>", classifier)
			.replace("<environment>", environment)
			.replace("<center>", center)
			.replace("<timestamp>", timestamp)
	}
}