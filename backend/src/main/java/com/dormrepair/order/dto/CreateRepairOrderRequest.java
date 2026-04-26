package com.dormrepair.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRepairOrderRequest(
    @NotBlank(message = "工单标题不能为空")
    @Size(max = 100, message = "工单标题不能超过100个字符")
    String title,

    @NotNull(message = "故障分类不能为空")
    Long categoryId,

    @Size(max = 2000, message = "问题描述不能超过2000个字符")
    String description,

    @Size(max = 500, message = "图片地址不能超过500个字符")
    String imageUrl,

    @Size(max = 20, message = "联系电话不能超过20个字符")
    String contactPhone,

    @Pattern(
        regexp = "LOW|NORMAL|HIGH|URGENT",
        message = "优先级只能是 LOW、NORMAL、HIGH、URGENT"
    )
    String priority
) {
}
