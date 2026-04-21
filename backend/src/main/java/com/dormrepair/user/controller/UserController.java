package com.dormrepair.user.controller;

import com.dormrepair.auth.vo.LoginUserInfo;
import com.dormrepair.common.exception.BusinessException;
import com.dormrepair.common.result.Result;
import com.dormrepair.common.result.ResultCode;
import com.dormrepair.security.LoginUser;
import com.dormrepair.security.LoginUserContext;
import com.dormrepair.user.entity.UserEntity;
import com.dormrepair.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

