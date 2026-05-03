package com.dormrepair.stats.vo;

public record OverviewVO(
    long total,
    long completed,
    long processing,
    long pendingAudit,
    long cancelled,
    long rejected,
    double completedRate
) {
}
