package com.project.alm;

public enum BpiEnvironment {
    Dsv, Qld, Pre, Prd;

    public static BpiEnvironment fromCbk(String cbkEnv) {
        if ("DEV" == cbkEnv) {
            return Dsv;
        } else if ("TST" == cbkEnv) {
            return Qld;
        } else if ("PRE" == cbkEnv) {
            return Pre;
        } else if ("PRO" == cbkEnv) {
            return Prd;
        } else {
            return null;
        }
    }
}
