package com.dormrepair.order.dto;

import jakarta.validation.constraints.Size;

public record WorkerAcceptRequest(
    @Size(max = 255, message = "备注长度不能超过255个字符")
    String remark
) {
    public String trimRemark() {
        if (remark == null || remark.trim().isEmpty()) {
            return null;
        }
        return remark.trim();
    }
}
