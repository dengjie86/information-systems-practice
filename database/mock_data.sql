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

-- 学生自己取消的工单
INSERT INTO repair_order (order_no, user_id, title, category_id, description, dorm_building, dorm_room, contact_phone, status, priority, submit_time, close_time) VALUES
('WO202604220001', 3, '水龙头有点松动', 1, '感觉水龙头基座有点松。', '1号楼', '203', '13800000012', 'CANCELLED', 'LOW', '2026-04-22 09:00:00', '2026-04-22 09:30:00'),
('WO202604230001', 4, '桌子有点晃', 4, '桌子晃动，准备重新填一份描述更清楚的。', '2号楼', '305', '13800000013', 'CANCELLED', 'NORMAL', '2026-04-23 14:00:00', '2026-04-23 14:20:00');

-- 近7天的演示数据 用相对时间 这样趋势图永远有数据
-- 已完成 给统计的好评率/差评中评铺底
INSERT INTO repair_order (order_no, user_id, title, category_id, description, dorm_building, dorm_room, contact_phone, status, priority, assigned_worker_id, submit_time, assign_time, accept_time, finish_time) VALUES
('WORECENT0001', 2, '台灯坏了', 5, '台灯按了没反应，可能是开关问题。', '1号楼', '101', '13800000011', 'COMPLETED', 'NORMAL', 5,
    DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 30 MINUTE, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 1 HOUR, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 4 HOUR),
('WORECENT0002', 4, '插座冒火花', 1, '插充电器的时候插座有点火花，吓了一跳。', '2号楼', '305', '13800000013', 'COMPLETED', 'HIGH', 6,
    DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 2 HOUR, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 3 HOUR, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 4 HOUR, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 7 HOUR),
('WORECENT0003', 3, '柜门关不上', 4, '衣柜门合页好像松了，关不严实。', '1号楼', '203', '13800000012', 'COMPLETED', 'NORMAL', 5,
    DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 1 HOUR, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 2 HOUR, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 5 HOUR),
('WORECENT0004', 2, '宿舍网速慢', 3, '晚上网络特别卡，看视频一直转圈。', '1号楼', '101', '13800000011', 'COMPLETED', 'LOW', 6,
    DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 1 HOUR, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 2 HOUR, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 6 HOUR);

-- 待确认 处理中
INSERT INTO repair_order (order_no, user_id, title, category_id, description, dorm_building, dorm_room, contact_phone, status, priority, assigned_worker_id, submit_time, assign_time, accept_time) VALUES
('WORECENT0005', 4, '马桶冲水有问题', 7, '按下水箱按钮要按好几次才有水。', '2号楼', '305', '13800000013', 'PENDING_CONFIRM', 'NORMAL', 5,
    DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 3 HOUR, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 4 HOUR, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 5 HOUR),
('WORECENT0006', 3, '空调遥控器没反应', 6, '空调遥控器换电池也没用。', '1号楼', '203', '13800000012', 'PROCESSING', 'URGENT', 6,
    DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 5 HOUR, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 6 HOUR, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 7 HOUR);

-- 待接单和待分派
INSERT INTO repair_order (order_no, user_id, title, category_id, description, dorm_building, dorm_room, contact_phone, status, priority, assigned_worker_id, submit_time, assign_time) VALUES
('WORECENT0007', 2, '门锁卡', 2, '锁不顺，钥匙拧的时候特别费劲。', '1号楼', '101', '13800000011', 'PENDING_ACCEPT', 'NORMAL', 5,
    DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 1 HOUR);

INSERT INTO repair_order (order_no, user_id, title, category_id, description, dorm_building, dorm_room, contact_phone, status, priority, submit_time) VALUES
('WORECENT0008', 4, '床板有响声', 4, '翻身就响，已经响了好几天。', '2号楼', '305', '13800000013', 'PENDING_ASSIGN', 'NORMAL', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 4 HOUR),
('WORECENT0009', 3, '淋浴喷头漏水', 7, '关掉之后还在滴水。', '1号楼', '203', '13800000012', 'PENDING_AUDIT', 'LOW', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('WORECENT0011', 4, '玻璃窗关不严', 2, '风大的时候窗户会响。', '2号楼', '305', '13800000013', 'PENDING_AUDIT', 'NORMAL', NOW());

INSERT INTO repair_order (order_no, user_id, title, category_id, description, dorm_building, dorm_room, contact_phone, status, priority, assigned_worker_id, submit_time, assign_time, accept_time) VALUES
('WORECENT0010', 2, '空调不太冷', 6, '感觉没以前冷了。', '1号楼', '101', '13800000011', 'PROCESSING', 'HIGH', 6,
    DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 2 HOUR, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 3 HOUR, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 4 HOUR);

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

-- 近7天工单的维修记录
INSERT INTO repair_record (order_id, worker_id, action_desc, action_type, status_before, status_after, action_time) VALUES
(13, 5, '已接单。', 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 1 HOUR),
(13, 5, '更换了新开关，台灯正常。', 'FINISH', 'PROCESSING', 'PENDING_CONFIRM', DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 4 HOUR),
(14, 6, '已接单 注意安全。', 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 4 HOUR),
(14, 6, '更换了插座，已正常供电。', 'FINISH', 'PROCESSING', 'PENDING_CONFIRM', DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 7 HOUR),
(15, 5, '已接单。', 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 2 HOUR),
(15, 5, '紧固了合页螺丝，柜门可以正常关闭。', 'FINISH', 'PROCESSING', 'PENDING_CONFIRM', DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 5 HOUR),
(16, 6, '已接单。', 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 2 HOUR),
(16, 6, '重启了路由器并清理了缓存，网速恢复。', 'FINISH', 'PROCESSING', 'PENDING_CONFIRM', DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 6 HOUR),
(17, 5, '已接单。', 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 5 HOUR),
(17, 5, '更换了进水阀，按一次就能正常冲水。', 'FINISH', 'PROCESSING', 'PENDING_CONFIRM', DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 6 HOUR),
(18, 6, '已接单 准备测试遥控器。', 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 7 HOUR),
(23, 6, '已接单 上门检查。', 'ACCEPT', 'PENDING_ACCEPT', 'PROCESSING', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 4 HOUR);

-- 评价 既有好评也有差评中评
INSERT INTO evaluation (order_id, user_id, score, content, create_time) VALUES
(2, 3, 5, '维修很及时，师傅态度也很好，换完之后水龙头完全不漏了，非常满意！', '2026-04-15 18:00:00'),
(3, 2, 4, '修好了，速度挺快，就是来的时候比预期晚了一点。', '2026-04-13 15:00:00'),
(13, 2, 5, '修得挺快的。', DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 5 HOUR),
(14, 4, 1, '来得有点慢，等了好久才上门。', DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 8 HOUR),
(15, 3, 3, '修好了 还行吧。', DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 6 HOUR),
(16, 2, 4, '问题解决了，师傅人挺好。', DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 7 HOUR);
