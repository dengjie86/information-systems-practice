package com.dormrepair.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dormrepair.user.dto.UpdateDormRequest;
import com.dormrepair.user.entity.UserEntity;
import com.dormrepair.user.mapper.UserMapper;
import java.util.List;
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

    public void updateDormInfo(Long userId, UpdateDormRequest req) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setDormBuilding(req.dormBuilding());
        user.setDormRoom(req.dormRoom());
        user.setPhone(req.phone());
        userMapper.updateById(user);
    }

    public List<UserEntity> listByRole(String role) {
        return userMapper.selectList(
            new QueryWrapper<UserEntity>().eq("role", role).eq("status", 1)
        );
    }

    public List<UserEntity> listAll() {
        return userMapper.selectList(null);
    }
}

