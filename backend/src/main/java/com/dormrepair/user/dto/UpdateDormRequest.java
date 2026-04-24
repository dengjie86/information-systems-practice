package com.dormrepair.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateDormRequest(
    @NotBlank(message = "宿舍楼不能为空")
    String dormBuilding,
    @NotBlank(message = "宿舍房间不能为空")
    String dormRoom,
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    String phone
) {
}
