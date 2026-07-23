-- 为现有用户填充模拟身份证号（格式通过前端正则校验）
UPDATE `user` SET `id_card` = '110101199001010011', `real_name` = '系统管理员' WHERE `id` = 1;
UPDATE `user` SET `id_card` = '110101198505150042', `real_name` = '张医生'       WHERE `id` = 2;
UPDATE `user` SET `id_card` = '110101200003200053', `real_name` = '测试用户'     WHERE `id` = 3;
UPDATE `user` SET `id_card` = '310101198812100024', `real_name` = '李医生'       WHERE `id` = 4;
UPDATE `user` SET `id_card` = '320101199203080035', `real_name` = '王医生'       WHERE `id` = 5;
UPDATE `user` SET `id_card` = '440101198807220016', `real_name` = '赵医生'       WHERE `id` = 6;
UPDATE `user` SET `id_card` = '510101199105180027', `real_name` = '陈医生'       WHERE `id` = 7;
UPDATE `user` SET `id_card` = '330101198609140018', `real_name` = '刘医生'       WHERE `id` = 8;
