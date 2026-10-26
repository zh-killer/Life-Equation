-- 排班补齐：为全部医生生成未来 30 天的门诊排班（2026-10-26 新增）
--
-- 背景：库里原有的 22 条排班停在 2026-07-23，导致今天打开小程序点「预约挂号」
--       必然报「该时段暂无排班」，挂号与取消挂号都没法演示。
-- 用法：演示前执行一次即可；脚本按 CURDATE() 计算日期，随时可重复执行。
--       唯一键 uk_doctor_date_slot 配合 INSERT IGNORE 保证不会插重。
--
-- ⚠️ 本机 mysql 命令行默认连 3308，Spring Boot 连 3306，执行前先 SELECT @@port 确认。
USE `life_equation`;

INSERT IGNORE INTO `doctor_schedule`
  (`doctor_id`, `schedule_date`, `time_slot`, `type`, `max_slots`, `booked_count`, `status`)
SELECT
  d.id,
  DATE_ADD(CURDATE(), INTERVAL offs.n DAY)                             AS schedule_date,
  sl.slot,
  CASE WHEN d.title LIKE '%副主任医师%' THEN '普通门诊'
       WHEN d.title LIKE '%主任医师%'   THEN '专家门诊'
       ELSE '普通门诊' END                                              AS type,
  CASE WHEN d.title LIKE '%副主任医师%' THEN 25
       WHEN d.title LIKE '%主任医师%'   THEN 20
       ELSE 30 END                                                      AS max_slots,
  0,
  1
FROM `doctor` d
CROSS JOIN (
  SELECT 0 AS n UNION ALL SELECT 1  UNION ALL SELECT 2  UNION ALL SELECT 3
  UNION ALL SELECT 4  UNION ALL SELECT 5  UNION ALL SELECT 6  UNION ALL SELECT 7
  UNION ALL SELECT 8  UNION ALL SELECT 9  UNION ALL SELECT 10 UNION ALL SELECT 11
  UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
  UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19
  UNION ALL SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23
  UNION ALL SELECT 24 UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27
  UNION ALL SELECT 28 UNION ALL SELECT 29
) offs
CROSS JOIN (
  SELECT '上午' AS slot UNION ALL SELECT '下午'
) sl
WHERE
  -- 每位医生一周出 2-3 个半天门诊，按医生ID分组错开，避免全部挤在同一天
  ( MOD(d.id, 5) = 0 AND WEEKDAY(DATE_ADD(CURDATE(), INTERVAL offs.n DAY)) IN (0, 2, 4) )
  OR ( MOD(d.id, 5) = 1 AND WEEKDAY(DATE_ADD(CURDATE(), INTERVAL offs.n DAY)) IN (1, 3, 5) )
  OR ( MOD(d.id, 5) = 2 AND WEEKDAY(DATE_ADD(CURDATE(), INTERVAL offs.n DAY)) IN (0, 3) )
  OR ( MOD(d.id, 5) = 3 AND WEEKDAY(DATE_ADD(CURDATE(), INTERVAL offs.n DAY)) IN (2, 5) )
  OR ( MOD(d.id, 5) = 4 AND WEEKDAY(DATE_ADD(CURDATE(), INTERVAL offs.n DAY)) IN (1, 4) );
