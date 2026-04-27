CREATE DATABASE IF NOT EXISTS dorm_repair
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE dorm_repair;

-- 先删旧表，注意外键顺序
DROP TABLE IF EXISTS evaluation;
DROP TABLE IF EXISTS repair_record;
DROP TABLE IF EXISTS repair_order;
DROP TABLE IF EXISTS repair_category;
DROP TABLE IF EXISTS `user`;

-- 用户表
CREATE TABLE `user` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'STUDENT ADMIN WORKER',
    phone VARCHAR(20) DEFAULT NULL,
    dorm_building VARCHAR(50) DEFAULT NULL,
    dorm_room VARCHAR(20) DEFAULT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 故障分类
CREATE TABLE repair_category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 报修工单
-- 状态: PENDING_AUDIT / PENDING_ASSIGN / PENDING_ACCEPT / PROCESSING / PENDING_CONFIRM / COMPLETED / REJECTED / CLOSED
CREATE TABLE repair_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL,
    description TEXT,
    image_url VARCHAR(500) DEFAULT NULL,
    dorm_building VARCHAR(50) NOT NULL,
    dorm_room VARCHAR(20) NOT NULL,
    contact_phone VARCHAR(20) DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_AUDIT',
    priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL' COMMENT 'LOW NORMAL HIGH URGENT',
    assigned_worker_id BIGINT DEFAULT NULL,
    reject_reason VARCHAR(255) DEFAULT NULL,
    admin_remark VARCHAR(255) DEFAULT NULL,
    dispatch_remark VARCHAR(255) DEFAULT NULL,
    submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assign_time DATETIME DEFAULT NULL,
    accept_time DATETIME DEFAULT NULL,
    finish_time DATETIME DEFAULT NULL,
    close_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_category_id (category_id),
    KEY idx_assigned_worker_id (assigned_worker_id),
    KEY idx_status (status),
    KEY idx_submit_time (submit_time),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_order_category FOREIGN KEY (category_id) REFERENCES repair_category (id),
    CONSTRAINT fk_order_worker FOREIGN KEY (assigned_worker_id) REFERENCES `user` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 维修记录
CREATE TABLE repair_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    worker_id BIGINT NOT NULL,
    action_desc TEXT,
    action_type VARCHAR(20) NOT NULL COMMENT 'ACCEPT REJECT START FINISH',
    result_image VARCHAR(500) DEFAULT NULL,
    status_before VARCHAR(20) DEFAULT NULL,
    status_after VARCHAR(20) DEFAULT NULL,
    action_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_worker_id (worker_id),
    CONSTRAINT fk_record_order FOREIGN KEY (order_id) REFERENCES repair_order (id),
    CONSTRAINT fk_record_worker FOREIGN KEY (worker_id) REFERENCES `user` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评价
CREATE TABLE evaluation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    score TINYINT NOT NULL COMMENT '1-5分',
    content VARCHAR(500) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_id (order_id),
    KEY idx_user_id (user_id),
    CONSTRAINT fk_eval_order FOREIGN KEY (order_id) REFERENCES repair_order (id),
    CONSTRAINT fk_eval_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
