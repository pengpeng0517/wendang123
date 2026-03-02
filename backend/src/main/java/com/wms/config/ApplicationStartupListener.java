package com.wms.config;

import com.wms.modules.system.entity.User;
import com.wms.modules.system.service.UserService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private UserService userService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 创建系统管理员
        User adminUser = userService.findByUsername("admin");
        if (adminUser == null) {
            adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRole("admin");
            adminUser.setStatus(1);
            adminUser.setCreateTime(LocalDateTime.now());
            adminUser.setUpdateTime(LocalDateTime.now());
            userService.save(adminUser);
            System.out.println("已创建系统管理员用户: admin");
        }

        // 创建仓库管理员
        User warehouseUser = userService.findByUsername("warehouse");
        if (warehouseUser == null) {
            warehouseUser = new User();
            warehouseUser.setUsername("warehouse");
            warehouseUser.setPassword(passwordEncoder.encode("admin123"));
            warehouseUser.setRole("warehouse");
            warehouseUser.setStatus(1);
            warehouseUser.setCreateTime(LocalDateTime.now());
            warehouseUser.setUpdateTime(LocalDateTime.now());
            userService.save(warehouseUser);
            System.out.println("已创建仓库管理员用户: warehouse");
        }
    }
}