package com.dormrepair.order.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminOrderAssignRequest(
    @NotNull(message = "维修人员ID不能为空")
    Long workerId,

    @Size(max = 255, message = "分派备注长度不能超过255个字符")
    @JsonAlias("adminRemark")
    String dispatchRemark
) {
    public String trimDispatchRemark() {
        if (dispatchRemark == null || dispatchRemark.trim().isEmpty()) {
            return null;
        }
        return dispatchRemark.trim();
    }
}
