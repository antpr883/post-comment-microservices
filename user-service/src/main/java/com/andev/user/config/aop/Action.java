package com.andev.user.config.aop;

public enum Action {
    CREATE("CREATE"),
    UPDATE("UPDATED"),
    DELETE("DELETED"),
    LOAD("LOAD");

    private final String name;

    Action(String value) {
        this.name = value;
    }

    public String value() {
        return this.name;
    }

    @Override
    public String toString() {
        return name;
    }
}
