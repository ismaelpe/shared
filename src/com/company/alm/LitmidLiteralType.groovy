package com.project.alm

enum LitmidLiteralType {

    APPLICATION("aplicativo"),   
	ERROR("error")

    String literalType;

    private LitmidLiteralType(String literalType) {
        this.literalType = literalType;
    }

    String literalType() {
        return this.literalType;
    }
}

