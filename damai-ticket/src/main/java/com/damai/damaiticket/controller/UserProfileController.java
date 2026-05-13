package com.damai.damaiticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.damai.damaiticket.entity.User;
import com.damai.damaiticket.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    // ✅ 查看个人信息
    @GetMapping("/{id}")
    public User get(@PathVariable Long id) {
        User u = userService.getById(id);
        if (u != null) u.setPassword(null); // 不返回密码
        return u;
    }

    // ✅ 修改个人信息（只允许改 phone/nickname/email）
    @PutMapping("/update")
    public String update(@RequestBody User body) {
        if (body.getId() == null) return "更新失败：缺少id";

        User db = userService.getById(body.getId());
        if (db == null) return "更新失败：用户不存在";

        db.setPhone(body.getPhone());
        db.setNickname(body.getNickname());
        db.setEmail(body.getEmail());

        boolean ok = userService.updateById(db);
        return ok ? "更新成功" : "更新失败";
    }
}
