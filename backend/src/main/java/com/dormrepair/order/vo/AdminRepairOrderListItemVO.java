package com.dormrepair.order.vo;

import java.time.LocalDateTime;

public record AdminRepairOrderListItemVO(
    Long id,
    String orderNo,
    Long userId,
    String studentName,
    String title,
    Long categoryId,
    String categoryName,
    String dormBuilding,
    String dormRoom,
    String status,
    String priority,
    Long assignedWorkerId,
    String assignedWorkerName,
    String rejectReason,
    String adminRemark,
    String dispatchRemark,
    LocalDateTime submitTime,
    LocalDateTime assignTime
) {
}
