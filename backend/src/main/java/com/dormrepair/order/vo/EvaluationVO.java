package com.dormrepair.order.vo;

import java.time.LocalDateTime;

public record EvaluationVO(
    Long id,
    Long orderId,
    Integer score,
    String content,
    LocalDateTime createTime
) {}
