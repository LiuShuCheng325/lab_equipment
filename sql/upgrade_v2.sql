-- ============================================
-- 实验室设备借用管理系统 - 增量升级脚本 v2
-- 适用：已经在跑旧版 lab_equipment.sql 的数据库
-- 特性：幂等，可重复执行（建表/加列/插数据前均做存在性判断）
-- 执行：mysql -uroot -p lab_equipment < sql/upgrade_v2.sql
-- ============================================

USE lab_equipment;

SET NAMES utf8mb4;

-- 幂等加列辅助存储过程：目标列不存在时才 ALTER TABLE ADD COLUMN
DROP PROCEDURE IF EXISTS `add_column_if_missing`;
DELIMITER $$
CREATE PROCEDURE `add_column_if_missing`(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_definition VARCHAR(512)
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO v_exists
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND COLUMN_NAME = p_column;
    IF v_exists = 0 THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('[upgrade_v2] 已新增列 ', p_table, '.', p_column) AS `log`;
    ELSE
        SELECT CONCAT('[upgrade_v2] 列已存在，跳过 ', p_table, '.', p_column) AS `log`;
    END IF;
END$$
DELIMITER ;

-- --------------------------------------------
-- 1. borrow_record 结构升级
-- --------------------------------------------
CALL add_column_if_missing('borrow_record', 'audit_remark',
    "VARCHAR(255) DEFAULT NULL COMMENT '审批备注(审批人填写，不覆盖申请人remark)' AFTER `remark`");
CALL add_column_if_missing('borrow_record', 'return_condition',
    "TINYINT DEFAULT NULL COMMENT '归还时设备状况: 0=完好, 1=损坏, 2=丢失' AFTER `audit_remark`");
CALL add_column_if_missing('borrow_record', 'remind_count',
    "INT NOT NULL DEFAULT 0 COMMENT '已提醒次数' AFTER `return_condition`");
CALL add_column_if_missing('borrow_record', 'last_remind_time',
    "DATETIME DEFAULT NULL COMMENT '最近一次提醒时间' AFTER `remind_count`");

-- 补充索引（MySQL 无 ADD INDEX IF NOT EXISTS，用 information_schema 判断后动态执行）
DROP PROCEDURE IF EXISTS `add_index_if_missing`;
DELIMITER $$
CREATE PROCEDURE `add_index_if_missing`(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_definition VARCHAR(512)
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO v_exists
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND INDEX_NAME = p_index;
    IF v_exists = 0 THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('[upgrade_v2] 已新增索引 ', p_table, '.', p_index) AS `log`;
    ELSE
        SELECT CONCAT('[upgrade_v2] 索引已存在，跳过 ', p_table, '.', p_index) AS `log`;
    END IF;
END$$
DELIMITER ;

CALL add_index_if_missing('borrow_record', 'idx_expect_return', 'KEY `idx_expect_return` (`expect_return_time`)');
CALL add_index_if_missing('equipment', 'idx_eq_type', 'KEY `idx_eq_type` (`eq_type`)');
CALL add_index_if_missing('equipment', 'idx_status', 'KEY `idx_status` (`status`)');

-- --------------------------------------------
-- 2. 新建站内提醒表 notice
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `notice` (
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

-- --------------------------------------------
-- 3. 数据修正：把已过期的“借用中”记录落库为“已逾期”(status=4)
-- ------------------------------------------------------------------
-- 说明：旧版本只在 Java 里按时间算 overdueDays，borrow_record.status 永远是 1，
--       无法统计与筛选。这里把逾期状态真正落库。
UPDATE `borrow_record`
SET `status` = 4
WHERE `status` = 1
  AND `expect_return_time` IS NOT NULL
  AND `expect_return_time` < NOW();

-- --------------------------------------------
-- 4. 补充演示账号（如不存在则插入，密码 123456）
-- --------------------------------------------
INSERT INTO `user` (`username`, `password`, `name`, `phone`, `email`, `role`)
SELECT '2024001', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '李四', '13800000002', 'lisi@lab.edu.cn', 0
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = '2024001');

INSERT INTO `user` (`username`, `password`, `name`, `phone`, `email`, `role`)
SELECT '2024002', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '王五', '13800000003', 'wangwu@lab.edu.cn', 0
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = '2024002');

-- --------------------------------------------
-- 5. 预置演示提醒（仅当 student 用户还没有提醒时插入，避免重复）
-- --------------------------------------------
INSERT INTO `notice` (`user_id`, `type`, `title`, `content`, `is_read`)
SELECT u.id, 2, '借用逾期提醒', '您借用的设备已超过预计归还时间，请尽快归还，逾期记录会影响后续借用审批！', 0
FROM `user` u
WHERE u.username = 'student'
  AND NOT EXISTS (SELECT 1 FROM `notice` n WHERE n.user_id = u.id);

INSERT INTO `notice` (`user_id`, `type`, `title`, `content`, `is_read`, `create_time`)
SELECT u.id, 1, '即将到期提醒', '您借用的设备将在 3 天内到期，请提前安排归还或申请续借。', 0, DATE_ADD(NOW(), INTERVAL 1 SECOND)
FROM `user` u
WHERE u.username = 'student'
  AND (SELECT COUNT(1) FROM `notice` n WHERE n.user_id = u.id) < 2;

-- 清理辅助存储过程
DROP PROCEDURE IF EXISTS `add_column_if_missing`;
DROP PROCEDURE IF EXISTS `add_index_if_missing`;

SELECT '[upgrade_v2] 升级完成' AS `log`;
