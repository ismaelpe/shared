package com.project.alm


class HikariDataSourceTenantValidationException extends RuntimeException {

    def expectedTenantList
    Set datasourcesTenants
    Set readonlyDatasourcesTenants
    def datasourceConnections
    def readOnlyDatasourceConnections
	def readOnlyDatasourceConnectionsList


    HikariDataSourceTenantValidationException(String message, def expectedTenantList, Set datasourcesTenants, Set readonlyDatasourcesTenants, def datasourceConnections, def readOnlyDatasourceConnections = null) {
        super(message)
        this.expectedTenantList = expectedTenantList
        this.datasourcesTenants = datasourcesTenants
        this.readonlyDatasourcesTenants = readonlyDatasourcesTenants
        this.datasourceConnections = datasourceConnections
        this.readOnlyDatasourceConnections = readOnlyDatasourceConnections
    }

    HikariDataSourceTenantValidationException(String message, Throwable cause, def expectedTenantList, Set datasourcesTenants, Set readonlyDatasourcesTenants, def datasourceConnections, def readOnlyDatasourceConnections = null, def readOnlyDatasourceConnectionsList = null) {
        super(message, cause)
        this.expectedTenantList = expectedTenantList
        this.datasourcesTenants = datasourcesTenants
        this.readonlyDatasourcesTenants = readonlyDatasourcesTenants
        this.datasourceConnections = datasourceConnections
        this.readOnlyDatasourceConnections = readOnlyDatasourceConnections
		this.readOnlyDatasourceConnectionsList = readOnlyDatasourceConnectionsList
    }

    def prettyPrint() {

        return
        """
        ${message}
        
        expectedTenantList: ${expectedTenantList}
        datasourcesTenants: ${datasourcesTenants}
        readonlyDatasourcesTenants: ${readonlyDatasourcesTenants}
        datasourceConnections: ${datasourceConnections}
        readOnlyDatasourceConnections: ${readOnlyDatasourceConnections}
        readOnlyDatasourceConnectionsList: ${readOnlyDatasourceConnectionsList}
        """

    }
}

