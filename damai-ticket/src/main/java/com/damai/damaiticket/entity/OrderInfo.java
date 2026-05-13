package com.damai.damaiticket.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_info")
public class OrderInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long showId;
    private Long seatId;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime createTime;
}
