package com.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 食品分类表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_category")
public class Category extends BaseEntity {

    /**
     * 分类ID
     */
    @TableId(type = IdType.AUTO)
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类编码
     */
    private String categoryCode;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 碳排放因子 (kg CO2/kg食品)
     */
    private Double carbonFactor;
}
