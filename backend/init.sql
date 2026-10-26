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
    `latitude` DECIMAL(10,7) COMMENT '纬度',
    `longitude` DECIMAL(10,7) COMMENT '经度',
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
    `status` VARCHAR(20) DEFAULT 'UNPAID' COMMENT '状态(UNPAID待支付/PENDING待确认/CONFIRMED已确认/COMPLETED已完成/CANCELLED已取消)',
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
(2, 'doctor1', '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '曹明艳',     'DOCTOR', '13800000001', 1),
(3, 'user1',   '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '测试用户',   'USER',   '13800000002', 1),
(4, 'doctor2', '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '曹锡文',     'DOCTOR', '13800000003', 1),
(5, 'doctor3', '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '李国亮',     'DOCTOR', '13800000004', 1),
(6, 'doctor4', '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '秦牧',       'DOCTOR', '13800000005', 1),
(7, 'doctor5', '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '黄勉芳',     'DOCTOR', '13800000006', 1),
(8, 'doctor6', '$2a$10$vU4rLjlKp.x/GX72Fl0K9.HOHa0n9nzpuzMWiHfbxANqJC7dYMomK', '曹烈权',     'DOCTOR', '13800000007', 1);

INSERT IGNORE INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 3),   -- admin → ADMIN角色
(2, 2),   -- doctor1 → DOCTOR角色
(3, 1),   -- user1 → USER角色
(4, 2),   -- doctor2 → DOCTOR角色
(5, 2),   -- doctor3 → DOCTOR角色
(6, 2),   -- doctor4 → DOCTOR角色
(7, 2),   -- doctor5 → DOCTOR角色
(8, 2);   -- doctor6 → DOCTOR角色

-- ============================================================
-- 单院数据：湖南省三甲医院（原型演示，医院名做了模糊化处理）
-- ============================================================

INSERT IGNORE INTO `hospital` (`id`, `name`, `address`, `city`, `phone`, `level`, `introduction`, `departments`, `latitude`, `longitude`, `business_hours`, `status`) VALUES
(1, '湖南省三甲医院', '湖南省郴州市北湖区人民西路', '郴州', '0735-1234567', '三甲',
 '湖南省三甲医院是一所集医疗、教学、科研、预防、保健、康复于一体的国家三级甲等综合医院。医院占地面积60余亩，建筑面积8万余平方米，编制床位1000张，开放床位1000张。现有职工1325人，其中正高99人、副高210人，博士19人、硕士212人。医院设有62个临床医技专业科室、17个教研室和1个临床技能中心，肿瘤科、骨科为国家临床重点专科培育项目，拥有省级临床重点专科10个、市级临床重点专科9个。医院配备TOMO肿瘤精准放射治疗系统、PET-CT、直线加速器、DSA、64排128层螺旋CT、磁共振等大型医疗设备，形成了肿瘤、骨科、心脏外科三大特色技术品牌。医院始终坚持以病人为中心，以精湛的技术、一流的设备、优质的服务为区域群众提供全方位的医疗健康服务。',
 '呼吸内科,消化内科,心血管内科,内分泌科,肾内科（风湿免疫科）,神经内科,全科医学科,临床心理科,营养科,老年医学科,骨一科,骨二科,脊柱外科,胃肠外科,肝胆胰脾外科,泌尿外科,胸外科（乳腺外科、甲状腺外科）,神经外科,介入（血管）外科,肛肠外科,整形美容科,妇科,产科,儿科·新生儿科,耳鼻咽喉头颈外科,眼科,口腔科,皮肤病科（性病科）,中医科,康复医学科,疼痛科,急诊医学科,重症医学科,麻醉科,肿瘤一科,肿瘤二科,肿瘤放疗科、血液淋巴瘤科,检验科,放射科,超声科,病理科,功能科,心电图室,核医学科',
 25.7938910, 113.0257560, '门诊 8:00-12:00，14:30-17:30；急诊 24小时', 1);

INSERT IGNORE INTO `doctor` (`id`, `user_id`, `hospital_id`, `name`, `title`, `specialty`, `department`, `introduction`, `consultation_fee`, `is_available`) VALUES
(1, 2, 1, '曹明艳', '主任医师',
 '冠心病、高血压病、心功能衰竭、高脂血症、心血管疾病介入治疗',
 '心血管内科',
 '主任医师，硕士研究生。1995年毕业于南华大学临床医学系，获学士学位；2005年7月毕业于中南大学，获硕士学位。现任本院内科教研室副主任。多年来一直从事心血管疾病的治疗和防治工作，尤其在冠心病、高血压病、心功能衰竭、高脂血症等方面积累了丰富经验。攻读硕士学位期间专攻心血管疾病介入治疗，掌握心血管疾病前沿治疗方法，并在本院开展心血管疾病介入治疗，取得了良好的临床效果。',
 30.00, 1),

(2, 4, 1, '曹锡文', '主任医师',
 '创伤骨科、关节外科、骨肿瘤、髋膝关节置换、骨盆骨折手术治疗',
 '骨一科',
 '主任医师，郴州市知名骨科专家，郴州市骨科专业学会副主委，本院骨科创始人，郴州市医疗事故鉴定专家组成员。大学本科毕业，从事医疗工作20多年，曾在武汉同济医科大学附属协和医院进修骨科一年。理论扎实，技术全面，经验丰富，在创伤骨科、关节外科及骨肿瘤等方面造诣较深，尤其擅长髋、膝关节置换，骨盆骨折手术治疗以及骨科疑难疾病的诊疗。',
 32.00, 1),

(3, 5, 1, '李国亮', '主任医师、教授',
 '肿瘤综合治疗、肿瘤放疗、鼻咽癌、乳腺癌、肺癌、淋巴瘤',
 '肿瘤一科',
 '男，主任医师、教授，本院肿瘤科名誉主任。1982年毕业于南华大学，从事肿瘤临床40余年，是郴州市肿瘤专业和本院肿瘤科主要创始人之一。1994年起担任本院肿瘤科主任，先后发起并成立郴州市抗癌协会、郴州市医学会肿瘤专业委员会。现任郴州市肿瘤学科主要带头人，湖南省抗癌协会、湖南省肿瘤专业委员会及湖南省放疗专业委员会主要成员，湖南知名肿瘤专家。',
 45.00, 1),

(4, 6, 1, '秦牧', '主任医师',
 '白内障、青光眼、泪道疾病、鼻眼相关疾病、眼肿瘤',
 '眼科',
 '男，眼科主任，硕士，主任医师。擅长白内障、青光眼、泪道相关疾病的诊治，目前主要致力于鼻眼相关疾病和眼肿瘤疾病的研究，是湖南省眼鼻相关专科的领头人物。',
 30.00, 1),

(5, 7, 1, '黄勉芳', '主任医师',
 '龋病、根尖周病、牙周病、缺失牙修复、错牙列正畸矫治',
 '口腔科',
 '口腔主任医师，本院口腔科主任、口腔教研室主任、口腔医学专业带头人，湖南省口腔修复专业委员会常务委员、湖南省口腔教育学专业委员会常务委员、湖南省口腔健康促进会理事。从事口腔临床工作三十余年，临床经验丰富，擅长龋病、根尖周病、牙周病诊治，在缺失牙修复、错牙列正畸矫治等各类口腔疾病综合诊治方面颇具经验。发表专业论文10余篇，获郴州市科技局科技进步三等奖。',
 38.00, 1),

(6, 8, 1, '曹烈权', '主任医师',
 '胃肠肿瘤、腹腔镜微创手术、血管外科、烧伤救治、肝胆疾病',
 '胃肠外科',
 '主任医师，胃肠外科主任，硕士研究生。2001年毕业于中南大学临床医学专业，从事临床工作20余年。现主要从事胃肠、烧伤、血管、肝胆外科疾病诊治，在胃肠道肿瘤、急性腹痛、消化道大出血及腹部外科急危重症救治方面经验丰富。擅长全腹腔镜胃癌根治、结直肠癌根治、腹腔镜疝修补等微创手术，以及血管外科疾病和各类烧伤的救治。',
 32.00, 1);

INSERT IGNORE INTO `pharmacy` (`id`, `name`, `address`, `city`, `phone`, `latitude`, `longitude`, `business_hours`, `is_24h`, `status`) VALUES
(1, '湖南省三甲医院门诊药房', '湖南省郴州市北湖区人民西路门诊楼1层', '郴州', '0735-1234567', 25.7938910, 113.0257560, '8:00-17:30', 0, 1),
(2, '湖南省三甲医院急诊药房', '湖南省郴州市北湖区人民西路急诊楼1层', '郴州', '0735-1234567', 25.7938910, 113.0257560, '24小时', 1, 1);

INSERT IGNORE INTO `doctor_schedule` (`doctor_id`, `schedule_date`, `time_slot`, `type`, `max_slots`) VALUES
-- 曹明艳（心血管内科）
(1, CURDATE(), '上午', '专家门诊', 20),
(1, CURDATE(), '下午', '专家门诊', 15),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '普通门诊', 30),
(1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '专家门诊', 20),
-- 曹锡文（骨一科）
(2, CURDATE(), '上午', '专家门诊', 25),
(2, CURDATE(), '下午', '普通门诊', 30),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '专家门诊', 20),
(2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '上午', '专家门诊', 25),
-- 李国亮（肿瘤一科）
(3, CURDATE(), '上午', '专家门诊', 15),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '专家门诊', 15),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '下午', '普通门诊', 30),
(3, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '上午', '专家门诊', 15),
-- 秦牧（眼科）
(4, CURDATE(), '上午', '普通门诊', 30),
(4, CURDATE(), '下午', '普通门诊', 30),
(4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '上午', '普通门诊', 30),
(4, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '下午', '普通门诊', 30),
-- 黄勉芳（口腔科）
(5, CURDATE(), '上午', '专家门诊', 10),
(5, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '上午', '专家门诊', 10),
(5, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '上午', '专家门诊', 10),
(5, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '下午', '普通门诊', 20),
-- 曹烈权（胃肠外科）
(6, CURDATE(), '上午', '专家门诊', 20),
(6, CURDATE(), '下午', '普通门诊', 25),
(6, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '下午', '专家门诊', 20),
(6, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '上午', '专家门诊', 20);
