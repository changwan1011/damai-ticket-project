package com.damai.damaiticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("seat")
public class Seat {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long showId;

    private String seatNumber;

    /**
     * 0 可选
     * 1 已售
     * 2 已锁定
     */
    private Integer status;

    // ✅ 座位价格（新增）
    private BigDecimal price;
}
