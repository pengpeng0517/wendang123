package com.wms.modules.location.enums;

public enum PolicyType {
    RULE("rule"),
    RL("rl");

    private final String value;

    PolicyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
