package com.dormrepair.auth.vo;

public record LoginResponse(String token, LoginUserInfo userInfo) {
}

