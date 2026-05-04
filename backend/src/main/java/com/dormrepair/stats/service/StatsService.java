package com.dormrepair.stats.service;

import com.dormrepair.common.constant.OrderStatuses;
import com.dormrepair.stats.mapper.StatsMapper;
import com.dormrepair.stats.vo.CategoryDistributionVO;
import com.dormrepair.stats.vo.DailyTrendVO;
import com.dormrepair.stats.vo.EvaluationStatsVO;
import com.dormrepair.stats.vo.OverviewVO;
import com.dormrepair.stats.vo.WorkerLoadVO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    public List<WorkerLoadVO> getWorkerLoad() {
        List<WorkerLoadVO> list = new ArrayList<>();
        for (Map<String, Object> row : statsMapper.countByWorker()) {
            Long wid = ((Number) row.get("wid")).longValue();
            String wname = (String) row.get("wname");
            Long total = ((Number) row.get("total")).longValue();
            Object completedObj = row.get("completed");
            // SUM 在没数据时可能返回 null
            long completed = completedObj == null ? 0L : ((Number) completedObj).longValue();
            list.add(new WorkerLoadVO(wid, wname, total, completed));
        }
        return list;
    }

    public EvaluationStatsVO getEvaluationStats() {
        long good = 0;
        long mid = 0;
        long bad = 0;
        for (Map<String, Object> row : statsMapper.countByScore()) {
            int score = ((Number) row.get("score")).intValue();
            long cnt = ((Number) row.get("cnt")).longValue();
            if (score >= 4) {
                good += cnt;
            } else if (score == 3) {
                mid += cnt;
            } else {
                bad += cnt;
            }
        }
        long total = good + mid + bad;
        double rate = total == 0 ? 0.0 : Math.round(good * 10000.0 / total) / 100.0;
        return new EvaluationStatsVO(total, good, mid, bad, rate);
    }

    public List<DailyTrendVO> getRecentTrend() {
        // 把查出来的结果先塞进 map
        Map<String, Long> dayMap = new HashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Map<String, Object> row : statsMapper.countRecentDays()) {
            Object dayObj = row.get("day");
            String dayStr;
            if (dayObj instanceof java.sql.Date d) {
                dayStr = d.toLocalDate().format(fmt);
            } else if (dayObj instanceof LocalDate ld) {
                dayStr = ld.format(fmt);
            } else {
                dayStr = dayObj.toString();
            }
            Long cnt = ((Number) row.get("cnt")).longValue();
            dayMap.put(dayStr, cnt);
        }
        // 补齐近7天 没数据的填0
        List<DailyTrendVO> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            String d = today.minusDays(i).format(fmt);
            result.add(new DailyTrendVO(d, dayMap.getOrDefault(d, 0L)));
        }
        return result;
    }
}
