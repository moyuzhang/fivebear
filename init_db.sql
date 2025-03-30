-- 代理IP表
CREATE TABLE IF NOT EXISTS `proxy_ip` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ip` varchar(50) NOT NULL COMMENT 'IP地址',
  `port` int NOT NULL COMMENT '端口',
  `country` varchar(50) DEFAULT NULL COMMENT '国家',
  `location` varchar(100) DEFAULT NULL COMMENT '位置',
  `type` varchar(10) DEFAULT NULL COMMENT '类型',
  `anonymity` varchar(20) DEFAULT NULL COMMENT '匿名性',
  `available` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `last_validate_time` datetime DEFAULT NULL COMMENT '最后验证时间',
  `validate_count` int NOT NULL DEFAULT '0' COMMENT '验证次数',
  `available_count` int DEFAULT NULL COMMENT '可用次数',
  `unavailable_count` int DEFAULT NULL COMMENT '不可用次数',
  `response_time` bigint DEFAULT NULL COMMENT '响应时间',
  `request_time` bigint DEFAULT NULL COMMENT '请求时间',
  `use_time` bigint DEFAULT NULL COMMENT '使用时间',
  `available_rate` decimal(5,3) DEFAULT NULL COMMENT '可用率',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ip_port_type` (`ip`,`port`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理IP表';

-- IP池配置表
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

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account` varchar(50) NOT NULL COMMENT '账号',
  `password` varchar(60) NOT NULL COMMENT '密码',
  `parent_id` bigint DEFAULT NULL COMMENT '父级ID',
  `level` int DEFAULT NULL COMMENT '层级',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `role` varchar(20) DEFAULT 'USER' COMMENT '角色',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否激活',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `full_name` varchar(50) DEFAULT NULL COMMENT '全名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员用户，密码为 123456 的 MD5 加密值
INSERT INTO `user` (
  `account`, `password`, `parent_id`, `level`, `create_time`, 
  `role`, `is_active`, `last_login_time`, `full_name`, `email`
) VALUES (
  'admin', 'e10adc3949ba59abbe56e057f20f883e', 0, 1, NOW(),
  'ADMIN', 1, NOW(), 'Administrator', 'admin@example.com'
); 