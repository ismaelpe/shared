package com.caixabank.absis3

abstract class DataSourceGenerator {


    public final static String initialConnections = "initialConnections"
    public final static String maxConnections = "maxConnections"
    public final static String capacityIncrement = "capacityIncrement"
    public final static String idleTimeout = "idleTimeout"
    public final static String highestNumWaiters = "highestNumWaiters"
    public final static String initializationFailTimeout = "initializationFailTimeout"
    public final static String maxWait = "maxWait"
    public final static String frequencyTestIdle = "frequencyTestIdle"
    public final static String validationTimeout = "validationTimeout"
    public final static String validationInterval = "validationInterval"
    public final static String activeConnectionTimeout = "activeConnectionTimeout"
    public final static String testOnReserve = "testOnReserve"
    public final static String profileHarvestFrequency = "profileHarvestFrequency"
    public final static String inactiveConnectionTimeout = "inactiveConnectionTimeout"
    public final static String validationQuery = "validationQuery"
    public final static String loginDelaySeconds = "loginDelaySeconds"
    public final static String statementCacheSize = "statementCacheSize"
    public final static String statementCacheType = "statementCacheType"
    public final static String secondsToTrustIdleConnection = "secondsToTrustIdleConnection"
    public final static String statementTimeout = "statementTimeout"
    public final static String autocommit = "autocommit"
    public final static String jdbcDriver = "jdbcDriver"
    public final static String maxIdleConnections = "maxIdleConnections"
    public final static String timeBetweenEvictionRunsMillis = "timeBetweenEvictionRunsMillis"
    public final static String minIdle = "minIdle"
    public final static String testOnReturn = "testOnReturn"
    public final static String testWhileIdle = "testWhileIdle"
    public final static String poolName = "poolName"
    public final static String connectionInitSql = "connectionInitSql"
    public final static String schema = "schema"
    public final static String connectionProperties = "connectionProperties"
    public final static String url = "url"
    public final static String username = "username"
    public final static String password = "password"
    public final static String optional = "optional"


    abstract def generate(String domain, String app, GarAppType appType, Map originalData)
	
	abstract def generate(String domain, String app, GarAppType appType, Map originalData, Map tenants)

}

