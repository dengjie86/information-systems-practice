package com.dormrepair.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkerRejectRequest(
    @NotBlank(message = "拒单原因不能为空")
    @Size(max = 255, message = "拒单原因长度不能超过255个字符")
    String rejectReason
) {
    public String trimRejectReason() {
        return rejectReason == null ? null : rejectReason.trim();
    }
}
