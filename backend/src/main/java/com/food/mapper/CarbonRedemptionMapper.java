package com.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.food.entity.CarbonRedemption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CarbonRedemptionMapper extends BaseMapper<CarbonRedemption> {

    @Select("""
            SELECT COUNT(*) FROM biz_carbon_redemption
            WHERE user_id = #{userId} AND category = 'TREE' AND deleted = 0
            """)
    long countTreesByUser(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*) FROM biz_carbon_redemption
            WHERE user_id = #{userId} AND item_code = #{itemCode} AND category = 'BADGE' AND deleted = 0
            """)
    long countBadgeByUserAndCode(@Param("userId") Long userId, @Param("itemCode") String itemCode);
}
