package com.damai.damaiticket.controller;

import java.util.Map;
import com.damai.damaiticket.entity.User;
import com.damai.damaiticket.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // 注册
    @PostMapping("/register")
    public String register(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String phone = (String) body.get("phone");
        String role = (String) body.get("role");           // "USER" 或 "ADMIN"
        String adminCode = (String) body.get("adminCode"); // 邀请码

        if (username == null || password == null) return "注册失败：缺少用户名或密码";

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);

        // 默认只能注册 USER
        user.setRole("USER");

        // 如果要注册管理员，必须邀请码正确
        if ("ADMIN".equalsIgnoreCase(role)) {
            String CODE = "123456"; // ✅ 你的管理员邀请码（可改）
            if (!CODE.equals(adminCode)) {
                return "注册失败：管理员邀请码错误";
            }
            user.setRole("ADMIN");
        }

        boolean success = userService.register(user);
        return success ? "注册成功" : "注册失败（用户名可能已存在）";
    }


    // 登录
    @PostMapping("/login")
    public User login(@RequestBody User user) {
        return userService.login(user.getUsername(), user.getPassword());
    }
}
