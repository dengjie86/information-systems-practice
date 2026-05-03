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
}
