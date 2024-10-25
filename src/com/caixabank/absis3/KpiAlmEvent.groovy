package com.caixabank.absis3

class KpiAlmEvent extends KpiData {

    KpiAlmEventStage almStage
    KpiAlmEventOperation operation
    KpiAlmEventSubOperation subOperation
    KpiAlmEventStatusType statusType
    String errorCode

    Long duration


    KpiAlmEvent(PomXmlStructure pomXml, PipelineData pipelineData,
                KpiAlmEventStage almStage, KpiAlmEventOperation operation) {

        this(pomXml, pipelineData, almStage, operation, null, null, null)
    }

    KpiAlmEvent(PomXmlStructure pomXml, PipelineData pipelineData,
                KpiAlmEventStage almStage, KpiAlmEventOperation operation, KpiAlmEventSubOperation subOperation,
                KpiAlmEventStatusType statusType, String errorCode,
                String environment = null, Long duration = null) {

        super(pomXml, pipelineData, null, null, null, environment ? environment : pipelineData?.bmxStructure?.environment?.toUpperCase())

        this.almStage = almStage
        this.operation = operation
        this.subOperation = subOperation
        this.statusType = statusType
        this.errorCode = errorCode
        this.duration = duration
    }

    KpiAlmEvent(IClientInfo clientInfo, PipelineData pipelineData,
                KpiAlmEventStage almStage, KpiAlmEventOperation operation) {

        this(clientInfo, pipelineData, almStage, operation, null, null, null)
    }

    KpiAlmEvent(IClientInfo clientInfo, PipelineData pipelineData,
                KpiAlmEventStage almStage, KpiAlmEventOperation operation, KpiAlmEventSubOperation subOperation,
                KpiAlmEventStatusType statusType, String errorCode,
                String environment = null, Long duration = null) {

        super(clientInfo, pipelineData, null, null, null, environment ? environment : pipelineData?.bmxStructure?.environment?.toUpperCase())

        this.almStage = almStage
        this.operation = operation
        this.subOperation = subOperation
        this.statusType = statusType
        this.errorCode = errorCode
        this.duration = duration
    }

    KpiAlmEvent operation(KpiAlmEventOperation operation) {

        this.operation = operation
        return this

    }

    KpiAlmEvent subOperation(KpiAlmEventSubOperation subOperation) {

        this.subOperation = subOperation
        return this

    }

    KpiAlmEvent statusType(KpiAlmEventStatusType statusType) {

        this.statusType = statusType
        return this

    }

    KpiAlmEvent errorCode(String errorCode) {

        this.errorCode = errorCode
        return this

    }

    KpiAlmEvent duration(Long duration) {

        this.duration = duration
        return this

    }

    def retry() {

        return this.subOperation(KpiAlmEventSubOperation.RETRY)
            .statusType(null)
            .errorCode(null)
            .duration(null)

    }

    def callSuccess(Long duration) {

        return this.subOperation(KpiAlmEventSubOperation.CALL)
            .statusType(KpiAlmEventStatusType.SUCCESS)
            .errorCode(null)
            .duration(duration)

    }

    def requestSuccess(Long duration) {

        return this.subOperation(KpiAlmEventSubOperation.REQUEST)
            .statusType(KpiAlmEventStatusType.SUCCESS)
            .errorCode(null)
            .duration(duration)

    }	
	
	def pipelineSuccess(Long duration) {
		
		return this.subOperation(KpiAlmEventSubOperation.PIPELINE)
			.statusType(KpiAlmEventStatusType.SUCCESS)
			.errorCode(null)
			.duration(duration)
		
	}
	
	def pipelineFail(Long duration, String errorCode = KpiAlmEventErrorCode.UNDEFINED) {
		
		return this.subOperation(KpiAlmEventSubOperation.PIPELINE)
			.statusType(KpiAlmEventStatusType.FAIL)
			.errorCode(errorCode)
			.duration(duration)
		
	}
	
    def callFail(Long duration, String errorCode = KpiAlmEventErrorCode.UNDEFINED) {

        return this.subOperation(KpiAlmEventSubOperation.CALL)
            .statusType(KpiAlmEventStatusType.FAIL)
            .errorCode(errorCode)
            .duration(duration)

    }

    def requestFail(Long duration, String errorCode = KpiAlmEventErrorCode.UNDEFINED) {

        return this.subOperation(KpiAlmEventSubOperation.REQUEST)
            .statusType(KpiAlmEventStatusType.FAIL)
            .errorCode(errorCode)
            .duration(duration)

    }

    def callAppFail(Long duration, String errorCode = KpiAlmEventErrorCode.UNDEFINED) {

        return this.subOperation(KpiAlmEventSubOperation.CALL)
            .statusType(KpiAlmEventStatusType.APP_FAIL)
            .errorCode(errorCode)
            .duration(duration)

    }

    def requestAppFail(Long duration, String errorCode = KpiAlmEventErrorCode.UNDEFINED) {

        return this.subOperation(KpiAlmEventSubOperation.REQUEST)
            .statusType(KpiAlmEventStatusType.APP_FAIL)
            .errorCode(errorCode)
            .duration(duration)

    }

    def callAlmFail(Long duration, String errorCode = KpiAlmEventErrorCode.UNDEFINED) {

        return this.subOperation(KpiAlmEventSubOperation.CALL)
            .statusType(KpiAlmEventStatusType.ALM_FAIL)
            .errorCode(errorCode)
            .duration(duration)

    }

    def requestAlmFail(Long duration, String errorCode = KpiAlmEventErrorCode.UNDEFINED) {

        return this.subOperation(KpiAlmEventSubOperation.REQUEST)
            .statusType(KpiAlmEventStatusType.ALM_FAIL)
            .errorCode(errorCode)
            .duration(duration)

    }

    def retrieveData() {
        retrieveData(null)
    }

    def retrieveData(String buildTag) {
    
        super.retrieveData(buildTag)

        String jenkinsEnvironment = System.getProperty('jenkins.environment')
        String jenkinsApplication = System.getProperty('jenkins.application')

        this.status = KpiAlmEventStatusType.SUCCESS == statusType ? KpiLifeCycleStatus.OK : KpiLifeCycleStatus.KO

        return [
            space_name_str: GlobalVars.BMX_PRO_SPACE,
            loglevel: "INFO",
            aplicacion: 'absis-alm',
            app: [
                clase_str: 'logs'
            ],
            internal_type: "kpi",
			almenv: GlobalVars.ALMENV_ALMMETRICS,
            timestamp: new Date(System.currentTimeMillis()).format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
            type: "ALM_EVENT",
            stage: this.stageAsStr,
            componentType: this.componentType,
            component:  this.component?.toLowerCase(),
            major: this.major,
            minor: this.minor,
            patch: this.patch,
            build: this.build,
            buildTag: this.buildTag,
            environment: this.environment,
            status: this.status ? this.status.asString() : "",
            features: this.features,
            lines: this.lines, 
            qualityLevel: this.qualityLevel,
            pipelineType: pipelineData?.pipelineStructureType,
            almStage: this.almStage ? this.almStage.asString() : "",
            operation: this.operation ? this.operation.asString() : "",
            subOperation: this.subOperation ? this.subOperation.asString() : "",
            statusType: this.statusType ? this.statusType.asString() : "",
            errorCode: this.errorCode ? this.errorCode : "",
            duration: this.duration ? this.duration/1000 : "",
            jenkinsenvironment: this.jenkinsEnvironment,
            jenkinsApplication: this.jenkinsApplication
        ] 
    }

    String toString() {
        return "KpiData:\n" +
            "\tstage: ${stageAsStr}\n" +
            "\tcomponentType: ${componentType}\n" +
            "\tcomponent: ${component?.toLowerCase()}\n" +
            "\tmajor: ${major}\n" +
            "\tminor: ${minor}\n" +
            "\tpatch: ${patch}\n" +
            "\tbuild: ${build}\n" +
            "\tenvironment: ${environment}\n" +
            "\tstatus: ${status}\n" +
            "\tfeatures: ${features}\n" +
            "\tlines: ${lines}\n" +
            "\tqualityLevel: ${qualityLevel}\n" +
            "\tpipelineType: ${pipelineData?.pipelineStructureType}\n" +
            "\talmStage: ${almStage}\n" +
            "\toperation: ${operation}\n" +
            "\tsubOperation: ${subOperation}\n" +
            "\tstatusType: ${statusType}\n" +
            "\terrorCode: ${errorCode}\n" +
            "\tduration: ${duration}\n" + 
            "\tjenkinsenvironment: ${jenkinsEnvironment}\n" + 
            "\tjenkinsApplication: ${jenkinsApplication}\n"
    }
}
