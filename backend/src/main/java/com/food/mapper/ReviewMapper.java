package com.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.food.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("""
        SELECT r.*, u.user_name
        FROM biz_review r
        LEFT JOIN sys_user u ON r.user_id = u.user_id
        WHERE r.merchant_id = #{merchantId}
          AND r.deleted = 0
        ORDER BY r.create_time DESC
        """)
    List<Review> selectByMerchant(@Param("merchantId") Long merchantId);

    @Select("""
        SELECT r.*, u.user_name
        FROM biz_review r
        LEFT JOIN sys_user u ON r.user_id = u.user_id
        WHERE r.user_id = #{userId}
          AND r.deleted = 0
        ORDER BY r.create_time DESC
        """)
    List<Review> selectByUser(@Param("userId") Long userId);

    @Select("""
        SELECT r.*, u.user_name
        FROM biz_review r
        LEFT JOIN sys_user u ON r.user_id = u.user_id
        WHERE r.merchant_id = #{merchantId}
          AND r.deleted = 0
        ORDER BY r.create_time DESC
        LIMIT #{limit}
        """)
    List<Review> selectLatestByMerchant(@Param("merchantId") Long merchantId, @Param("limit") Integer limit);
}

