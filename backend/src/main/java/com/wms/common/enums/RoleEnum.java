package com.wms.common.enums;

public enum RoleEnum {
    /**
     * 系统管理员
     */
    SYSTEM_ADMIN("system_admin", "系统管理员"),
    /**
     * 仓库管理员
     */
    WAREHOUSE_ADMIN("warehouse_admin", "仓库管理员");

    private String code;
    private String name;

    RoleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static RoleEnum getByCode(String code) {
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}