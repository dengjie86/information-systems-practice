USE dorm_repair;

ALTER TABLE repair_order
    ADD COLUMN dispatch_remark VARCHAR(255) DEFAULT NULL AFTER admin_remark;
