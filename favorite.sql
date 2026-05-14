-- ----------------------------
-- 收藏表
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `show_id` BIGINT NOT NULL COMMENT '演出ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_show` (`user_id`, `show_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_show_id` (`show_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- ----------------------------
-- 已有数据操作
-- ----------------------------
-- 从现有数据库导出时包含此表即可
