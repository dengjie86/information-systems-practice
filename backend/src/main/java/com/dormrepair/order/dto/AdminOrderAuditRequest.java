package com.dormrepair.order.dto;

import jakarta.validation.constraints.Size;

public record AdminOrderAuditRequest(
    @Size(max = 255, message = "审核备注长度不能超过255个字符")
    String adminRemark,

    @Size(max = 255, message = "驳回原因长度不能超过255个字符")
    String rejectReason
) {
    public String requireRejectReason() {
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new IllegalArgumentException("rejectReason");
        }
        return rejectReason.trim();
    }

    public String trimAdminRemark() {
        if (adminRemark == null || adminRemark.trim().isEmpty()) {
            return null;
        }
        return adminRemark.trim();
    }
}
