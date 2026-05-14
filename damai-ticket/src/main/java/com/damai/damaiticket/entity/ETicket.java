package com.damai.damaiticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 电子票实体类
 */
@Data
@TableName("eticket")
public class ETicket {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 演出ID
     */
    private Long showId;

    /**
     * 座位ID
     */
    private Long seatId;

    /**
     * 电子票码（唯一随机码）
     */
    private String ticketCode;

    /**
     * 验票状态：0-未使用 1-已使用 2-已退款
     */
    private Integer status;

    /**
     * 验票时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime usedTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
