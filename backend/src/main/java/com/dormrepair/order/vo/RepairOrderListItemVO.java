package com.dormrepair.order.vo;

import java.time.LocalDateTime;

public record RepairOrderListItemVO(
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
    LocalDateTime submitTime
) {
}
