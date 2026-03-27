package com.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.food.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 购物车Mapper
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 查询用户的购物车列表(带商品详情)
     */
    @Select("""
        SELECT c.*, p.product_name, p.product_image, p.discount_price,
               p.expire_datetime, p.stock as stock
        FROM biz_cart c
        LEFT JOIN biz_product p ON c.product_id = p.product_id
        WHERE c.user_id = #{userId}
          AND c.deleted = 0
          AND p.status = 1
          AND p.stock > 0
        ORDER BY c.create_time DESC
        """)
    List<Cart> selectCartListByUser(@Param("userId") Long userId);
}
