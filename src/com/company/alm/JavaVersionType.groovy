package com.project.alm

import com.project.alm.GlobalVars

public enum JavaVersionType {
    JAVA8("Java8"),
    JAVA11("Java11"),
    NOSE("NO")

    private String name;

    private JavaVersionType(String s) {
        name = s;
    }

    public boolean equalsName(String other) {
        return name.equals(other);
    }

    static JavaVersionType valueOfType(String other) {
        values().find { it.name == other }
    }

    public String toString() {
        return name
    }
    
}
