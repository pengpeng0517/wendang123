package com.wms.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.common.result.Result;
import com.wms.modules.system.entity.User;
import com.wms.modules.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public Result<List<User>> list() {
        try {
            List<User> userList = userService.list();
            return Result.success("获取成功", userList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/info/{id}")
    public Result<User> info(@PathVariable String id) {
        try {
            Long userId = Long.parseLong(id);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success("获取成功", user);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 新增用户
     */
    @PostMapping("/add")
    public Result<User> add(@RequestBody User user) {
        try {
            // 检查用户名是否已存在
            User existingUser = userService.getOne(new QueryWrapper<User>().eq("username", user.getUsername()));
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }

            // 验证角色，只允许添加admin和warehouse角色的用户
            if (user.getRole() == null || (!"admin".equals(user.getRole()) && !"warehouse".equals(user.getRole()))) {
                return Result.error("只能添加系统管理员或仓库管理员角色的用户");
            }

            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setStatus(1); // 默认启用
            user.setName(user.getUsername()); // 设置默认姓名为用户名，避免null值

            boolean success = userService.save(user);
            if (success) {
                return Result.success("新增成功", user);
            } else {
                return Result.error("新增失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 修改用户
     */
    @PutMapping("/update")
    public Result<User> update(@RequestBody User user) {
        try {
            // 检查用户是否存在
            User existingUser = userService.getById(user.getId());
            if (existingUser == null) {
                return Result.error("用户不存在");
            }

            // 如果修改了密码，需要加密
            if (user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().equals(existingUser.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                // 如果密码为空字符串，保持原有密码不变
                user.setPassword(existingUser.getPassword());
            }

            // 设置默认姓名为用户名，避免null值
            if (user.getName() == null || user.getName().isEmpty()) {
                user.setName(user.getUsername());
            }

            boolean success = userService.updateById(user);
            if (success) {
                return Result.success("修改成功", user);
            } else {
                return Result.error("修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable String id) {
        try {
            Long userId = Long.parseLong(id);
            boolean success = userService.removeById(userId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/status/{id}/{status}")
    public Result<String> status(@PathVariable String id, @PathVariable String status) {
        try {
            Long userId = Long.parseLong(id);
            Integer userStatus = Integer.parseInt(status);
            User user = new User();
            user.setId(userId);
            user.setStatus(userStatus);

            boolean success = userService.updateById(user);
            if (success) {
                return Result.success("操作成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/changePassword")
    public Result<String> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            // 检查用户是否存在
            Long userId = Long.parseLong(request.getUserId());
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 验证旧密码
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                return Result.error("旧密码错误");
            }

            // 加密新密码
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));

            boolean success = userService.updateById(user);
            if (success) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("密码修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 修改密码请求参数
     */
    public static class ChangePasswordRequest {
        private String userId;
        private String oldPassword;
        private String newPassword;
        private String confirmPassword; // 确认密码，前端传递但后端不使用

        // getter and setter
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
