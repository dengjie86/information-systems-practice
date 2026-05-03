package com.dormrepair.stats.vo;

public record WorkerLoadVO(
    Long workerId,
    String workerName,
    long total,
    long completed
) {
}
