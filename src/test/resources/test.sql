create database `veasion_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

use `veasion_db`;

-- 学生表
CREATE TABLE `t_student` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `sno` varchar(50) NOT NULL COMMENT '学号',
  `name` varchar(100) NOT NULL COMMENT '学生姓名',
  `class_id` bigint(20) DEFAULT NULL COMMENT '班级',
  `sex` tinyint(2) NULL DEFAULT NULL COMMENT '1-男，2-女',
  `age` int(11) DEFAULT NULL COMMENT '年龄',
  `desc` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT '0',
  `is_deleted` bigint(20) DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE `idx_sno` (`sno`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生';

-- 班级表
CREATE TABLE `t_classes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `class_name` varchar(255) NOT NULL COMMENT '班级名称',
  `master_tno` varchar(50) NOT NULL COMMENT '班主任编号',
  `version` int(11) DEFAULT '0',
  `is_deleted` bigint(20) DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_class_name` (`class_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级';

-- 教师表
CREATE TABLE `t_teacher` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tno` varchar(50) NOT NULL COMMENT '教师编号',
  `name` varchar(100) NOT NULL COMMENT '教师名称',
  `sex` tinyint(2) NULL DEFAULT NULL COMMENT '1-男，2-女',
  `work_years` int(11) NULL DEFAULT NULL COMMENT '工作年限',
  `competent` varchar(30) NOT NULL COMMENT '职称',
  `department` varchar(30) NOT NULL COMMENT '部门',
  `version` int(11) DEFAULT '0',
  `is_deleted` bigint(20) DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE `idx_tno` (`tno`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师';

-- 课程表
CREATE TABLE `t_course` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `course_name` varchar(255) NOT NULL COMMENT '课程名称',
  `class_id` bigint(20) DEFAULT NULL COMMENT '班级',
  `tno` varchar(50) NOT NULL COMMENT '教师编号',
  `version` int(11) DEFAULT '0',
  `is_deleted` bigint(20) DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程';

-- 分数表
CREATE TABLE `t_score` (
  `sno` varchar(50) NOT NULL COMMENT '学号',
  `course_id` bigint(20) NOT NULL COMMENT '课程',
  `score` int(11) DEFAULT '0' COMMENT '分数',
  `is_deleted` bigint(20) DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_sno_course` (`sno`,`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分数';

-- 初始化数据
INSERT INTO `t_teacher`(`id`, `tno`, `name`, `sex`, `work_years`, `competent`, `department`, `version`, `is_deleted`, `create_time`, `update_time`) VALUES
(1, 't001', '王老师', 2, 10, '教导主任', '教研部', 0, 0, NOW(), NOW()),
(2, 't002', '张老师', 1, 5, '班主任', '教研部', 0, 0, NOW(), NOW()),
(3, 't003', '刘老师', 1, 3, '普通教师', '教研部', 0, 0, NOW(), NOW()),
(4, 't004', '罗老师', 1, 4, '班主任', '教研部', 0, 0, NOW(), NOW());

INSERT INTO `t_classes`(`id`, `class_name`, `master_tno`, `version`, `is_deleted`, `create_time`, `update_time`) VALUES
(1, '初一一班', 't004', 0, 0, NOW(), NOW()),
(2, '初二二班', 't002', 0, 0, NOW(), NOW()),
(3, '初三三班', 't001', 0, 0, NOW(), NOW());

INSERT INTO `t_course`(`id`, `course_name`, `class_id`, `tno`, `version`, `is_deleted`, `create_time`, `update_time`) VALUES
(1, '语文课', 1, 't001', 0, 0, NOW(), NOW()),
(2, '数学课', 1, 't003', 0, 0, NOW(), NOW()),
(3, '计算机课', 1, 't002', 0, 0, NOW(), NOW()),
(4, '语文课', 2, 't001', 0, 0, NOW(), NOW()),
(5, '数学课', 2, 't003', 0, 0, NOW(), NOW()),
(6, '语文课', 3, 't002', 0, 0, NOW(), NOW()),
(7, '数学课', 3, 't003', 0, 0, NOW(), NOW());

INSERT INTO `t_student`(`id`, `sno`, `name`, `class_id`, `sex`, `age`, `desc`, `version`, `is_deleted`, `create_time`, `update_time`) VALUES
(1, 's001', '熊大', 1, 1, 18, '体型庞大', 0, 0, NOW(), NOW()),
(2, 's002', '熊二', 1, 1, 16, NULL, 0, 0, NOW(), NOW()),
(3, 's003', '张三', 2, 1, 17, NULL, 0, 0, NOW(), NOW()),
(4, 's004', '李四', 2, 1, 18, NULL, 0, 0, NOW(), NOW()),
(5, 's005', '王五', 3, 1, 19, NULL, 0, 0, NOW(), NOW()),
(6, 's006', '赵六', 3, 1, 20, NULL, 0, 0, NOW(), NOW()),
(7, 's007', '孙七', 1, 1, 15, NULL, 0, 0, NOW(), NOW()),
(8, 's008', '周八', 1, 1, 14, NULL, 0, 0, NOW(), NOW()),
(9, 's009', '吴九', 2, 1, 15, NULL, 0, 0, NOW(), NOW()),
(10, 's010', '郑十', 2, 1, 15, NULL, 0, 0, NOW(), NOW()),
(11, 's011', '小明', 3, 1, 16, '真伤、回血', 0, 0, NOW(), NOW()),
(12, 's012', '小爱', 1, 2, 15, '人工智能二哈', 0, 0, NOW(), NOW());

INSERT INTO `t_score`(`sno`, `course_id`, `score`, `is_deleted`, `create_time`, `update_time`) VALUES
('s001', 1, 30, 0, NOW(), NOW()),
('s001', 2, 24, 0, NOW(), NOW()),
('s001', 3, 20, 0, NOW(), NOW()),
('s002', 1, 40, 0, NOW(), NOW()),
('s002', 2, 35, 0, NOW(), NOW()),
('s002', 3, 48, 0, NOW(), NOW()),
('s003', 4, 69, 0, NOW(), NOW()),
('s003', 5, 70, 0, NOW(), NOW()),
('s004', 4, 62, 0, NOW(), NOW()),
('s004', 5, 42, 0, NOW(), NOW()),
('s005', 6, 70, 0, NOW(), NOW()),
('s005', 7, 100, 0, NOW(), NOW()),
('s006', 6, 80, 0, NOW(), NOW()),
('s006', 7, 90, 0, NOW(), NOW()),
('s007', 1, 78, 0, NOW(), NOW()),
('s007', 2, 69, 0, NOW(), NOW()),
('s007', 3, 92, 0, NOW(), NOW()),
('s008', 1, 61, 0, NOW(), NOW()),
('s008', 2, 34, 0, NOW(), NOW()),
('s008', 3, 80, 0, NOW(), NOW()),
('s009', 4, 50, 0, NOW(), NOW()),
('s009', 5, 61, 0, NOW(), NOW()),
('s010', 4, 76, 0, NOW(), NOW()),
('s010', 5, 43, 0, NOW(), NOW()),
('s011', 6, 56, 0, NOW(), NOW()),
('s011', 7, 67, 0, NOW(), NOW()),
('s012', 1, 100, 0, NOW(), NOW()),
('s012', 2, 100, 0, NOW(), NOW()),
('s012', 3, 100, 0, NOW(), NOW());

