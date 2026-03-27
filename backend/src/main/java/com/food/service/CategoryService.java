package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.entity.Category;
import com.food.mapper.CategoryMapper;
import com.food.util.DemoTextNormalizeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类服务
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 获取所有启用的分类
     */
    public List<Category> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1).orderByAsc(Category::getSortOrder);
        List<Category> list = categoryMapper.selectList(wrapper);
        list.forEach(DemoTextNormalizeUtil::normalizeCategory);
        return list;
    }

    /**
     * 根据ID获取分类
     */
    public Category getCategoryById(Long categoryId) {
        return DemoTextNormalizeUtil.normalizeCategory(categoryMapper.selectById(categoryId));
    }

    /**
     * 添加分类
     */
    public Category addCategory(Category category) {
        category.setStatus(1);
        category.setCreateTime(LocalDateTime.now());
        categoryMapper.insert(category);
        return category;
    }

    /**
     * 更新分类
     */
    public Category updateCategory(Category category) {
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(category);
        return category;
    }

    /**
     * 删除分类
     */
    public void deleteCategory(Long categoryId) {
        categoryMapper.deleteById(categoryId);
    }
}
