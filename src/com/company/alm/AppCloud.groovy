package com.project.alm


class AppCloud {

    private String app = ''
    private String routes = ''

    AppCloud() {
        app = ''
        routes = ''
    }


    String toString() {
        return "AppCloud:\n" +
                "\tapp: " + app + "\n" +
                "\troutes: " + routes + "\n"
    }

}