package com.project.alm

class KpiData {

    public String componentType 
    public String component 
    public String major 
    public String minor 
    public String patch 
    public String build
    public String buildTag
    public String environment
    public String features 
    public String lines 
    public String qualityLevel

    public PomXmlStructure pomXml
    public IClientInfo clientInfo
    public PipelineData pipelineData
    public SonarData sonarData

    public KpiLifeCycleStage stage
    public KpiLifeCycleStatus status

    public String stageAsStr

    public String jenkinsEnvironment
    public String jenkinsApplication

    KpiData(PomXmlStructure pomXml, PipelineData pipelineData, SonarData sonarData, KpiLifeCycleStage stage, KpiLifeCycleStatus status, String environment) {
        this.features = ""     
        this.lines = 0     
        this.qualityLevel = ""     
        this.stage = stage
        this.status = status
        this.environment = environment
        this.pomXml = pomXml
        this.pipelineData = pipelineData 
        this.sonarData = sonarData ? sonarData : new SonarData()
        this.jenkinsEnvironment = System.getProperty('jenkins.environment')
        this.jenkinsApplication = System.getProperty('jenkins.application')
    }

    KpiData(IClientInfo clientInfo, PipelineData pipelineData, SonarData sonarData, KpiLifeCycleStage stage, KpiLifeCycleStatus status, String environment) {
        this.features = ""
        this.lines = 0
        this.qualityLevel = ""
        this.stage = stage
        this.status = status
        this.environment = environment
        this.clientInfo = clientInfo
        this.pipelineData = pipelineData
        this.sonarData = sonarData ? sonarData : new SonarData()
        this.jenkinsEnvironment = System.getProperty('jenkins.environment')
        this.jenkinsApplication = System.getProperty('jenkins.application')
    }

    KpiData(String componentType, String component, KpiLifeCycleStage stage, KpiLifeCycleStatus status) {
        this.features = ""     
        this.lines = 0     
        this.qualityLevel = ""     
        this.stage = stage
        this.environment = ""
        this.status = status
        this.pomXml = null
        this.pipelineData = null 
        this.sonarData = new SonarData()  
        this.componentType = componentType
        this.component = component
        this.jenkinsEnvironment = System.getProperty('jenkins.environment')
        this.jenkinsApplication = System.getProperty('jenkins.application')
    }

    def retrieveData() {
        retrieveData(null)
    }

    def retrieveData(String buildTag) {

        this.buildTag = buildTag
        
        if (this.stage) {

            if (this.stage.asString().startsWith("INCREASE_CANARY_\$")) {

                this.stageAsStr = this.stage.asString().replace("\$", "${pipelineData.pipelineStructure.resultPipelineData.cannaryPercentage}")

            } else {

                this.stageAsStr = this.stage.asString()

            }

        } else {

            this.stageAsStr = ""

        }



        if(this.pipelineData!=null) {
            this.componentType = "${this.pipelineData.garArtifactType.getGarName()}" 
            this.build = "${this.pipelineData.buildCode}"
        } else {
            this.componentType = "${this.componentType}"            
            this.build = "0"
        }
        if(this.pomXml!=null){
            this.component =  "${this.pomXml.artifactName}" 
            this.major = "${this.pomXml.getArtifactMajorVersion()}" 
            this.minor = "${this.pomXml.getArtifactMinorVersion()}" 
            this.patch = "${this.pomXml.getArtifactFixVersion()}" 
        } else if(this.clientInfo!=null){
            this.component =  "${this.clientInfo.getArtifactId()}"
            this.major = "${MavenVersionUtilities.getArtifactMajorVersion(this.clientInfo.artifactVersion)}"
            this.minor = "${MavenVersionUtilities.getArtifactMinorVersion(this.clientInfo.artifactVersion)}"
            this.patch = "${MavenVersionUtilities.getArtifactFixVersion(this.clientInfo.artifactVersion)}"
        } else {
            this.component =  "${this.component}"           
            this.major = "0" 
            this.minor = "0" 
            this.patch = "0" 
        }          

        if (this.sonarData.found) {
            def result = 3
                                    
            this.lines = this.sonarData.lines
            
            if (this.sonarData.blocker_violations != 0){
                result = 0
            }     
            
            if (this.sonarData.duplicated_lines_density < 15) {
                result = Math.min(result, 3)
            } else if (this.sonarData.duplicated_lines_density < 25) {
                result = Math.min(result, 2)
            } else if (this.sonarData.duplicated_lines_density < 50) {
                result = Math.min(result, 1)
            } else {
                result = 0
            }
            
            if (this.sonarData.new_critical_violations < 0) {
                result = Math.min(result, 3)
            } else if (this.sonarData.new_critical_violations < 10) {
                result = Math.min(result, 2)
            } else if (this.sonarData.new_critical_violations < 20) {
                result = Math.min(result, 1)
            } else {
                result = 0
            }
            
            if (this.sonarData.comment_lines_density > 60) {
                result = Math.min(result, 3)
            } else if (this.sonarData.comment_lines_density < 50) {
                result = Math.min(result, 2)
            } else if (this.sonarData.comment_lines_density < 25) {
                result = Math.min(result, 1)
            } else {
                result = 0
            }

            if (this.sonarData.sqale_index < 10) {
                result = Math.min(result, 3)
            } else if (this.sonarData.sqale_index < 30) {
                result = Math.min(result, 2)
            } else if (this.sonarData.sqale_index < 60) {
                result = Math.min(result, 1)
            } else {
                result = 0
            }

            if (this.sonarData.sqale_debt_ratio < 10) {
                result = Math.min(result, 3)
            } else if (this.sonarData.sqale_debt_ratio < 20) {
                result = Math.min(result, 2)
            } else if (this.sonarData.sqale_debt_ratio < 40) {
                result = Math.min(result, 1)
            } else {
                result = 0
            }
            
            if (this.sonarData.coverage > 70) {
                result = Math.min(result, 3)
            } else if (this.sonarData.coverage > 25) {
                result = Math.min(result, 2)
            } else if (this.sonarData.coverage > 10) {
                result = Math.min(result, 1)
            } else {
                result = 0
            }
            
            switch(result) { 
                case 1: 
                this.qualityLevel = "LOW"
                break 
                case 2:
                this.qualityLevel = "MEDIUM"
                break 
                case 3: 
                this.qualityLevel = "HARD"
                break 
                default:
                this.qualityLevel = "NONE"
            }

        }

        return [
            space_name_str: GlobalVars.BMX_PRO_SPACE,
            loglevel: "INFO",
            aplicacion: 'alm-alm',
            app: [
                clase_str: 'logs'
            ],
            internal_type: "kpi",
            almenv: GlobalVars.ALMENV_ALMMETRICS,
            timestamp: new Date(System.currentTimeMillis()).format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
            type: "LIFE_CYCLE",
            stage: stageAsStr,
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
            jenkinsEnvironment: this.jenkinsEnvironment,
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
            "\tbuildTag: ${buildTag}\n" +
            "\tenvironment: ${environment}\n" +
            "\tstatus: ${status ? status.asString() : ""}\n" +
            "\tfeatures: ${features}\n" +
            "\tlines: ${lines}\n" +
            "\tqualityLevel: ${qualityLevel}\n" + 
            "\tjenkinsEnvironment: ${jenkinsEnvironment}\n" + 
            "\tjenkinsApplication: ${jenkinsApplication}\n"  
    }
}
