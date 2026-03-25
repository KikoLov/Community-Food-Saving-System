package com.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.food.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户Mapper
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
}
