package com.wms.modules.system.controller;

import com.wms.common.result.Result;
import com.wms.modules.system.dto.LoginRequest;
import com.wms.modules.system.dto.LoginResponse;
import com.wms.modules.system.entity.User;
import com.wms.modules.system.service.UserService;
import com.wms.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // 从数据库查询用户
        User user = userService.findByUsername(loginRequest.getUsername());
        
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        
        // 验证密码
        if (!userService.checkPassword(user, loginRequest.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            return Result.error("用户已被禁用");
        }
        
        // 检查用户角色，只有系统管理员和仓库管理员可以登录
        if (!"admin".equals(user.getRole()) && !"warehouse".equals(user.getRole())) {
            return Result.error("只有系统管理员和仓库管理员才能登录系统");
        }
        
        // 生成JWT令牌
        String token = tokenProvider.generateToken(user);
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setName(user.getName());
        userInfo.setRole(user.getRole());
        userInfo.setStatus(user.getStatus());
        
        // 设置角色名称
        if ("admin".equals(user.getRole())) {
            userInfo.setRoleName("系统管理员");
        } else if ("warehouse".equals(user.getRole())) {
            userInfo.setRoleName("仓库管理员");
        } else {
            userInfo.setRoleName("普通用户");
        }
        
        response.setUserInfo(userInfo);

        return Result.success("登录成功", response);
    }
}