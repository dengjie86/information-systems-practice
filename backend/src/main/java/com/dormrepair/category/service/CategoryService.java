package com.dormrepair.category.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dormrepair.category.entity.RepairCategoryEntity;
import com.dormrepair.category.mapper.RepairCategoryMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    @Autowired
    private RepairCategoryMapper categoryMapper;

    public List<RepairCategoryEntity> listEnabled() {
        return categoryMapper.selectList(
            new QueryWrapper<RepairCategoryEntity>()
                .eq("status", 1)
                .orderByAsc("sort_order")
        );
    }

    public List<RepairCategoryEntity> listAll() {
        return categoryMapper.selectList(
            new QueryWrapper<RepairCategoryEntity>().orderByAsc("sort_order")
        );
    }

    public void add(RepairCategoryEntity entity) {
        entity.setStatus(1);
        categoryMapper.insert(entity);
    }

    public void update(RepairCategoryEntity entity) {
        if (categoryMapper.selectById(entity.getId()) == null) {
            throw new RuntimeException("分类不存在");
        }
        categoryMapper.updateById(entity);
    }

    public void updateStatus(Long id, Integer status) {
        RepairCategoryEntity cat = categoryMapper.selectById(id);
        if (cat == null) {
            throw new RuntimeException("分类不存在");
        }
        cat.setStatus(status);
        categoryMapper.updateById(cat);
    }
}
