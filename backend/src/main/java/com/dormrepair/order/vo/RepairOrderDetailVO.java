package com.dormrepair.order.vo;

import java.time.LocalDateTime;
import java.util.List;

public record RepairOrderDetailVO(
    Long id,
    String orderNo,
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
    String rejectReason,
    String adminRemark,
    String dispatchRemark,
    LocalDateTime submitTime,
    LocalDateTime assignTime,
    LocalDateTime acceptTime,
    LocalDateTime finishTime,
    LocalDateTime closeTime,
    EvaluationVO evaluation,
    List<RepairRecordVO> records
) {
}
