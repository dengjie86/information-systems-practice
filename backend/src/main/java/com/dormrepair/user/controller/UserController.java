package com.dormrepair.user.controller;

import com.dormrepair.auth.vo.LoginUserInfo;
import com.dormrepair.common.constant.UserRoles;
import com.dormrepair.common.exception.BusinessException;
import com.dormrepair.common.result.Result;
import com.dormrepair.common.result.ResultCode;
import com.dormrepair.security.LoginUser;
import com.dormrepair.security.LoginUserContext;
import com.dormrepair.user.dto.UpdateDormRequest;
import com.dormrepair.user.entity.UserEntity;
import com.dormrepair.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Result<LoginUserInfo> me() {
        LoginUser loginUser = LoginUserContext.requireUser();
        UserEntity user = userService.getById(loginUser.userId());
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录状态已失效，请重新登录");
        }
        return Result.success(LoginUserInfo.from(user));
    }

    @PutMapping("/dorm")
    public Result<?> updateDorm(@Valid @RequestBody UpdateDormRequest req) {
        LoginUser loginUser = LoginUserContext.requireUser();
        if (!UserRoles.STUDENT.equals(loginUser.role())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅学生可以维护宿舍信息");
        }
        userService.updateDormInfo(loginUser.userId(), req);
        return Result.success();
    }

    // 管理员拿用户列表
    @GetMapping("/list")
    public Result<List<LoginUserInfo>> list() {
        LoginUser loginUser = LoginUserContext.requireUser();
        if (!UserRoles.ADMIN.equals(loginUser.role())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问");
        }
        List<LoginUserInfo> list = userService.listAll().stream()
            .map(LoginUserInfo::from).toList();
        return Result.success(list);
    }

    // 管理员分派时拿维修人员列表
    @GetMapping("/workers")
    public Result<List<LoginUserInfo>> workers() {
        LoginUser loginUser = LoginUserContext.requireUser();
        if (!UserRoles.ADMIN.equals(loginUser.role())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问");
        }
        List<LoginUserInfo> list = userService.listByRole(UserRoles.WORKER).stream()
            .map(LoginUserInfo::from).toList();
        return Result.success(list);
    }
}
