package com.dormrepair.stats.service;

import com.dormrepair.common.constant.OrderStatuses;
import com.dormrepair.stats.mapper.StatsMapper;
import com.dormrepair.stats.vo.CategoryDistributionVO;
import com.dormrepair.stats.vo.OverviewVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    @Autowired
    private StatsMapper statsMapper;

    public OverviewVO getOverview() {
        Map<String, Long> map = new HashMap<>();
        for (Map<String, Object> row : statsMapper.countByStatus()) {
            String status = (String) row.get("status");
            Long cnt = ((Number) row.get("cnt")).longValue();
            map.put(status, cnt);
        }

        long completed = map.getOrDefault(OrderStatuses.COMPLETED, 0L);
        long pendingAudit = map.getOrDefault(OrderStatuses.PENDING_AUDIT, 0L);
        long cancelled = map.getOrDefault(OrderStatuses.CANCELLED, 0L);
        long rejected = map.getOrDefault(OrderStatuses.REJECTED, 0L);
        // 处理中算作分派、接单、维修、待确认四个状态加起来
        long processing = map.getOrDefault(OrderStatuses.PENDING_ASSIGN, 0L)
            + map.getOrDefault(OrderStatuses.PENDING_ACCEPT, 0L)
            + map.getOrDefault(OrderStatuses.PROCESSING, 0L)
            + map.getOrDefault(OrderStatuses.PENDING_CONFIRM, 0L);

        long total = 0;
        for (Long v : map.values()) {
            total += v;
        }

        double rate = total == 0 ? 0.0 : Math.round(completed * 10000.0 / total) / 100.0;
        return new OverviewVO(total, completed, processing, pendingAudit, cancelled, rejected, rate);
    }

    public List<CategoryDistributionVO> getCategoryDistribution() {
        List<CategoryDistributionVO> list = new ArrayList<>();
        for (Map<String, Object> row : statsMapper.countByCategory()) {
            String name = (String) row.get("name");
            Long cnt = ((Number) row.get("cnt")).longValue();
            list.add(new CategoryDistributionVO(name, cnt));
        }
        return list;
    }
}
