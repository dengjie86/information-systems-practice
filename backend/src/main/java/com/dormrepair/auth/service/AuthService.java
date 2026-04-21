package com.dormrepair.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dormrepair.auth.dto.LoginRequest;
import com.dormrepair.auth.vo.LoginResponse;
import com.dormrepair.auth.vo.LoginUserInfo;
import com.dormrepair.common.exception.BusinessException;
import com.dormrepair.common.result.ResultCode;
import com.dormrepair.security.JwtTokenProvider;
import com.dormrepair.user.entity.UserEntity;
import com.dormrepair.user.mapper.UserMapper;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Set<String> VALID_ROLES = Set.of("STUDENT", "ADMIN", "WORKER");

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserMapper userMapper, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userMapper.selectOne(
            new QueryWrapper<UserEntity>().eq("username", request.username())
        );

        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        if (!VALID_ROLES.contains(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号角色不合法");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, LoginUserInfo.from(user));
    }
}

