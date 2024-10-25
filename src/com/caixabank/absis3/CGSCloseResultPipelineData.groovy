package com.caixabank.absis3


import com.caixabank.absis3.GlobalVars
import com.caixabank.absis3.TrazabilidadGPLType

class CGSCloseResultPipelineData extends CGSProvisioningResultPipelineData {


    @Override
    def getAuthServiceToInform() {
        return AuthorizationServiceToInformType.WITHOUT_AUTHORIZATION.toString()
    }


    CGSCloseResultPipelineData(String gitUrl) {
        super(gitUrl)
    }

    @Override
    def getAcciones(boolean result) {
        if (result) {
            return [
            ]
        } else {
            return [
            ]
        }
    }

    @Override
    def getDeployed() {
        return TrazabilidadGPLType.NADA.toString()
    }

    @Override
    def getDisabledPolicy() {
        return GlobalVars.DISABLED_POLICY_BY_VERSION
    }
}
