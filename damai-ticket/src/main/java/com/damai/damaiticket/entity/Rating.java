package com.damai.damaiticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rating")
public class Rating {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long showId;
    private Long orderId;

    private Integer score;      // 1~5
    private String content;     // 可选
    private LocalDateTime createTime;
}
