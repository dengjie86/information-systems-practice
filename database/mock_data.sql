USE dorm_repair;

-- 演示用的工单数据，各种状态都有
-- user_id: admin=1, student1=2, student2=3, student3=4, worker1=5, worker2=6

INSERT INTO repair_order (order_no, user_id, title, category_id, description, dorm_building, dorm_room, contact_phone, status, priority, assigned_worker_id, submit_time, assign_time, accept_time, finish_time) VALUES
('WO202604150001', 2, '宿舍灯管不亮', 5, '101寝室靠窗一侧的灯管完全不亮，已尝试开关多次无效，影响晚间学习。', '1号楼', '101', '13800000011', 'COMPLETED', 'NORMAL', 5, '2026-04-15 09:30:00', '2026-04-15 10:00:00', '2026-04-15 10:30:00', '2026-04-15 14:00:00'),
('WO202604150002', 3, '水龙头漏水严重', 1, '洗手间水龙头关不紧，一直在滴水，浪费水资源且噪音影响休息。', '1号楼', '203', '13800000012', 'COMPLETED', 'HIGH', 6, '2026-04-15 11:00:00', '2026-04-15 11:30:00', '2026-04-15 13:00:00', '2026-04-15 16:00:00'),
('WO202604130001', 2, '插座没电', 1, '书桌旁的插座突然没电了，充电器和台灯都没反应。', '1号楼', '101', '13800000011', 'COMPLETED', 'HIGH', 6, '2026-04-13 08:00:00', '2026-04-13 09:00:00', '2026-04-13 09:30:00', '2026-04-13 11:00:00');

INSERT INTO repair_order (order_no, user_id, title, category_id, description, dorm_building, dorm_room, contact_phone, status, priority, assigned_worker_id, submit_time, assign_time, accept_time) VALUES
('WO202604160001', 2, '门锁打不开', 2, '宿舍门锁经常卡住，需要用力才能打开，担心完全锁死出不去。', '1号楼', '101', '13800000011', 'PENDING_CONFIRM', 'HIGH', 5, '2026-04-16 08:00:00', '2026-04-16 08:30:00', '2026-04-16 09:00:00'),
('WO202604170001', 4, '网络连接不上', 3, '305室所有网口均无法连接网络，Wi-Fi也搜索不到信号，已超过24小时。', '2号楼', '305', '13800000013', 'PROCESSING', 'URGENT', 6, '2026-04-17 10:00:00', '2026-04-17 10:15:00', '2026-04-17 11:00:00');

INSERT INTO repair_order (order_no, user_id, title, category_id, description, dorm_building, dorm_room, contact_phone, status, priority, assigned_worker_id, submit_time, assign_time) VALUES
('WO202604180001', 3, '空调不制冷', 6, '寝室空调开了两个小时还是不制冷，吹出来的风是热的。', '1号楼', '203', '13800000012', 'PENDING_ACCEPT', 'NORMAL', 5, '2026-04-18 14:00:00', '2026-04-18 15:00:00');

INSERT INTO repair_order (order_no, user_id, title, category_id, description, dorm_building, dorm_room, contact_phone, status, priority, submit_time) VALUES
('WO202604190001', 4, '椅子腿断裂', 4, '书桌旁边的椅子一条腿断了，坐上去会晃动，有安全隐患。', '2号楼', '305', '13800000013', 'PENDING_ASSIGN', 'NORMAL', '2026-04-19 09:00:00'),
('WO202604200001', 2, '卫生间地漏堵塞', 7, '卫生间洗澡时排水特别慢，地漏好像被堵住了，水会积到脚踝。', '1号楼', '101', '13800000011', 'PENDING_AUDIT', 'NORMAL', '2026-04-20 16:00:00'),
('WO202604200002', 3, '墙面瓷砖脱落', 8, '卫生间靠门那面墙有两块瓷砖掉落，露出水泥面，担心继续脱落。', '1号楼', '203', '13800000012', 'PENDING_AUDIT', 'LOW', '2026-04-20 17:30:00');

INSERT INTO repair_order (order_no, user_id, title, category_id, description, dorm_building, dorm_room, contact_phone, status, priority, reject_reason, submit_time) VALUES
('WO202604140001', 4, '想更换窗帘', 8, '窗帘颜色太旧了，想换一个新的。', '2号楼', '305', '13800000013', 'REJECTED', 'LOW', '窗帘更换不属于报修范围，请联系宿管自行购买。', '2026-04-14 10:00:00');

-- 维修记录
INSERT INTO repair_record (order_id, worker_id, action_desc, action_type, status_before, status_after, action_time) VALUES
(1, 5, '已接单，预计下午上门维修。', 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', '2026-04-15 10:30:00'),
(1, 5, '已更换新灯管，测试正常发光，维修完成。', 'FINISH', 'PROCESSING', 'PENDING_CONFIRM', '2026-04-15 14:00:00'),
(2, 6, '已接单，准备维修工具。', 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', '2026-04-15 13:00:00'),
(2, 6, '拆除旧水龙头，安装新阀芯，测试不再漏水。', 'FINISH', 'PROCESSING', 'PENDING_CONFIRM', '2026-04-15 16:00:00'),
(4, 5, '已接单，带门锁配件上门。', 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', '2026-04-16 09:00:00'),
(4, 5, '更换锁芯，润滑锁体，开关顺畅，等待学生确认。', 'FINISH', 'PROCESSING', 'PENDING_CONFIRM', '2026-04-16 11:30:00'),
(5, 6, '已接单，需要先排查楼层交换机。', 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', '2026-04-17 11:00:00'),
(3, 6, '已接单。', 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', '2026-04-13 09:30:00'),
(3, 6, '检查发现是空气开关跳闸，已复位，插座恢复供电。', 'FINISH', 'PROCESSING', 'PENDING_CONFIRM', '2026-04-13 11:00:00');

-- 评价
INSERT INTO evaluation (order_id, user_id, score, content, create_time) VALUES
(2, 3, 5, '维修很及时，师傅态度也很好，换完之后水龙头完全不漏了，非常满意！', '2026-04-15 18:00:00'),
(3, 2, 4, '修好了，速度挺快，就是来的时候比预期晚了一点。', '2026-04-13 15:00:00');
