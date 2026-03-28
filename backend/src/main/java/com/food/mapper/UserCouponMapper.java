package com.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.food.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    @Select("""
            SELECT * FROM biz_user_coupon
            WHERE user_id = #{userId} AND status = 0 AND deleted = 0
            ORDER BY create_time DESC
            """)
    List<UserCoupon> selectUnusedByUser(@Param("userId") Long userId);
}
