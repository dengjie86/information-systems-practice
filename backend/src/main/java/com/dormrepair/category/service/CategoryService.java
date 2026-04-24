package com.dormrepair.category.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dormrepair.category.entity.RepairCategoryEntity;
import com.dormrepair.category.mapper.RepairCategoryMapper;
import com.dormrepair.common.exception.BusinessException;
import com.dormrepair.common.result.ResultCode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final RepairCategoryMapper categoryMapper;

    public CategoryService(RepairCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

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
        assertCategoryNameUnique(entity.getCategoryName(), null);
        entity.setCategoryName(entity.getCategoryName().trim());
        entity.setSortOrder(entity.getSortOrder() == null ? 0 : entity.getSortOrder());
        entity.setStatus(1);
        categoryMapper.insert(entity);
    }

    public void update(RepairCategoryEntity entity) {
        if (entity.getId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "分类ID不能为空");
        }
        RepairCategoryEntity existed = categoryMapper.selectById(entity.getId());
        if (existed == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }
        assertCategoryNameUnique(entity.getCategoryName(), entity.getId());
        entity.setCategoryName(entity.getCategoryName().trim());
        entity.setSortOrder(entity.getSortOrder() == null ? existed.getSortOrder() : entity.getSortOrder());
        categoryMapper.updateById(entity);
    }

    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "状态值只能是0或1");
        }
        RepairCategoryEntity cat = categoryMapper.selectById(id);
        if (cat == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }
        cat.setStatus(status);
        categoryMapper.updateById(cat);
    }

    private void assertCategoryNameUnique(String categoryName, Long excludeId) {
        QueryWrapper<RepairCategoryEntity> queryWrapper = new QueryWrapper<RepairCategoryEntity>()
            .eq("category_name", categoryName.trim());
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        if (categoryMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "分类名称已存在");
        }
    }
}
