-- 电子票表
CREATE TABLE IF NOT EXISTS `eticket` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `show_id` BIGINT NOT NULL COMMENT '演出ID',
  `seat_id` BIGINT NOT NULL COMMENT '座位ID',
  `ticket_code` VARCHAR(64) NOT NULL UNIQUE COMMENT '电子票码（唯一随机码）',
  `status` TINYINT DEFAULT 0 COMMENT '验票状态：0-未使用 1-已使用 2-已退款',
  `used_time` DATETIME DEFAULT NULL COMMENT '验票时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticket_code` (`ticket_code`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子票表';
