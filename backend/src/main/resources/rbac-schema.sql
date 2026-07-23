-- ============================================================
-- RBAC (Role-Based Access Control) 数据库初始化脚本
-- 为 Spring Security 提供细粒度权限控制的基础表结构
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
-- 初始化种子数据
-- ============================================================

INSERT IGNORE INTO `role` (`name`, `description`) VALUES
    ('USER', '普通用户'),
    ('DOCTOR', '医生'),
    ('ADMIN', '系统管理员');

-- 权限种子数据（后续可按需扩展）
INSERT IGNORE INTO `permission` (`name`, `resource`, `description`) VALUES
    ('user:view', '/api/admin/users', '查看用户列表'),
    ('user:update', '/api/admin/users/*', '修改用户信息'),
    ('user:delete', '/api/admin/users/*', '删除用户'),
    ('doctor:schedule', '/api/doctor/schedule/**', '排班管理'),
    ('doctor:patient', '/api/doctor/patient/**', '查看患者病历'),
    ('consultation:create', '/api/consultations', '创建咨询'),
    ('consultation:reply', '/api/doctor/reply-consultation/**', '回复咨询');

-- 为 ADMIN 角色分配所有权限
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
    SELECT r.id, p.id FROM `role` r, `permission` p
    WHERE r.name = 'ADMIN';
