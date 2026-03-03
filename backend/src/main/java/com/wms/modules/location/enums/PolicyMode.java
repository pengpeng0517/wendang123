package com.wms.modules.location.enums;

public enum PolicyMode {
    RULE("rule"),
    SHADOW("shadow"),
    MANUAL("manual"),
    AUTO("auto");

    private final String value;

    PolicyMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PolicyMode fromValue(String value) {
        if (value == null) {
            return RULE;
        }
        for (PolicyMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value.trim())) {
                return mode;
            }
        }
        return RULE;
    }
}
