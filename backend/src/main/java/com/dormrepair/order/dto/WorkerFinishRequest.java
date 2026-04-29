package com.dormrepair.order.dto;

import jakarta.validation.constraints.Size;

public record WorkerFinishRequest(
    @Size(max = 2000, message = "维修描述长度不能超过2000个字符")
    String actionDesc,

    @Size(max = 500, message = "图片地址长度不能超过500个字符")
    String resultImage
) {
}
