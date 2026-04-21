package com.dormrepair.user.service;

import com.dormrepair.user.entity.UserEntity;
import com.dormrepair.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserEntity getById(Long id) {
        return userMapper.selectById(id);
    }
}

