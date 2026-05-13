package com.damai.damaiticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;

    // 手机号
    private String phone;

    // 角色：USER / ADMIN
    private String role;

    // ✅ 新增：昵称
    private String nickname;

    // ✅ 新增：邮箱
    private String email;
}
