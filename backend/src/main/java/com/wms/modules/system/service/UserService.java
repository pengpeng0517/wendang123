package com.wms.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.system.entity.User;

public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(String username);

    /**
     * 验证密码是否正确
     * @param user 用户信息
     * @param rawPassword 原始密码
     * @return 是否正确
     */
    boolean checkPassword(User user, String rawPassword);
}