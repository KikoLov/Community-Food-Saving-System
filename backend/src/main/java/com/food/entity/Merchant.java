package com.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商户信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_merchant")
public class Merchant extends BaseEntity {

    /**
     * 商户ID
     */
    @TableId(type = IdType.AUTO)
    private Long merchantId;

    /**
     * 关联sys_user的ID
     */
    private Long userId;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 营业执照图片URL
     */
    private String businessLicense;

    /**
     * 资质状态: 0-待审核 1-已通过 2-已拒绝
     */
    private Integer licenseStatus;

    /**
     * 营业时间，如: 08:00-22:00
     */
    private String openingHours;

    /**
     * 店铺描述
     */
    private String description;

    /**
     * 社区ID (主要服务社区)
     */
    private Long communityId;
}
