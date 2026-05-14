package com.damai.damaiticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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

    // 非数据库字段
    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String email;

    @TableField(exist = false)
    private String avatar;
}
