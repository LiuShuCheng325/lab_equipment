-- ============================================
-- 实验室设备借用管理系统 - 数据库初始化脚本（v2）
-- 数据库: lab_equipment
-- MySQL 8.x
-- 说明：本脚本可重复执行（先 DROP 再 CREATE），会清空并重建演示数据
-- ============================================

CREATE DATABASE IF NOT EXISTS lab_equipment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE lab_equipment;

SET NAMES utf8mb4;

-- ============================================
-- 用户表
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '密码(SHA256加密)',
    `name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role` INT NOT NULL DEFAULT 0 COMMENT '角色: 0=普通学生, 1=管理员',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 设备表
-- ============================================
DROP TABLE IF EXISTS `equipment`;
CREATE TABLE `equipment` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    `eq_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
    `model` VARCHAR(100) DEFAULT NULL COMMENT '设备型号',
    `eq_type` VARCHAR(50) DEFAULT NULL COMMENT '设备类别',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '总库存',
    `available` INT NOT NULL DEFAULT 0 COMMENT '可用数量',
    `location` VARCHAR(200) DEFAULT NULL COMMENT '存放位置',
    `remark` TEXT DEFAULT NULL COMMENT '备注/描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0=正常, 1=维修中, 2=已报废',
    `img` VARCHAR(255) DEFAULT NULL COMMENT '设备图片路径',
    PRIMARY KEY (`id`),
    KEY `idx_eq_type` (`eq_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- ============================================
-- 借用记录表
-- status: 0=待审批, 1=已通过(借用中), 2=已驳回, 3=已归还, 4=已逾期
-- ============================================
DROP TABLE IF EXISTS `borrow_record`;
CREATE TABLE `borrow_record` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '借用记录ID',
    `user_id` INT NOT NULL COMMENT '借用用户ID',
    `equipment_id` INT NOT NULL COMMENT '设备ID',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '借用数量',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0=待审批, 1=已通过(借用中), 2=已驳回, 3=已归还, 4=已逾期',
    `borrow_time` DATETIME DEFAULT NULL COMMENT '借用时间',
    `expect_return_time` DATETIME DEFAULT NULL COMMENT '预计归还时间',
    `return_time` DATETIME DEFAULT NULL COMMENT '实际归还时间',
    `approver` VARCHAR(50) DEFAULT NULL COMMENT '审批人',
    `remark` TEXT DEFAULT NULL COMMENT '申请人备注(借用用途)',
    `audit_remark` VARCHAR(255) DEFAULT NULL COMMENT '审批备注(审批人填写，不覆盖申请人备注)',
    `return_condition` TINYINT DEFAULT NULL COMMENT '归还时设备状况: 0=完好, 1=损坏, 2=丢失',
    `remind_count` INT NOT NULL DEFAULT 0 COMMENT '已提醒次数',
    `last_remind_time` DATETIME DEFAULT NULL COMMENT '最近一次提醒时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_equipment_id` (`equipment_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expect_return` (`expect_return_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借用记录表';

-- ============================================
-- 报修记录表
-- status: 0=待处理, 1=处理中, 2=已完成, 3=已报废
-- ============================================
DROP TABLE IF EXISTS `repair_record`;
CREATE TABLE `repair_record` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '报修记录ID',
    `equipment_id` INT NOT NULL COMMENT '设备ID',
    `user_id` INT NOT NULL COMMENT '报修人ID',
    `description` TEXT NOT NULL COMMENT '故障描述',
    `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0=待处理, 1=处理中, 2=已完成, 3=已报废',
    `handler` VARCHAR(50) DEFAULT NULL COMMENT '处理人',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '处理备注',
    PRIMARY KEY (`id`),
    KEY `idx_equipment_id` (`equipment_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修记录表';

-- ============================================
-- 站内提醒表（逾期提醒 / 即将到期提醒 / 审批结果 / 系统通知）
-- type: 1=即将到期, 2=逾期, 3=审批结果, 4=系统
-- ============================================
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '提醒ID',
    `user_id` INT NOT NULL COMMENT '接收用户ID',
    `borrow_id` INT DEFAULT NULL COMMENT '关联借用记录ID(可为空)',
    `type` INT NOT NULL DEFAULT 4 COMMENT '类型: 1=即将到期, 2=逾期, 3=审批结果, 4=系统',
    `title` VARCHAR(200) NOT NULL COMMENT '提醒标题',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '提醒内容',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0=未读, 1=已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_read` (`user_id`, `is_read`),
    KEY `idx_borrow_type` (`borrow_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内提醒表';

-- ============================================
-- 初始化演示数据
-- ============================================

-- 所有演示账号密码统一为 123456（下方哈希即该密码的 SHA256）
-- SHA256('123456') = 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
INSERT INTO `user` (`username`, `password`, `name`, `phone`, `email`, `role`) VALUES
('admin',   '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '系统管理员', '13800000000', 'admin@lab.edu.cn', 1),
('student', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '张三', '13800000001', 'zhangsan@lab.edu.cn', 0),
('2024001', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '李四', '13800000002', 'lisi@lab.edu.cn', 0),
('2024002', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '王五', '13800000003', 'wangwu@lab.edu.cn', 0);

-- 演示设备数据（14 台，覆盖 7 个类别）
-- available 已按“借用中/逾期”记录扣减后的真实可用量设置
INSERT INTO `equipment` (`eq_name`, `model`, `eq_type`, `stock`, `available`, `location`, `remark`, `status`, `img`) VALUES
('光学显微镜',       'Olympus CX23',        '光学仪器',     10, 9,  '实验楼A栋301', '生物实验室常用光学显微镜', 0, NULL),
('电子天平',         'METTLER ME204',       '称量设备',     5,  4,  '实验楼A栋302', '精度0.1mg电子天平',       0, NULL),
('离心机',           'Eppendorf 5424R',     '离心设备',     3,  3,  '实验楼B栋201', '冷冻离心机',              0, NULL),
('PCR仪',            'Bio-Rad T100',        '分子生物设备', 2,  2,  '实验楼B栋205', '梯度PCR仪',               0, NULL),
('分光光度计',       'Shimadzu UV-1800',    '光学仪器',     4,  4,  '实验楼A栋303', '紫外可见分光光度计',      0, NULL),
('超净工作台',       '苏净 SW-CJ-1FD',      '无菌设备',     6,  6,  '实验楼B栋101', '双人单面垂直送风',        1, NULL),
('高压灭菌锅',       '博迅 YXQ-LS-50G',     '灭菌设备',     2,  0,  '实验楼B栋102', '立式压力蒸汽灭菌器',      2, NULL),
('数字示波器',       'Tektronix TBS1102B',  '电子测量设备', 8,  7,  '实验楼C栋101', '100MHz 双通道数字示波器', 0, NULL),
('数字万用表',       'Fluke 17B+',          '电子测量设备', 15, 15, '实验楼C栋102', '四位半手持万用表',        0, NULL),
('恒温磁力搅拌器',   'IKA RCT basic',       '通用工具',     10, 10, '实验楼A栋304', '带加热功能的磁力搅拌器',  0, NULL),
('电热鼓风干燥箱',   '上海一恒 DHG-9053A',  '灭菌设备',     3,  3,  '实验楼B栋103', '50L 恒温干燥箱',          0, NULL),
('高速冷冻离心机',   'Sigma 3-18KS',        '离心设备',     2,  1,  '实验楼B栋202', '最高18000rpm',            0, NULL),
('移液器套装',       'Eppendorf Research+', '分子生物设备', 12, 11, '实验楼B栋206', '0.5-10/20-200/100-1000ul', 0, NULL),
('实验室工具套装',   'STANLEY 65件',        '通用工具',     20, 19, '实验楼C栋103', '常用拆装与维修工具',      0, NULL);

-- 演示借用记录（覆盖 待审批 / 已通过 / 已驳回 / 已归还 / 已逾期 五种状态，含 2 条逾期）
INSERT INTO `borrow_record`
(`user_id`, `equipment_id`, `quantity`, `status`, `borrow_time`, `expect_return_time`, `return_time`, `approver`, `remark`, `audit_remark`, `return_condition`)
VALUES
-- 借用中（正常，尚未到期）
(2, 1,  1, 1, DATE_SUB(NOW(), INTERVAL 3 DAY),  DATE_ADD(NOW(), INTERVAL 4 DAY),  NULL, '系统管理员', '课程实验借用', '同意借用', NULL),
-- 借用中（即将到期：2 天后到期，用于“即将到期提醒”演示）
(4, 12, 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY),  DATE_ADD(NOW(), INTERVAL 2 DAY),  NULL, '系统管理员', '样品分离实验', '同意借用', NULL),
-- 待审批
(2, 3,  1, 0, NOW(),                               DATE_ADD(NOW(), INTERVAL 7 DAY), NULL, NULL, '毕业论文实验', NULL, NULL),
(3, 4,  1, 0, NOW(),                               DATE_ADD(NOW(), INTERVAL 5 DAY), NULL, NULL, '创新项目测试', NULL, NULL),
-- 已逾期（2 条）
(3, 2,  1, 4, DATE_SUB(NOW(), INTERVAL 10 DAY),   DATE_SUB(NOW(), INTERVAL 3 DAY),  NULL, '系统管理员', '课外科研借用', '同意借用，请按时归还', NULL),
(4, 8,  1, 4, DATE_SUB(NOW(), INTERVAL 12 DAY),   DATE_SUB(NOW(), INTERVAL 5 DAY),  NULL, '系统管理员', '电子设计竞赛', '同意借用', NULL),
-- 已归还（其中一条为“逾期后归还”，用于逾期天数统计演示）
(2, 5,  1, 3, DATE_SUB(NOW(), INTERVAL 20 DAY),   DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), '系统管理员', '课程实验借用', '同意借用', 0),
(3, 10, 2, 3, DATE_SUB(NOW(), INTERVAL 25 DAY),   DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY), '系统管理员', '化学合成实验', '同意借用', 1),
-- student 账号的逾期记录与即将到期记录（用于首页/提醒中心演示）
(2, 13, 1, 4, DATE_SUB(NOW(), INTERVAL 9 DAY),    DATE_SUB(NOW(), INTERVAL 2 DAY),  NULL, '系统管理员', '开放实验借用', '同意借用，请按时归还', NULL),
(2, 14, 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY),     DATE_ADD(NOW(), INTERVAL 2 DAY),  NULL, '系统管理员', '工具借用',     '同意借用', NULL),
-- 已驳回
(4, 9,  1, 2, DATE_SUB(NOW(), INTERVAL 6 DAY),    DATE_ADD(NOW(), INTERVAL 1 DAY),  NULL, '系统管理员', '电路调试', '设备紧张，暂不借出', NULL);

-- 演示报修记录
INSERT INTO `repair_record`
(`equipment_id`, `user_id`, `description`, `apply_time`, `complete_time`, `status`, `handler`, `remark`)
VALUES
(6,  2, '超净工作台风机异响，需检查更换', DATE_SUB(NOW(), INTERVAL 8 DAY),  NULL, 1, '系统管理员', '已联系厂家维修'),
(7,  2, '高压灭菌锅密封圈老化，无法保压', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), 3, '系统管理员', '设备已报废，需采购新设备'),
(11, 3, '干燥箱温控显示异常，温度偏高',   DATE_SUB(NOW(), INTERVAL 2 DAY),  NULL, 0, NULL, NULL);

-- 演示站内提醒（student 用户：1 条逾期提醒 + 1 条即将到期提醒）
-- 使用子查询按“用户名 + 设备名”定位借用记录，避免硬编码自增ID
INSERT INTO `notice` (`user_id`, `borrow_id`, `type`, `title`, `content`, `is_read`)
SELECT b.user_id, b.id, 2, '借用逾期提醒',
       CONCAT('您借用的「', e.eq_name, '」已超过预计归还时间，请尽快归还！'), 0
FROM `borrow_record` b
         JOIN `equipment` e ON e.id = b.equipment_id
         JOIN `user` u ON u.id = b.user_id
WHERE u.username = 'student'
  AND b.status = 4
  AND e.eq_name = '移液器套装'
LIMIT 1;

INSERT INTO `notice` (`user_id`, `borrow_id`, `type`, `title`, `content`, `is_read`)
SELECT b.user_id, b.id, 1, '即将到期提醒',
       CONCAT('您借用的「', e.eq_name, '」将在 3 天内到期，请提前安排归还。'), 0
FROM `borrow_record` b
         JOIN `equipment` e ON e.id = b.equipment_id
         JOIN `user` u ON u.id = b.user_id
WHERE u.username = 'student'
  AND b.status = 1
  AND e.eq_name = '实验室工具套装'
LIMIT 1;
