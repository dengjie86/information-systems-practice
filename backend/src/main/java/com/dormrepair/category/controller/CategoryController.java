package com.dormrepair.category.controller;

import com.dormrepair.category.entity.RepairCategoryEntity;
import com.dormrepair.category.service.CategoryService;
import com.dormrepair.common.result.Result;
import com.dormrepair.security.LoginUser;
import com.dormrepair.security.LoginUserContext;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

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
    public Result<?> add(@RequestBody RepairCategoryEntity entity) {
        checkAdmin();
        categoryService.add(entity);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody RepairCategoryEntity entity) {
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

    private void checkAdmin() {
        LoginUser user = LoginUserContext.requireUser();
        if (!"ADMIN".equals(user.role())) {
            throw new RuntimeException("没有权限");
        }
    }
}
