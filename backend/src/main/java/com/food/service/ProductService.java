package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.food.dto.ProductDTO;
import com.food.entity.Category;
import com.food.entity.Merchant;
import com.food.entity.Product;
import com.food.mapper.CategoryMapper;
import com.food.mapper.MerchantMapper;
import com.food.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final MerchantMapper merchantMapper;
    private final CategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STOCK_KEY_PREFIX = "product:stock:";

    /**
     * 商户添加商品
     */
    @Transactional
    public Product addProduct(ProductDTO productDTO) {
        // 校验商品过期时间
        if (productDTO.getExpireDatetime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("过期时间必须晚于当前时间");
        }

        // 获取商户信息
        Merchant merchant = merchantMapper.selectById(productDTO.getMerchantId());
        if (merchant == null) {
            throw new RuntimeException("商户不存在");
        }

        // 转换DTO为实体
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        if (product.getWarningHours() == null) {
            product.setWarningHours(24);
        }
        if (product.getUnit() == null) {
            product.setUnit("件");
        }
        if (product.getStatus() == null) {
            product.setStatus(1); // 上架
        }
        product.setCreateTime(LocalDateTime.now());
        productMapper.insert(product);

        // 初始化Redis库存
        redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + product.getProductId(), product.getStock(), 7, TimeUnit.DAYS);

        return product;
    }

    /**
     * 商户更新商品
     */
    @Transactional
    public Product updateProduct(ProductDTO productDTO) {
        Product product = productMapper.selectById(productDTO.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 校验商品过期时间
        if (productDTO.getExpireDatetime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("过期时间必须晚于当前时间");
        }

        BeanUtils.copyProperties(productDTO, product);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);

        // 更新Redis库存
        redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + product.getProductId(), product.getStock(), 7, TimeUnit.DAYS);

        return product;
    }

    /**
     * 商户删除商品
     */
    public void deleteProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        productMapper.deleteById(productId);
        redisTemplate.delete(STOCK_KEY_PREFIX + productId);
    }

    /**
     * 批量更新商品上/下架状态（仅商户自己的商品）
     */
    @Transactional
    public int batchUpdateStatus(Long merchantId, List<Long> productIds, Integer status) {
        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Long productId : productIds) {
            Product product = productMapper.selectById(productId);
            if (product == null) {
                continue;
            }
            if (!Objects.equals(product.getMerchantId(), merchantId)) {
                continue;
            }
            product.setStatus(status);
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);
            count++;
        }
        return count;
    }

    /**
     * 批量删除商品（仅商户自己的商品）
     */
    @Transactional
    public int batchDelete(Long merchantId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Long productId : productIds) {
            Product product = productMapper.selectById(productId);
            if (product == null) {
                continue;
            }
            if (!Objects.equals(product.getMerchantId(), merchantId)) {
                continue;
            }
            productMapper.deleteById(productId);
            redisTemplate.delete(STOCK_KEY_PREFIX + productId);
            count++;
        }
        return count;
    }

    /**
     * 商户获取商品列表
     */
    public Page<Product> getMerchantProducts(Long merchantId, int pageNum, int pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getMerchantId, merchantId).orderByDesc(Product::getCreateTime);
        return productMapper.selectPage(page, wrapper);
    }

    /**
     * 居民获取商品列表(按社区)
     */
    public List<Product> getProductsByCommunity(Long communityId) {
        return productMapper.selectProductListByCommunity(communityId);
    }

    /**
     * 获取预警商品列表
     */
    public List<Product> getWarningProducts(Long merchantId) {
        return productMapper.selectWarningProducts(merchantId);
    }

    /**
     * 扣减库存(使用Redis)
     */
    public boolean decreaseStock(Long productId, Integer quantity) {
        String stockKey = STOCK_KEY_PREFIX + productId;
        Object stockObj = redisTemplate.opsForValue().get(stockKey);

        if (stockObj == null) {
            // Redis中没有，从数据库加载
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStock() < quantity) {
                return false;
            }
            redisTemplate.opsForValue().set(stockKey, product.getStock(), 7, TimeUnit.DAYS);
            stockObj = product.getStock();
        }

        Integer stock = Integer.parseInt(stockObj.toString());
        if (stock < quantity) {
            return false;
        }

        // 扣减库存
        Long result = redisTemplate.opsForValue().decrement(stockKey, quantity);
        if (result < 0) {
            // 库存不足，回滚
            redisTemplate.opsForValue().increment(stockKey, quantity);
            return false;
        }

        // 同步到数据库
        Product product = productMapper.selectById(productId);
        product.setStock(product.getStock() - quantity);
        if (product.getStock() <= 0) {
            product.setStatus(2); // 已售罄
        }
        productMapper.updateById(product);

        return true;
    }

    /**
     * 回滚库存
     */
    public void rollbackStock(Long productId, Integer quantity) {
        String stockKey = STOCK_KEY_PREFIX + productId;
        redisTemplate.opsForValue().increment(stockKey, quantity);

        Product product = productMapper.selectById(productId);
        product.setStock(product.getStock() + quantity);
        if (product.getStatus() == 2 && product.getStock() > 0) {
            product.setStatus(1); // 恢复上架
        }
        productMapper.updateById(product);
    }

    /**
     * 获取商品详情
     */
    public Product getProductById(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product != null && product.getCategoryId() != null) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                product.setCategoryName(category.getCategoryName());
            }
        }
        return product;
    }
}
