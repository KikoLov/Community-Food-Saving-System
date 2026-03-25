package com.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.food.entity.CarbonLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 低碳日志Mapper
 */
@Mapper
public interface CarbonLogMapper extends BaseMapper<CarbonLog> {

    /**
     * 查询用户的低碳日志列表
     */
    @Select("""
        SELECT * FROM biz_carbon_log
        WHERE user_id = #{userId}
        ORDER BY create_time DESC
        """)
    List<CarbonLog> selectCarbonLogListByUser(@Param("userId") Long userId);

    /**
     * 统计全平台碳减排总量
     */
    @Select("SELECT IFNULL(SUM(carbon_saved), 0) FROM biz_carbon_log WHERE log_type = 1")
    BigDecimal selectTotalCarbonSaved();

    /**
     * 统计全平台挽救食品总重量
     */
    @Select("""
        SELECT IFNULL(SUM(o.quantity * 1.0), 0)
        FROM biz_order o
        WHERE o.order_status = 1
        """)
    BigDecimal selectTotalFoodSaved();

    /**
     * 统计全平台交易总额
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM biz_order WHERE order_status = 1")
    BigDecimal selectTotalTransactionAmount();

    /**
     * 统计全平台订单总数
     */
    @Select("SELECT COUNT(*) FROM biz_order WHERE order_status = 1")
    Long selectTotalOrderCount();
}
