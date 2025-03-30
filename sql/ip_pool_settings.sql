CREATE TABLE IF NOT EXISTS `ip_pool_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `thread_pool_size` int NOT NULL DEFAULT '5' COMMENT '线程池大小',
  `scheduler_pool_size` int NOT NULL DEFAULT '30' COMMENT '调度器线程池大小',
  `initial_delay` bigint NOT NULL DEFAULT '20' COMMENT '初始延迟时间(秒)',
  `period` bigint NOT NULL DEFAULT '100' COMMENT '执行间隔(秒)',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `expire_time` bigint NOT NULL DEFAULT '180' COMMENT 'Redis过期时间(秒)',
  `list_key` varchar(50) NOT NULL DEFAULT 'proxy:ip:list' COMMENT 'Redis列表键名',
  `set_key` varchar(50) NOT NULL DEFAULT 'proxy:ip:set' COMMENT 'Redis集合键名',
  `max_validate_count` int NOT NULL DEFAULT '10' COMMENT '最大验证次数',
  `min_available_rate` int NOT NULL DEFAULT '50' COMMENT '最小可用率(%)',
  `validate_interval` bigint NOT NULL DEFAULT '300' COMMENT '验证间隔(秒)',
  `timeout` int NOT NULL DEFAULT '5000' COMMENT '超时时间(毫秒)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP池配置表';

-- 插入默认配置
INSERT INTO `ip_pool_settings` (
  `thread_pool_size`, `scheduler_pool_size`, `initial_delay`, `period`, 
  `enabled`, `expire_time`, `list_key`, `set_key`, 
  `max_validate_count`, `min_available_rate`, `validate_interval`, `timeout`
) VALUES (
  5, 30, 20, 100,
  1, 180, 'proxy:ip:list', 'proxy:ip:set',
  10, 50, 300, 5000
); 