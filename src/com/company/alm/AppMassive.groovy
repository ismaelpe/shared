package com.project.alm

class AppMassive {

    private String appName = ''
    private String garApp = ''
    private Map valuesDeployed
    private CloudAppResources
    private String garType
    private String namespace
    private String cloudNamespace
    private String cloudNamespaceId
    private Boolean isArchProject
    private String state


    AppMassive() {
        appName = ''
        garApp = ''
        garType= ''
        valuesDeployed = new HashMap()
        CloudAppResources= null
        cloudNamespace=''
        namespace=''
        isArchProject=false
        cloudNamespaceId= ''
        state="Not Processed"
    }

    String getEstado() {
        return state
    }

    void setEstado(String estado) {
        this.state = estado
    }

    String getIcpNamespaceId() {
        return cloudNamespaceId
    }
    
    void setIcpNamespaceId(String cloudNamespaceId) {
        this.cloudNamespaceId = cloudNamespaceId
    }
    
    Boolean getIsArchProject() {
        return isArchProject
    }
    
    void setIsArchProject(Boolean isArchProject) {
        this.isArchProject = isArchProject
    }
    
    String getIcpNamespace() {
        return cloudNamespace
    }
    
    void setIcpNamespace(String cloudNamespace) {
        this.cloudNamespace = cloudNamespace
    }
    
    String getNamespace() {
        return namespace
    }
    
    void setNamespace(String namespace) {
        this.namespace = namespace
    }
    
    String getGarType() {
        return garType
    }
    
    void setGarType(String garType) {
        this.garType = garType
    }
    
    def getCloudAppResources() {
        return CloudAppResources
    }
    
    void setCloudAppResources(CloudAppResources) {
        this.CloudAppResources = CloudAppResources
    }
    
    String getAppName() {
        return appName
    }
    
    Map getValuesDeployed() {
        return valuesDeployed
    }
    
    void setValuesDeployed(Map valuesDeployed) {
        this.valuesDeployed = valuesDeployed
    }
    
    String getGarApp() {
        return garApp
    }
    
    void setGarApp(String garApp) {
        this.garApp = garApp
    }
    
    void setAppName(String appName) {
        this.appName = appName
    }
}
