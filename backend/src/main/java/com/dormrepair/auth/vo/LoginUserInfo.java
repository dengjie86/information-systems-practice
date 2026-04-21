package com.dormrepair.auth.vo;

import com.dormrepair.user.entity.UserEntity;

public record LoginUserInfo(
    Long id,
    String username,
    String realName,
    String role,
    String phone,
    String dormBuilding,
    String dormRoom,
    Integer status
) {

    public static LoginUserInfo from(UserEntity user) {
        return new LoginUserInfo(
            user.getId(),
            user.getUsername(),
            user.getRealName(),
            user.getRole(),
            user.getPhone(),
            user.getDormBuilding(),
            user.getDormRoom(),
            user.getStatus()
        );
    }
}

