package com.dormrepair.category.controller;

import com.dormrepair.category.entity.RepairCategoryEntity;
import com.dormrepair.category.service.CategoryService;
import com.dormrepair.common.constant.UserRoles;
import com.dormrepair.common.exception.BusinessException;
import com.dormrepair.common.result.Result;
import com.dormrepair.common.result.ResultCode;
import com.dormrepair.security.LoginUser;
import com.dormrepair.security.LoginUserContext;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 学生报修时拿分类列表，只返回启用的
    @GetMapping("/list")
    public Result<List<RepairCategoryEntity>> list() {
        return Result.success(categoryService.listEnabled());
    }

    @GetMapping("/all")
    public Result<List<RepairCategoryEntity>> all() {
        checkAdmin();
        return Result.success(categoryService.listAll());
    }

    @PostMapping("/add")
    public Result<?> add(@Valid @RequestBody RepairCategoryEntity entity) {
        checkAdmin();
        categoryService.add(entity);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> update(@Valid @RequestBody RepairCategoryEntity entity) {
        checkAdmin();
        categoryService.update(entity);
        return Result.success();
    }

    // 启用/停用
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        checkAdmin();
        categoryService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        checkAdmin();
        categoryService.deleteUnused(id);
        return Result.success();
    }

    private void checkAdmin() {
        LoginUser user = LoginUserContext.requireUser();
        if (!UserRoles.ADMIN.equals(user.role())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问");
        }
    }
}
