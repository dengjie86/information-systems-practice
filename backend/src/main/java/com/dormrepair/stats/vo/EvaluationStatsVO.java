package com.dormrepair.stats.vo;

// 评价分布 4-5分好评 3分中评 1-2分差评
public record EvaluationStatsVO(
    long total,
    long good,
    long mid,
    long bad,
    double favorableRate
) {
}
