package com.project.alm

enum BranchType {
    FEATURE, RELEASE, MASTER, HOTFIX, CONFIGFIX, PROTOTYPE, UNKNOWN

    static BranchType valueOfType(String value) {
        values().find { it.toString() == value }
    }
}
