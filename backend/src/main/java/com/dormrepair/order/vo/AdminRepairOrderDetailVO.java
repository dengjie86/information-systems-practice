package com.dormrepair.order.vo;

import java.time.LocalDateTime;

public record AdminRepairOrderDetailVO(
    Long id,
    String orderNo,
    Long userId,
    String studentName,
    String title,
    Long categoryId,
    String categoryName,
    String description,
    String imageUrl,
    String dormBuilding,
    String dormRoom,
    String contactPhone,
    String status,
    String priority,
    Long assignedWorkerId,
    String assignedWorkerName,
    String rejectReason,
    String adminRemark,
    String dispatchRemark,
    LocalDateTime submitTime,
    LocalDateTime assignTime,
    LocalDateTime acceptTime,
    LocalDateTime finishTime,
    LocalDateTime closeTime
) {
}
