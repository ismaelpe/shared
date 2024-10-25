package com.project.alm

class AppMassive {

    private String appName = ''
    private String garApp = ''
    private Map valuesDeployed
    private ICPAppResources
    private String garType
    private String namespace
    private String icpNamespace
    private String icpNamespaceId
    private Boolean isArchProject
    private String state


    AppMassive() {
        appName = ''
        garApp = ''
        garType= ''
        valuesDeployed = new HashMap()
        ICPAppResources= null
        icpNamespace=''
        namespace=''
        isArchProject=false
        icpNamespaceId= ''
        state="Not Processed"
    }

    String getEstado() {
        return state
    }

    void setEstado(String estado) {
        this.state = estado
    }

    String getIcpNamespaceId() {
        return icpNamespaceId
    }
    
    void setIcpNamespaceId(String icpNamespaceId) {
        this.icpNamespaceId = icpNamespaceId
    }
    
    Boolean getIsArchProject() {
        return isArchProject
    }
    
    void setIsArchProject(Boolean isArchProject) {
        this.isArchProject = isArchProject
    }
    
    String getIcpNamespace() {
        return icpNamespace
    }
    
    void setIcpNamespace(String icpNamespace) {
        this.icpNamespace = icpNamespace
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
    
    def getICPAppResources() {
        return ICPAppResources
    }
    
    void setICPAppResources(ICPAppResources) {
        this.ICPAppResources = ICPAppResources
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
