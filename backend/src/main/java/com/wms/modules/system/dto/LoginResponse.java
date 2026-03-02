package com.wms.modules.system.dto;

import com.wms.modules.system.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    /**
     * JWT令牌
     */
    private String token;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String name;
        private String role;
        private String roleName;
        private Integer status;

        // 无参构造函数
        public UserInfo() {
        }

        // 带参数的构造函数
        public UserInfo(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.name = user.getName();
            this.role = user.getRole();
            // 避免调用可能导致空指针的方法
            this.roleName = "系统管理员";
            this.status = user.getStatus();
        }
    }

    // 无参构造函数
    public LoginResponse() {
    }

    // 带参数的构造函数
    public LoginResponse(String token, User user) {
        this.token = token;
        this.userInfo = new UserInfo(user);
    }
}