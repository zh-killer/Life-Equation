-- 挂号费按职称重新定档（2026-10-26）
-- 原数据只有 10 / 20 / 30 三档，且与职称无关（出现「主任医师 10 元、住院医师 30 元」的倒挂）。
-- 现按三甲医院门诊诊查费的一般规律定档：
--   住院医师        12
--   副主任医师      18 - 25
--   主任医师        30 - 35
--   主任医师+教授/副教授  38 - 45
--   学科带头人（教授+硕导） 50
--
-- ⚠️ 本机 mysql 命令行默认连 3308，Spring Boot 连 3306，执行前先 SELECT @@port 确认。
USE `life_equation`;

UPDATE `doctor` SET `consultation_fee` = 30.00 WHERE `id` = 1;  -- 曹明艳 主任医师
UPDATE `doctor` SET `consultation_fee` = 20.00 WHERE `id` = 2;  -- 何劲松 副主任医师
UPDATE `doctor` SET `consultation_fee` = 32.00 WHERE `id` = 3;  -- 曹锡文 主任医师
UPDATE `doctor` SET `consultation_fee` = 30.00 WHERE `id` = 4;  -- 唐梅艳 主任医师
UPDATE `doctor` SET `consultation_fee` = 50.00 WHERE `id` = 5;  -- 胡久叶 主任医师、教授、硕士研究生导师
UPDATE `doctor` SET `consultation_fee` = 35.00 WHERE `id` = 6;  -- 邝爱玲 主任医师
UPDATE `doctor` SET `consultation_fee` = 30.00 WHERE `id` = 7;  -- 秦牧   主任医师
UPDATE `doctor` SET `consultation_fee` = 18.00 WHERE `id` = 8;  -- 欧丽娜 副主任医师
UPDATE `doctor` SET `consultation_fee` = 32.00 WHERE `id` = 9;  -- 唐建生 主任医师
UPDATE `doctor` SET `consultation_fee` = 30.00 WHERE `id` = 10; -- 陈筱涛 主任医师
UPDATE `doctor` SET `consultation_fee` = 35.00 WHERE `id` = 11; -- 李之茂 主任医师
UPDATE `doctor` SET `consultation_fee` = 30.00 WHERE `id` = 12; -- 邓忠   主任医师
UPDATE `doctor` SET `consultation_fee` = 38.00 WHERE `id` = 13; -- 刘松涛 主任医师、副教授
UPDATE `doctor` SET `consultation_fee` = 22.00 WHERE `id` = 14; -- 黄永   副主任医师
UPDATE `doctor` SET `consultation_fee` = 32.00 WHERE `id` = 15; -- 李尧学 主任医师
UPDATE `doctor` SET `consultation_fee` = 38.00 WHERE `id` = 16; -- 黄勉芳 主任医师（口腔科）
UPDATE `doctor` SET `consultation_fee` = 45.00 WHERE `id` = 17; -- 李国亮 主任医师、教授
UPDATE `doctor` SET `consultation_fee` = 45.00 WHERE `id` = 18; -- 黄存军 主任医师、教授
UPDATE `doctor` SET `consultation_fee` = 25.00 WHERE `id` = 19; -- 张明   副主任医师、副教授
UPDATE `doctor` SET `consultation_fee` = 20.00 WHERE `id` = 20; -- 戴洪波 副主任医师
UPDATE `doctor` SET `consultation_fee` = 38.00 WHERE `id` = 21; -- 李仁喜 主任医师、副教授
UPDATE `doctor` SET `consultation_fee` = 22.00 WHERE `id` = 22; -- 史巧静 副主任医师
UPDATE `doctor` SET `consultation_fee` = 32.00 WHERE `id` = 23; -- 曹烈权 主任医师
UPDATE `doctor` SET `consultation_fee` = 25.00 WHERE `id` = 24; -- 罗红梅 副主任医师
UPDATE `doctor` SET `consultation_fee` = 12.00 WHERE `id` = 25; -- 王炯   医师
UPDATE `doctor` SET `consultation_fee` = 30.00 WHERE `id` = 26; -- 廖昆   主任医师
UPDATE `doctor` SET `consultation_fee` = 45.00 WHERE `id` = 27; -- 李启秀 主任医师、教授
