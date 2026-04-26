DROP TABLE IF EXISTS repair_order;
DROP TABLE IF EXISTS repair_category;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    phone VARCHAR(20),
    dorm_building VARCHAR(50),
    dorm_room VARCHAR(20),
    avatar VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE repair_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE repair_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    dorm_building VARCHAR(50) NOT NULL,
    dorm_room VARCHAR(20) NOT NULL,
    contact_phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_AUDIT',
    priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
    assigned_worker_id BIGINT,
    reject_reason VARCHAR(255),
    admin_remark VARCHAR(255),
    submit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assign_time TIMESTAMP,
    accept_time TIMESTAMP,
    finish_time TIMESTAMP,
    close_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_order_no UNIQUE (order_no),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_order_category FOREIGN KEY (category_id) REFERENCES repair_category (id)
);

