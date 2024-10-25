package com.caixabank.absis3


interface IClientInfo {

    String getGroupId();

    String getArtifactId();

    String getArtifactVersion();

    String getApp(GarAppType garAppType);

    boolean isMicro();

    boolean isSampleApp();

    boolean isApplication();
}
