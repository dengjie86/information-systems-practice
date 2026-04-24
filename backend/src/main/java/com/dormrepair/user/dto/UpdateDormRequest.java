package com.dormrepair.user.dto;

public record UpdateDormRequest(
    String dormBuilding,
    String dormRoom,
    String phone
) {
}
