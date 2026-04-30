package com.dormrepair.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudentEvaluateRequest(
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    Integer score,

    @Size(max = 500, message = "评价内容长度不能超过500个字符")
    String content
) {}
