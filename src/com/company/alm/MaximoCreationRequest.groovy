package com.project.alm

class MaximoCreationRequest {

    String emailUsuarioCreador
    MaximoAbstractFallo tipoFallo

    MaximoCreationRequest emailUsuarioCreador(String emailUsuarioCreador) {
        this.emailUsuarioCreador = emailUsuarioCreador
        return this
    }

    MaximoCreationRequest tipoFallo(MaximoAbstractFallo tipoFallo) {
        this.tipoFallo = tipoFallo
        return this
    }

}
