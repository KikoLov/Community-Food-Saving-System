package com.food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 用户资料扩展表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_user_profile")
public class UserProfile extends BaseEntity {

    /**
     * 资料ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long profileId;

    /**
     * 关联sys_user的ID
     */
    private Long userId;

    /**
     * 所属社区ID
     */
    private Long communityId;

    /**
     * 低碳积分
     */
    private BigDecimal carbonPoints;

    /**
     * 累计碳减排量(kg CO2)
     */
    private BigDecimal totalCarbonSaved;

    /**
     * 累计挽救食品重量(kg)
     */
    private BigDecimal totalFoodSaved;

    /**
     * 冗余字段：社区名称
     */
    @TableField(exist = false)
    private String communityName;

    /**
     * 冗余字段：用户名
     */
    @TableField(exist = false)
    private String userName;
}
