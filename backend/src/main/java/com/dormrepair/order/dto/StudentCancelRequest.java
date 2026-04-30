package com.dormrepair.order.dto;

import jakarta.validation.constraints.Size;

public record StudentCancelRequest(
    @Size(max = 255, message = "取消原因长度不能超过255个字符")
    String cancelReason
) {}
