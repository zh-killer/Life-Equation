-- ============================================================
-- 生命方程式 - MySQL 初始化脚本
-- 包含所有业务表 + RBAC 权限表 + 种子数据
-- ============================================================

CREATE DATABASE IF NOT EXISTS life_equation
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE life_equation;

-- ============================================================
-- 核心业务表
-- ============================================================

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色标识',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `avatar_url` VARCHAR(500) COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    `birth_date` DATE COMMENT '出生日期',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `hospital` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '医院名称',
    `address` VARCHAR(255) COMMENT '地址',
    `city` VARCHAR(50) COMMENT '城市',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `level` VARCHAR(20) COMMENT '等级(三甲/二甲等)',
    `introduction` TEXT COMMENT '医院简介',
    `departments` VARCHAR(500) COMMENT '科室列表',
    `image_url` VARCHAR(500) COMMENT '图片URL',
    `business_hours` VARCHAR(100) COMMENT '营业时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医院表';

CREATE TABLE IF NOT EXISTS `doctor` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT COMMENT '关联用户ID',
    `hospital_id` BIGINT COMMENT '所属医院ID',
    `name` VARCHAR(50) NOT NULL COMMENT '医生姓名',
    `title` VARCHAR(50) COMMENT '职称',
    `specialty` VARCHAR(255) COMMENT '专长',
    `department` VARCHAR(50) COMMENT '科室',
    `introduction` TEXT COMMENT '简介',
    `avatar_url` VARCHAR(500) COMMENT '头像URL',
    `consultation_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '咨询费',
    `is_available` TINYINT DEFAULT 1 COMMENT '是否接诊',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生表';

CREATE TABLE IF NOT EXISTS `doctor_schedule` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
    `schedule_date` DATE NOT NULL COMMENT '排班日期',
    `time_slot` VARCHAR(20) NOT NULL COMMENT '时间段(am/pm)',
    `type` VARCHAR(20) DEFAULT 'normal' COMMENT '门诊类型',
    `max_slots` INT DEFAULT 20 COMMENT '最大号源数',
    `booked_count` INT DEFAULT 0 COMMENT '已预约数',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生排班表';

CREATE TABLE IF NOT EXISTS `appointment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `doctor_id` BIGINT COMMENT '医生ID',
    `hospital_id` BIGINT COMMENT '医院ID',
    `appointment_date` DATE COMMENT '预约日期',
    `time_slot` VARCHAR(20) COMMENT '时间段',
    `symptom_desc` VARCHAR(500) COMMENT '症状描述',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态(pending/confirmed/cancelled/completed)',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约挂号表';

CREATE TABLE IF NOT EXISTS `consultation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `doctor_id` BIGINT COMMENT '医生ID',
    `question` TEXT COMMENT '咨询问题',
    `answer` TEXT COMMENT '医生回复',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态',
    `answered_at` DATETIME COMMENT '回复时间',
    `is_public` TINYINT DEFAULT 0 COMMENT '是否公开',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线咨询表';

CREATE TABLE IF NOT EXISTS `article` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` LONGTEXT COMMENT '内容(富文本)',
    `summary` VARCHAR(500) COMMENT '摘要',
    `category` VARCHAR(50) COMMENT '分类',
    `cover_image` VARCHAR(500) COMMENT '封面图',
    `author_id` BIGINT COMMENT '作者ID',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `status` VARCHAR(20) DEFAULT 'draft' COMMENT '状态',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶',
    `publish_time` DATETIME COMMENT '发布时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康资讯表';

CREATE TABLE IF NOT EXISTS `disease` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '疾病名称',
    `category` VARCHAR(50) COMMENT '分类',
    `symptoms` TEXT COMMENT '症状',
    `description` TEXT COMMENT '描述',
    `cause` TEXT COMMENT '病因',
    `treatment` TEXT COMMENT '治疗方法',
    `prevention` TEXT COMMENT '预防措施',
    `department` VARCHAR(50) COMMENT '所属科室',
    `is_common` TINYINT DEFAULT 0 COMMENT '是否常见病',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疾病百科表';

CREATE TABLE IF NOT EXISTS `drug` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '药品名称',
    `trade_name` VARCHAR(100) COMMENT '商品名',
    `category` VARCHAR(50) COMMENT '分类',
    `indications` TEXT COMMENT '适应症',
    `usage_dosage` TEXT COMMENT '用法用量',
    `contraindications` TEXT COMMENT '禁忌',
    `adverse_reactions` TEXT COMMENT '不良反应',
    `precautions` TEXT COMMENT '注意事项',
    `image_url` VARCHAR(500) COMMENT '图片URL',
    `is_otc` TINYINT DEFAULT 0 COMMENT '是否OTC',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品表';

CREATE TABLE IF NOT EXISTS `pharmacy` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '药店名称',
    `address` VARCHAR(255) COMMENT '地址',
    `city` VARCHAR(50) COMMENT '城市',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `latitude` DECIMAL(10,7) COMMENT '纬度',
    `longitude` DECIMAL(10,7) COMMENT '经度',
    `business_hours` VARCHAR(100) COMMENT '营业时间',
    `is_24h` TINYINT DEFAULT 0 COMMENT '是否24小时营业',
    `image_url` VARCHAR(500) COMMENT '图片URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药店表';

CREATE TABLE IF NOT EXISTS `health_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `record_type` VARCHAR(50) COMMENT '记录类型',
    `record_title` VARCHAR(200) COMMENT '标题',
    `record_content` TEXT COMMENT '内容',
    `file_url` VARCHAR(500) COMMENT '附件URL',
    `record_date` DATE COMMENT '记录日期',
    `hospital_name` VARCHAR(100) COMMENT '医院名称',
    `doctor_name` VARCHAR(50) COMMENT '医生姓名',
    `is_public` TINYINT DEFAULT 0 COMMENT '是否公开',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康档案表';

CREATE TABLE IF NOT EXISTS `ai_consultation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT COMMENT '用户ID',
    `symptoms_input` TEXT COMMENT '症状输入',
    `ai_analysis` TEXT COMMENT 'AI分析结果',
    `suggested_department` VARCHAR(50) COMMENT '建议科室',
    `risk_level` VARCHAR(20) COMMENT '风险等级',
    `health_advice` TEXT COMMENT '健康建议',
    `related_diseases` VARCHAR(500) COMMENT '可能相关疾病',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI导诊记录表';

CREATE TABLE IF NOT EXISTS `ai_health_advice` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT COMMENT '用户ID',
    `age` INT COMMENT '年龄',
    `height` DECIMAL(5,2) COMMENT '身高(cm)',
    `weight` DECIMAL(5,2) COMMENT '体重(kg)',
    `gender` VARCHAR(10) COMMENT '性别',
    `lifestyle` VARCHAR(500) COMMENT '生活方式',
    `medical_history` TEXT COMMENT '病史',
    `concerns` TEXT COMMENT '健康关注点',
    `ai_advice` TEXT COMMENT 'AI建议',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI健康建议表';

CREATE TABLE IF NOT EXISTS `ai_report_interpretation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT COMMENT '用户ID',
    `report_type` VARCHAR(50) COMMENT '报告类型',
    `image_url` VARCHAR(500) COMMENT '报告图片URL',
    `ocr_text` TEXT COMMENT 'OCR识别文本',
    `ai_interpretation` TEXT COMMENT 'AI解读',
    `key_findings` TEXT COMMENT '关键发现',
    `risk_assessment` TEXT COMMENT '风险评估',
    `recommendations` TEXT COMMENT '建议',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI报告解读表';

-- ============================================================
-- RBAC 权限表
-- ============================================================

CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    `description` VARCHAR(255) COMMENT '角色描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限名称',
    `resource` VARCHAR(200) COMMENT '资源路径',
    `description` VARCHAR(255) COMMENT '权限描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE IF NOT EXISTS `user_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- ============================================================
-- 种子数据
-- ============================================================

INSERT IGNORE INTO `role` (`name`, `description`) VALUES
    ('USER', '普通用户'),
    ('DOCTOR', '医生'),
    ('ADMIN', '系统管理员');

INSERT IGNORE INTO `permission` (`name`, `resource`, `description`) VALUES
    ('user:view', '/api/admin/users', '查看用户列表'),
    ('user:update', '/api/admin/users/*', '修改用户信息'),
    ('user:delete', '/api/admin/users/*', '删除用户'),
    ('doctor:schedule', '/api/doctor/schedule/**', '排班管理'),
    ('doctor:patient', '/api/doctor/patient/**', '查看患者病历'),
    ('consultation:create', '/api/consultations', '创建咨询'),
    ('consultation:reply', '/api/doctor/reply-consultation/**', '回复咨询');

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
    SELECT r.id, p.id FROM `role` r, `permission` p
    WHERE r.name = 'ADMIN';

-- ============================================================
-- 测试账号数据
-- 密码统一为 123456（BCrypt加密）
-- ============================================================

INSERT IGNORE INTO `user` (`id`, `username`, `password`, `real_name`, `role`, `phone`, `status`) VALUES
(1, 'admin',   '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '系统管理员', 'ADMIN',  '13800000000', 1),
(2, 'doctor1', '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '张医生',     'DOCTOR', '13800000001', 1),
(3, 'user1',   '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '测试用户',   'USER',   '13800000002', 1),
(4, 'doctor2', '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '李医生',     'DOCTOR', '13800000003', 1),
(5, 'doctor3', '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '王医生',     'DOCTOR', '13800000004', 1),
(6, 'doctor4', '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '赵医生',     'DOCTOR', '13800000005', 1),
(7, 'doctor5', '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '陈医生',     'DOCTOR', '13800000006', 1),
(8, 'doctor6', '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '刘医生',     'DOCTOR', '13800000007', 1);

INSERT IGNORE INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 3),   -- admin → ADMIN角色
(2, 2),   -- doctor1 → DOCTOR角色
(3, 1),   -- user1 → USER角色
(4, 2),   -- doctor2 → DOCTOR角色
(5, 2),   -- doctor3 → DOCTOR角色
(6, 2),   -- doctor4 → DOCTOR角色
(7, 2),   -- doctor5 → DOCTOR角色
(8, 2);   -- doctor6 → DOCTOR角色

INSERT IGNORE INTO `hospital` (`id`, `name`, `address`, `city`, `phone`, `level`, `introduction`, `departments`, `status`) VALUES
(1, '生命方程式综合医院', '北京市海淀区中关村大街1号', '北京', '010-12345678', '三甲',
 '生命方程式综合医院始建于1958年，是一所集医疗、教学、科研、预防保健于一体的综合性三级甲等医院，' '国家卫生健康委直属重点医院，北京大学临床教学基地，国家住院医师规范化培训示范基地。' '医院占地面积15.6万平方米，建筑面积42万平方米，编制床位2200张，实际开放床位2600张。' '2024年门急诊量突破350万人次，年出院病人12万人次，年手术量8.6万台次，综合实力位居全国前列。' '医院设有临床科室52个、医技科室16个，其中国家临床重点专科12个（心血管内科、神经内科、骨科、普外科、妇产科、肿瘤科、' '呼吸与危重症医学科、肾内科、消化内科、内分泌科、眼科、急诊医学科），省级重点学科18个。' '配备有PET-CT、3.0T核磁共振（MRI）、256排螺旋CT、达芬奇手术机器人、直线加速器、' '数字减影血管造影机（DSA）等国际先进医疗设备，总值超过15亿元。医院拥有符合国际标准的层流净化手术室42间，' 'ICU床位120张。现有在职职工4200余人，其中高级职称专家680人，博士生导师95人，硕士生导师210人，' '享受国务院政府特殊津贴专家42人。医院承担国家级科研项目160余项，获得国家科技进步奖8项，' '省部级科技奖50余项。与美国梅奥诊所、约翰·霍普金斯医院、德国柏林大学附属医院等国际知名医疗机构建立了长期合作关系。' '医院秉承"生命至上、仁心仁术"的宗旨，致力于为每一位患者提供精准、温暖、高效的医疗服务。',
 '内科,外科,儿科,妇产科,骨科,眼科,皮肤科,神经内科,心血管内科,呼吸内科,消化内科,肾内科,内分泌科,肿瘤科,急诊医学科,康复科,放射科,超声科,检验科', 1),
(2, '生命方程式附属医院', '上海市徐汇区衡山路100号', '上海', '021-87654321', '三甲',
 '生命方程式附属医院成立于1985年，是复旦大学上海医学院附属医院，上海市卫生健康委直属的综合性三级甲等医院。' '医院坐落于上海市中心城区，交通便利，环境优美，是上海市医保定点医院和异地就医直接结算定点医院。' '医院占地面积8.2万平方米，建筑面积28万平方米，编制床位1500张，实际开放床位1800张。' '2024年门急诊量达220万人次，年出院病人8.5万人次，年手术量5.2万台次。' '医院设有临床科室38个、医技科室12个，拥有国家级临床重点专科6个（肿瘤科、康复医学科、妇产科、骨科、消化内科、放射科），' '上海市临床重点专科10个。医院以肿瘤综合治疗和康复医学为特色优势，建有华东地区最大的肿瘤精准诊疗中心和康复医学中心。' '配备PET-MRI、3.0T核磁共振（MRI）、双源CT、伽马刀、射波刀、直线加速器等先进医疗设备，总值超过10亿元。' '现有在职职工2800余人，其中高级职称专家420人，博士生导师56人，硕士生导师138人，上海市领军人才15人。' '医院承担国家级和省部级科研项目200余项，在肿瘤免疫治疗、基因检测指导个体化用药、智能康复机器人等领域具有显著科研优势。' '医院与日本国立癌症中心、新加坡中央医院、香港大学玛丽医院等建立了紧密的学术交流与合作关系。' '医院始终坚持以患者为中心，以质量为核心，为人民群众提供优质、安全、便捷的医疗健康服务。',
 '内科,外科,儿科,妇产科,肿瘤科,康复科,骨科,消化内科,神经内科,放射科,超声科,检验科,麻醉科,病理科,中医科', 1);

INSERT IGNORE INTO `doctor` (`id`, `user_id`, `hospital_id`, `name`, `title`, `specialty`, `department`, `introduction`, `consultation_fee`, `is_available`) VALUES
(1, 2, 1, '张医生', '主任医师',
 '冠心病介入治疗、高血压精准管理、心律失常射频消融、心力衰竭综合管理、高脂血症个体化治疗',
 '心血管内科',
 '张医生，医学博士，主任医师，教授，博士生导师，国家心血管病中心专家委员会委员，' '中华医学会心血管病学分会常务委员，中国医师协会心血管内科医师分会副会长。' '1988年毕业于北京医科大学（现北京大学医学部）临床医学专业，1995年获心血管内科博士学位，' '曾先后在美国梅奥诊所（Mayo Clinic）和克利夫兰医学中心（Cleveland Clinic）做访问学者各一年。' '从事心血管内科临床、教学与科研工作30余年，累计完成冠心病介入手术（PCI）超过15000例，' '其中包括高难度左主干病变、慢性完全闭塞病变（CTO）、分叉病变等复杂介入治疗，手术成功率达98%以上。' '在国内率先开展血管内超声（IVUS）和光学相干断层成像（OCT）指导的精准介入治疗技术，' '使支架内再狭窄率从传统治疗方式的15%降低至5%以下。在高血压精准管理方面，建立了基于基因检测和24小时动态血压监测的个体化用药方案，' '使难治性高血压的控制率从40%提升至78%。主持国家自然科学基金重点项目3项、面上项目5项，' '参与国家"973"计划和国家"十三五"重大慢病专项各1项。在《Circulation》《Journal of the American College of Cardiology》' '《European Heart Journal》等国际顶级心血管期刊发表SCI论文120余篇，总影响因子超过600分，被引次数超过5000次。' '主编《心血管介入治疗学》《高血压精准医学》等专著6部，参编国家级规划教材3部。获国家科技进步二等奖1项、' '中华医学科技奖一等奖2项、教育部科技进步一等奖1项。' '在患者中享有极高的口碑，连续十年被评为"中国好医生"，以耐心细致、医术精湛著称。',
 30.00, 1),

(2, 4, 1, '李医生', '副主任医师',
 '脑血管疾病、帕金森病、阿尔茨海默病、癫痫、头痛眩晕、睡眠障碍',
 '神经内科',
 '李医生，医学博士，副主任医师，副教授，硕士生导师，中华医学会神经病学分会青年委员，' '中国卒中学会青年理事会理事，北京神经内科学会脑血管病专业委员会副主任委员。' '2005年毕业于复旦大学上海医学院临床医学七年制，2012年获首都医科大学神经病学博士学位。' '曾于2016年至2017年在德国海德堡大学医院神经内科进修学习，专注于脑血管疾病的介入诊疗和神经退行性疾病的早期诊断。' '从事神经内科临床工作18年，在脑血管疾病（脑梗死、脑出血、短暂性脑缺血发作）的急性期救治和二级预防方面积累了丰富的经验，' '精通静脉溶栓和机械取栓的规范化流程，将脑卒中患者从入院到溶栓的时间（DNT）控制在35分钟以内（国际标准为60分钟），' '远优于国内平均水平。在帕金森病和阿尔茨海默病的早期诊断方面，建立了基于脑脊液生物标志物、多模态MRI和PET-CT的综合评估体系，' '使早期诊断准确率提高至92%以上，为早期干预争取了宝贵的时间窗口。' '主持国家自然科学基金面上项目2项，北京市自然科学基金重点项目1项。在《Neurology》《Stroke》《Brain》等国际神经病学权威期刊' '发表SCI论文45篇。参编《中国脑血管病防治指南》《帕金森病诊疗专家共识》等国家级指南和专家共识5部。' '对待患者亲切和蔼，善于用通俗易懂的语言解释复杂的神经科疾病，深受患者及家属的信赖和好评。',
 20.00, 1),

(3, 5, 1, '王医生', '主任医师',
 '脊柱微创手术、关节置换（髋/膝关节）、运动医学损伤、复杂骨折治疗、骨质疏松综合管理',
 '骨科',
 '王医生，医学博士，主任医师，教授，博士生导师，国际骨科与创伤学会（SICOT）中国部常委，' '中华医学会骨科学分会委员，中国医师协会骨科医师分会脊柱微创学组副组长。' '1992年毕业于华西医科大学（现四川大学华西医学中心）临床医学系，2000年获北京大学第三医院骨科博士学位。' '曾在美国纽约特种外科医院（HSS，全美骨科排名第一）和德国慕尼黑哈拉兴骨科医院做访问学者。' '从事骨科临床工作近30年，累计完成各类骨科手术逾20000台，其中脊柱微创手术8000余台、' '人工关节置换术6000余台。在国内率先开展经皮椎间孔镜（PELD）治疗腰椎间盘突出症、' '椎体后凸成形术（PKP）治疗骨质疏松性椎体压缩骨折等微创技术，' '使患者术后平均住院时间由传统开放手术的7-10天缩短至1-3天。' '在人工髋关节和膝关节置换方面，采用微创入路（直接前入路DAA、微创全膝关节置换）和加速康复外科（ERAS）方案，' '使关节置换患者术后当天即可下床活动，假体使用寿命达到20年以上（国际平均水平为15-20年）。' '在前交叉韧带（ACL）重建、半月板修复、肩袖损伤修复等运动医学手术方面也有深厚的造诣，' '曾为多位国家级运动员成功实施手术并助其重返赛场。' '主持国家自然科学基金项目4项，科技部重点研发计划子课题1项。在《Spine》《Journal of Bone and Joint Surgery》' '《Arthroscopy》等骨科顶级期刊发表SCI论文90余篇。获国家科技进步二等奖1项、中华医学科技奖一等奖2项。' '持有脊柱微创手术和关节置换器械相关国家发明专利8项、实用新型专利15项。' '以手术精细、术后康复快而受到患者的广泛赞誉。',
 30.00, 1),

(4, 6, 1, '赵医生', '主治医师',
 '儿童呼吸系统疾病、儿童消化系统疾病、新生儿疾病、儿童生长发育评估、儿童过敏性疾病',
 '儿科',
 '赵医生，医学硕士，主治医师，中华医学会儿科学分会会员，中国妇幼保健协会儿童保健专业委员会委员。' '2012年毕业于中山大学中山医学院临床医学专业，2015年获北京大学医学部儿科学硕士学位。' '曾在首都医科大学附属北京儿童医院完成儿科住院医师规范化培训，' '2020年赴加拿大多伦多病童医院（SickKids）进修学习儿童呼吸系统疾病的诊治。' '从事儿科临床工作10余年，擅长儿童呼吸系统疾病（支气管哮喘、肺炎、反复呼吸道感染）、' '消化系统疾病（小儿腹泻、牛奶蛋白过敏、功能性腹痛）、新生儿疾病（新生儿黄疸、早产儿管理）以及儿童生长发育评估。' '在儿童哮喘的规范化管理方面，采用全球哮喘防治倡议（GINA）方案，结合过敏原检测和肺功能监测，' '使哮喘患儿急性发作率降低60%，住院率降低75%。在儿童生长发育评估方面，' '采用世界卫生组织（WHO）生长标准和骨龄评估方法，为每个孩子制定个体化的营养和运动指导方案。' '对待小患者极具耐心和亲和力，善于通过游戏化的方式与儿童沟通，' '消除孩子对医院的恐惧感，被小患者和家长们亲切地称为"桃子医生"。' '主持北京市卫健委青年课题1项，参与国家自然科学基金项目2项。' '在《中华儿科杂志》《中国实用儿科杂志》《Pediatric Pulmonology》等期刊发表论文15篇。' '参与编写《儿童哮喘家庭管理手册》等科普读物3部，积极投身于儿科健康教育公益事业。',
 10.00, 1),

(5, 7, 2, '陈医生', '主任医师',
 '肿瘤免疫治疗、靶向治疗、化疗、肿瘤多学科综合治疗（MDT）、肿瘤早期筛查、姑息治疗',
 '肿瘤科',
 '陈医生，医学博士，主任医师，教授，博士生导师，中国临床肿瘤学会（CSCO）常务理事，' '中国抗癌协会肿瘤免疫治疗专业委员会副主任委员，上海市抗癌协会肿瘤精准治疗专业委员会主任委员。' '1996年毕业于上海医科大学（现复旦大学上海医学院）临床医学系，2005年获复旦大学肿瘤学博士学位。' '曾在美国MD安德森癌症中心（MD Anderson Cancer Center）和纪念斯隆·凯特琳癌症中心（MSKCC）' '做博士后研究和访问教授各两年，师从国际著名肿瘤免疫学家James P. Allison教授（2018年诺贝尔生理学或医学奖得主）团队。' '从事肿瘤内科临床、教学与科研工作25年，专注于肺癌、乳腺癌、消化道肿瘤（胃癌、结直肠癌、肝癌）的' '免疫治疗和精准靶向治疗。累计管理肿瘤患者超过30000例，在免疫检查点抑制剂（PD-1/PD-L1、CTLA-4抑制剂）' '的临床应用和不良反应管理方面具有丰富的经验，成功救治了大量晚期肿瘤患者，使部分晚期肺癌患者的5年生存率' '从传统化疗时代的5%提升至免疫治疗时代的30%以上，创造了多个生命的奇迹。' '在国内率先建立了基于二代测序（NGS）和液体活检（ctDNA）的肿瘤精准诊疗平台，' '通过基因检测指导靶向药物选择和耐药监测，使驱动基因阳性肺癌患者的客观缓解率（ORR）达到75%以上。' '大力推动肿瘤多学科综合治疗（MDT）模式，整合外科、放疗科、病理科、影像科等多学科专家资源，' '为每位患者制定最优的个体化综合治疗方案，MDT决策的执行率达到95%以上。' '主持国家自然科学基金重点项目2项、面上项目6项，科技部国家重点研发计划"精准医学研究"专项课题1项。' '在《The New England Journal of Medicine》《The Lancet Oncology》《Journal of Clinical Oncology》' '《Cancer Discovery》《Cancer Cell》等国际顶级肿瘤学期刊发表SCI论文150余篇，' '其中以第一作者或通讯作者发表于影响因子大于30分的期刊论文18篇，总被引次数超过15000次，H指数52。' '获国家科技进步二等奖1项、上海市科技进步一等奖2项、中华医学科技奖一等奖2项。' '入选国家杰出青年科学基金获得者、教育部长江学者特聘教授、上海市领军人才。' '陈医生以精湛的医术、仁爱的医德和永不放弃的精神，赢得了无数肿瘤患者及家属的深切信赖和感激。',
 50.00, 1),

(6, 8, 2, '刘医生', '副主任医师',
 '高危妊娠管理、产前诊断、妇科微创手术、不孕不育诊治、月经失调、更年期保健',
 '妇产科',
 '刘医生，医学博士，副主任医师，副教授，硕士生导师，中华医学会妇产科学分会青年委员，' '中国医师协会妇产科医师分会母胎医学专业委员会委员，上海市医学会妇产科学分会委员。' '2006年毕业于复旦大学上海医学院临床医学七年制，2013年获复旦大学妇产科学博士学位。' '曾于2017年在英国伦敦国王学院医院（King''s College Hospital）进修学习，' '师从国际著名胎儿医学专家Kypros Nicolaides教授，系统学习了产前筛查与诊断的前沿技术。' '从事妇产科临床工作16年，高危妊娠管理经验丰富，每年管理高危孕产妇500余例，' '包括妊娠期高血压疾病、妊娠期糖尿病、前置胎盘、胎盘早剥、多胎妊娠等复杂情况，' '高危妊娠管理成功率（母婴平安出院率）达到99.5%以上。在产前诊断方面，精通羊膜腔穿刺、绒毛取样、' '脐静脉穿刺等产前诊断操作，操作相关流产率控制在0.3%以下（国际标准为0.5%-1%）。' '在妇科微创手术领域技术精湛，年完成腹腔镜及宫腔镜手术800余台，包括腹腔镜下全子宫切除术、' '子宫肌瘤剔除术、卵巢囊肿剥除术、子宫内膜异位症手术以及宫腔镜下子宫内膜息肉电切、粘膜下肌瘤电切等，' '微创手术率达90%以上，术后并发症发生率低于1%。在不孕不育诊治方面，擅长排卵障碍性不孕的促排卵治疗' '和输卵管因素不孕的宫腹腔镜联合手术，帮助数百个家庭实现了生育梦想。' '主持国家自然科学基金面上项目2项，上海市自然科学基金项目1项。' '在《Obstetrics & Gynecology》《American Journal of Obstetrics and Gynecology》' '《Human Reproduction》《Fertility and Sterility》等国际妇产科权威期刊发表SCI论文40余篇。' '参编《高危妊娠管理指南》《产前诊断技术规范》等国家级指南和行业规范3部。' '刘医生对待患者温柔体贴，善于倾听和安抚，尤其注重孕产妇的心理健康，' '被患者们称为"最有温度的妇产科医生"。',
 20.00, 1);

INSERT IGNORE INTO `doctor_schedule` (`doctor_id`, `schedule_date`, `time_slot`, `type`, `max_slots`) VALUES
-- 张医生（心血管内科）
(1, CURDATE(), '上午', '专家门诊', 20),
(1, CURDATE(), '下午', '专家门诊', 15),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '普通门诊', 30),
(1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '专家门诊', 20),
-- 李医生（神经内科）
(2, CURDATE(), '上午', '专家门诊', 25),
(2, CURDATE(), '下午', '普通门诊', 30),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '专家门诊', 20),
(2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '上午', '专家门诊', 25),
-- 王医生（骨科）
(3, CURDATE(), '上午', '专家门诊', 15),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '专家门诊', 15),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '下午', '普通门诊', 30),
(3, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '上午', '专家门诊', 15),
-- 赵医生（儿科）
(4, CURDATE(), '上午', '普通门诊', 30),
(4, CURDATE(), '下午', '普通门诊', 30),
(4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '普通门诊', 30),
(4, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '下午', '普通门诊', 30),
-- 陈医生（肿瘤科）
(5, CURDATE(), '上午', '专家门诊', 10),
(5, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '专家门诊', 10),
(5, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '上午', '专家门诊', 10),
(5, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '下午', '普通门诊', 20),
-- 刘医生（妇产科）
(6, CURDATE(), '上午', '专家门诊', 20),
(6, CURDATE(), '下午', '普通门诊', 25),
(6, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '下午', '专家门诊', 20),
(6, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '上午', '专家门诊', 20);
