package com.dormrepair.order.vo;

import java.time.LocalDateTime;

public record RepairRecordVO(
    Long id,
    Long orderId,
    Long workerId,
    String actionType,
    String actionDesc,
    String resultImage,
    String statusBefore,
    String statusAfter,
    LocalDateTime actionTime
) {
}
