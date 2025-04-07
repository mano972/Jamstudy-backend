package com.app.studentromania.enumtype;

public enum EntityType {

    FACULTY("FACULTY"),
    COMPANY("COMPANY");

    private final String entityTypeText;

    EntityType(String entityTypeText) {
        this.entityTypeText = entityTypeText;
    }

    public String getValue() {
        return entityTypeText;
    }

    @Override
    public String toString() {
        return entityTypeText;
    }
}
