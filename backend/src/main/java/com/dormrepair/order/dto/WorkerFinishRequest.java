package com.dormrepair.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkerFinishRequest(
    @NotBlank(message = "完成说明不能为空")
    @Size(max = 2000, message = "维修描述长度不能超过2000个字符")
    String actionDesc,

    @Size(max = 500, message = "图片地址长度不能超过500个字符")
    String resultImage
) {
    public String requireActionDesc() {
        if (actionDesc == null || actionDesc.trim().isEmpty()) {
            throw new IllegalArgumentException("actionDesc");
        }
        return actionDesc.trim();
    }
}
