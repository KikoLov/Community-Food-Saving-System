package com.food.dto;

import com.food.entity.Merchant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 管理端商户详情与经营统计
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantAdminStatsDTO {

    private Merchant merchant;

    /** 今日销售额（已下单金额合计） */
    private BigDecimal todaySales;

    /** 今日订单数 */
    private Long todayOrderCount;

    /** 本月销售额 */
    private BigDecimal monthSales;

    /** 本月订单数 */
    private Long monthOrderCount;

    /** 待核销订单数 */
    private Long pendingVerifyCount;

    /** 累计已核销订单数 */
    private Long totalVerifiedCount;

    /** 商品在售数量 */
    private Long onSaleProductCount;
}
