package com.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社区信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_community")
public class Community extends BaseEntity {

    /**
     * 社区ID
     */
    @TableId(type = IdType.AUTO)
    private Long communityId;

    /**
     * 社区名称
     */
    private String communityName;

    /**
     * 社区编码
     */
    private String communityCode;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;
}
