CREATE TABLE `t_user_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) DEFAULT NULL COMMENT '用户账号',
  `user_nike` varchar(255) DEFAULT NULL COMMENT '昵称',
  `age` int(11) DEFAULT NULL COMMENT '年龄',
  `version` int(11) DEFAULT NULL,
  `test_column` varchar(255) DEFAULT NULL,
  `is_deleted` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
