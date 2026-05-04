package com.dormrepair.stats.controller;

import com.dormrepair.common.constant.UserRoles;
import com.dormrepair.common.exception.BusinessException;
import com.dormrepair.common.result.Result;
import com.dormrepair.common.result.ResultCode;
import com.dormrepair.security.LoginUser;
import com.dormrepair.security.LoginUserContext;
import com.dormrepair.stats.service.StatsService;
import com.dormrepair.stats.vo.CategoryDistributionVO;
import com.dormrepair.stats.vo.DailyTrendVO;
import com.dormrepair.stats.vo.EvaluationStatsVO;
import com.dormrepair.stats.vo.OverviewVO;
import com.dormrepair.stats.vo.WorkerLoadVO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 管理端统计接口
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/overview")
    public Result<OverviewVO> overview() {
        checkAdmin();
        return Result.success(statsService.getOverview());
    }

    @GetMapping("/category")
    public Result<List<CategoryDistributionVO>> category() {
        checkAdmin();
        return Result.success(statsService.getCategoryDistribution());
    }

    @GetMapping("/worker-load")
    public Result<List<WorkerLoadVO>> workerLoad() {
        checkAdmin();
        return Result.success(statsService.getWorkerLoad());
    }

    @GetMapping("/trend")
    public Result<List<DailyTrendVO>> trend() {
        checkAdmin();
        return Result.success(statsService.getRecentTrend());
    }

    @GetMapping("/evaluation")
    public Result<EvaluationStatsVO> evaluation() {
        checkAdmin();
        return Result.success(statsService.getEvaluationStats());
    }

    private void checkAdmin() {
        LoginUser user = LoginUserContext.requireUser();
        if (!UserRoles.ADMIN.equals(user.role())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问");
        }
    }
}
