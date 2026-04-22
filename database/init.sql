USE dorm_repair;

-- 测试账号，密码都是 123456 (BCrypt)
INSERT INTO `user` (username, password, real_name, role, phone, dorm_building, dorm_room, status) VALUES
('admin', '$2a$10$qq71wMIzL3WWTGX5qYA6JOk5Q6awRAIn3273U8Zkqf3DX1IfqusdK', '系统管理员', 'ADMIN', '13800000001', NULL, NULL, 1),
('student1', '$2a$10$qq71wMIzL3WWTGX5qYA6JOk5Q6awRAIn3273U8Zkqf3DX1IfqusdK', '张三', 'STUDENT', '13800000011', '1号楼', '101', 1),
('student2', '$2a$10$qq71wMIzL3WWTGX5qYA6JOk5Q6awRAIn3273U8Zkqf3DX1IfqusdK', '李四', 'STUDENT', '13800000012', '1号楼', '203', 1),
('student3', '$2a$10$qq71wMIzL3WWTGX5qYA6JOk5Q6awRAIn3273U8Zkqf3DX1IfqusdK', '王五', 'STUDENT', '13800000013', '2号楼', '305', 1),
('worker1', '$2a$10$qq71wMIzL3WWTGX5qYA6JOk5Q6awRAIn3273U8Zkqf3DX1IfqusdK', '赵师傅', 'WORKER', '13800000021', NULL, NULL, 1),
('worker2', '$2a$10$qq71wMIzL3WWTGX5qYA6JOk5Q6awRAIn3273U8Zkqf3DX1IfqusdK', '钱师傅', 'WORKER', '13800000022', NULL, NULL, 1);

-- 故障分类
INSERT INTO repair_category (category_name, description, sort_order, status) VALUES
('水电维修', '水管漏水、水龙头损坏、电路故障、跳闸、插座损坏等', 1, 1),
('门窗维修', '门锁损坏、门把手松动、窗户关不严、玻璃破损等', 2, 1),
('网络故障', '网络无法连接、网速异常、网口损坏、路由器故障等', 3, 1),
('家具损坏', '桌椅损坏、衣柜门坏、床板断裂、抽屉损坏等', 4, 1),
('灯具维修', '灯管不亮、灯具闪烁、灯罩脱落、开关故障等', 5, 1),
('空调暖气', '空调不制冷、空调漏水、暖气不热、温控器故障等', 6, 1),
('卫浴设施', '马桶堵塞、淋浴头损坏、洗手台漏水、地漏堵塞等', 7, 1),
('其他问题', '墙面脱落、地面损坏、虫害等其他宿舍设施问题', 8, 1);
