package com.dormrepair.stats.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StatsMapper {

    @Select("SELECT status, COUNT(*) AS cnt FROM repair_order GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT c.category_name AS name, COUNT(o.id) AS cnt "
        + "FROM repair_category c LEFT JOIN repair_order o ON o.category_id = c.id "
        + "GROUP BY c.id, c.category_name "
        + "ORDER BY cnt DESC, c.sort_order ASC")
    List<Map<String, Object>> countByCategory();

    // 每个维修工处理的工单数 总数+已完成
    @Select("SELECT u.id AS wid, u.real_name AS wname, "
        + "COUNT(o.id) AS total, "
        + "SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed "
        + "FROM user u LEFT JOIN repair_order o ON o.assigned_worker_id = u.id "
        + "WHERE u.role = 'WORKER' "
        + "GROUP BY u.id, u.real_name "
        + "ORDER BY total DESC, u.id ASC")
    List<Map<String, Object>> countByWorker();

    // 近7天每天的工单数 缺失的日期在service层补0
    @Select("SELECT DATE(submit_time) AS day, COUNT(*) AS cnt "
        + "FROM repair_order "
        + "WHERE submit_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) "
        + "GROUP BY DATE(submit_time)")
    List<Map<String, Object>> countRecentDays();

    // 按评分统计 1-5分各多少条
    @Select("SELECT score, COUNT(*) AS cnt FROM evaluation GROUP BY score")
    List<Map<String, Object>> countByScore();
}