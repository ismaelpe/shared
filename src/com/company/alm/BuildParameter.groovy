package com.caixabank.absis3


class BuildParameter {

    private String name;
    private String value;

    BuildParameter() {

    }

    public void init(def actionParameter) {
        this.name = actionParameter["nombre"].toString()
        this.value = actionParameter["valor"].toString()
    }

    String toString() {
        return "BuildParameter:\n" +
                "\tname: $name\n" +
                "\tvalue: $value\n"
    }

}