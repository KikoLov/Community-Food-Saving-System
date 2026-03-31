package com.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.food.dto.CommunityMerchantDTO;
import com.food.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商户Mapper
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
    /**
     * 按社区查询商家（含在售商品数，允许为0）
     */
    @Select("""
        SELECT m.merchant_id,
               m.merchant_name,
               m.community_id,
               CAST(COALESCE(SUM(
                 CASE
                   WHEN p.deleted = 0
                    AND p.status = 1
                    AND p.stock > 0
                    AND p.expire_datetime > NOW()
                   THEN 1 ELSE 0
                 END
               ), 0) AS SIGNED) AS product_count
        FROM biz_merchant m
        LEFT JOIN biz_product p ON p.merchant_id = m.merchant_id
        WHERE m.deleted = 0
          AND m.community_id = #{communityId}
        GROUP BY m.merchant_id, m.merchant_name, m.community_id
        ORDER BY m.merchant_id DESC
        """)
    List<CommunityMerchantDTO> selectCommunityMerchants(@Param("communityId") Long communityId);
}
