package com.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.food.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 查询商品列表(居民端) - 按社区
     */
    @Select("""
        SELECT p.*, m.merchant_name, c.category_name
        FROM biz_product p
        LEFT JOIN biz_merchant m ON p.merchant_id = m.merchant_id
        LEFT JOIN biz_category c ON p.category_id = c.category_id
        WHERE p.status = 1
          AND p.deleted = 0
          AND p.stock > 0
          AND p.expire_datetime > NOW()
          AND m.deleted = 0
          AND m.community_id = #{communityId}
        ORDER BY p.expire_datetime ASC
        """)
    List<Product> selectProductListByCommunity(@Param("communityId") Long communityId);

    /**
     * 查询商品列表(商家端) - 按商家ID
     */
    @Select("""
        SELECT p.*, c.category_name
        FROM biz_product p
        LEFT JOIN biz_category c ON p.category_id = c.category_id
        WHERE p.merchant_id = #{merchantId}
          AND p.deleted = 0
        ORDER BY p.create_time DESC
        """)
    List<Product> selectProductListByMerchant(@Param("merchantId") Long merchantId);

    /**
     * 查询预警商品列表
     */
    @Select("""
        SELECT p.*, c.category_name
        FROM biz_product p
        LEFT JOIN biz_category c ON p.category_id = c.category_id
        WHERE p.merchant_id = #{merchantId}
          AND p.deleted = 0
          AND p.status = 1
          AND p.stock > 0
          AND p.expire_datetime > NOW()
          AND TIMESTAMPDIFF(HOUR, NOW(), p.expire_datetime) <= p.warning_hours
        ORDER BY p.expire_datetime ASC
        """)
    List<Product> selectWarningProducts(@Param("merchantId") Long merchantId);
}
