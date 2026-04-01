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
import com.food.util.DemoTextNormalizeUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final JdbcTemplate jdbcTemplate;

    private static final String STOCK_KEY_PREFIX = "product:stock:";
    /**
     * 动态定价默认窗口（小时）：用于缺少创建时间时估算价格衰减区间
     */
    private static final long DEFAULT_PRICING_WINDOW_HOURS = 72;

    @Value("${app.pricing.strategy:exponential}")
    private String pricingStrategy;

    @Value("${app.pricing.exponential.base-lambda:3.2}")
    private double baseLambda;

    @PostConstruct
    public void ensureDynamicPricingColumn() {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_product' AND COLUMN_NAME = 'min_price'",
                Integer.class
        );
        if (exists == null || exists == 0) {
            jdbcTemplate.execute("ALTER TABLE biz_product ADD COLUMN min_price DECIMAL(10,2) DEFAULT NULL COMMENT '最低底价'");
        }
        jdbcTemplate.execute("UPDATE biz_product SET min_price = discount_price WHERE min_price IS NULL");

        Integer surpriseExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_product' AND COLUMN_NAME = 'surprise_bag'",
                Integer.class
        );
        if (surpriseExists == null || surpriseExists == 0) {
            jdbcTemplate.execute("ALTER TABLE biz_product ADD COLUMN surprise_bag TINYINT(1) DEFAULT 0 COMMENT '是否盲盒'");
        }

        Integer bagValueExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_product' AND COLUMN_NAME = 'bag_value'",
                Integer.class
        );
        if (bagValueExists == null || bagValueExists == 0) {
            jdbcTemplate.execute("ALTER TABLE biz_product ADD COLUMN bag_value DECIMAL(10,2) DEFAULT NULL COMMENT '盲盒名义价值'");
        }
    }

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
        if (productDTO.getSurpriseBag() != null && productDTO.getSurpriseBag()) {
            product.setSurpriseBag(1);
            if (productDTO.getBagValue() == null || productDTO.getBagValue().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("盲盒名义价值必须大于0");
            }
            product.setBagValue(productDTO.getBagValue());
            if (!product.getProductName().contains("盲盒")) {
                product.setProductName("【盲盒】" + product.getProductName());
            }
        } else {
            product.setSurpriseBag(0);
            product.setBagValue(null);
        }
        normalizeAndApplyDynamicPrice(product, productDTO.getExpireDatetime());
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
        if (productDTO.getSurpriseBag() != null && productDTO.getSurpriseBag()) {
            product.setSurpriseBag(1);
            if (productDTO.getBagValue() == null || productDTO.getBagValue().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("盲盒名义价值必须大于0");
            }
            product.setBagValue(productDTO.getBagValue());
            if (!product.getProductName().contains("盲盒")) {
                product.setProductName("【盲盒】" + product.getProductName());
            }
        } else {
            product.setSurpriseBag(0);
            product.setBagValue(null);
        }
        normalizeAndApplyDynamicPrice(product, productDTO.getExpireDatetime());
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
        Page<Product> result = productMapper.selectPage(page, wrapper);
        if (result.getRecords() != null) {
            result.getRecords().forEach(DemoTextNormalizeUtil::normalizeProduct);
        }
        return result;
    }

    /**
     * 居民获取商品列表(按社区)
     */
    public List<Product> getProductsByCommunity(Long communityId) {
        List<Product> list = productMapper.selectProductListByCommunity(communityId);
        if (list != null) {
            list.forEach(DemoTextNormalizeUtil::normalizeProduct);
        }
        return list;
    }

    /**
     * 获取预警商品列表
     */
    public List<Product> getWarningProducts(Long merchantId) {
        List<Product> list = productMapper.selectWarningProducts(merchantId);
        if (list != null) {
            list.forEach(DemoTextNormalizeUtil::normalizeProduct);
        }
        return list;
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
                product.setCategoryName(DemoTextNormalizeUtil.normalizeCategoryName(category.getCategoryName()));
            }
        }
        return DemoTextNormalizeUtil.normalizeProduct(product);
    }

    /**
     * 定时刷新动态定价（每分钟）
     */
    @Scheduled(cron = "${app.pricing.refresh-cron:0 * * * * ?}")
    @Transactional
    public void refreshDynamicPricingJob() {
        refreshDynamicPricingNow();
    }

    /**
     * 立即刷新动态定价（可被手动调用）
     */
    @Transactional
    public int refreshDynamicPricingNow() {
        LocalDateTime now = LocalDateTime.now();
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .gt(Product::getExpireDatetime, now)
                        .in(Product::getStatus, 0, 1)
                        .gt(Product::getStock, 0)
        );
        Map<Long, BigDecimal> categoryFactorMap = categoryMapper.selectList(null).stream()
                .filter(c -> c.getCategoryId() != null)
                .collect(Collectors.toMap(
                        Category::getCategoryId,
                        c -> c.getCarbonFactor() != null ? BigDecimal.valueOf(c.getCarbonFactor()) : BigDecimal.valueOf(1.5),
                        (a, b) -> a
                ));
        int changed = 0;
        for (Product p : products) {
            BigDecimal original = safeMoney(p.getOriginalPrice());
            BigDecimal min = resolveMinPrice(p);
            BigDecimal computed = calculateDynamicPrice(
                    original,
                    min,
                    p.getCreateTime(),
                    p.getExpireDatetime(),
                    now,
                    categoryFactorMap.get(p.getCategoryId())
            );
            if (p.getDiscountPrice() == null || p.getDiscountPrice().compareTo(computed) != 0 || p.getMinPrice() == null) {
                Product patch = new Product();
                patch.setProductId(p.getProductId());
                patch.setMinPrice(min);
                patch.setDiscountPrice(computed);
                patch.setUpdateTime(now);
                productMapper.updateById(patch);
                changed++;
            }
        }
        return changed;
    }

    private void normalizeAndApplyDynamicPrice(Product product, LocalDateTime expireDatetime) {
        BigDecimal original = safeMoney(product.getOriginalPrice());
        if (original.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("原价必须大于0");
        }
        BigDecimal min = resolveMinPrice(product);
        if (min.compareTo(original) > 0) {
            throw new RuntimeException("最低底价不能高于原价");
        }
        if (expireDatetime == null) {
            throw new RuntimeException("过期时间不能为空");
        }
        product.setOriginalPrice(original);
        product.setMinPrice(min);
        Category category = product.getCategoryId() != null ? categoryMapper.selectById(product.getCategoryId()) : null;
        BigDecimal categoryFactor = category != null && category.getCarbonFactor() != null
                ? BigDecimal.valueOf(category.getCarbonFactor())
                : null;
        product.setDiscountPrice(calculateDynamicPrice(
                original,
                min,
                product.getCreateTime(),
                expireDatetime,
                LocalDateTime.now(),
                categoryFactor
        ));
    }

    private BigDecimal resolveMinPrice(Product product) {
        if (product.getMinPrice() != null) {
            return safeMoney(product.getMinPrice());
        }
        if (product.getDiscountPrice() != null) {
            return safeMoney(product.getDiscountPrice());
        }
        BigDecimal fallback = safeMoney(product.getOriginalPrice()).multiply(new BigDecimal("0.50"));
        return safeMoney(fallback);
    }

    private BigDecimal safeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 线性时间衰减：price = min + (original - min) * ratio
     */
    private BigDecimal calculateDynamicPrice(BigDecimal original,
                                             BigDecimal min,
                                             LocalDateTime createTime,
                                             LocalDateTime expireTime,
                                             LocalDateTime now,
                                             BigDecimal categoryFactor) {
        if (expireTime == null || !expireTime.isAfter(now)) {
            return min;
        }
        LocalDateTime start = createTime != null && createTime.isBefore(expireTime)
                ? createTime
                : expireTime.minusHours(DEFAULT_PRICING_WINDOW_HOURS);
        long totalSeconds = Math.max(1, Duration.between(start, expireTime).getSeconds());
        long remainSeconds = Math.max(0, Duration.between(now, expireTime).getSeconds());
        BigDecimal ratio = BigDecimal.valueOf(remainSeconds)
                .divide(BigDecimal.valueOf(totalSeconds), 6, RoundingMode.HALF_UP);
        BigDecimal progress = BigDecimal.ONE.subtract(ratio); // 0~1，越接近到期越大

        BigDecimal result;
        if ("tiered".equalsIgnoreCase(pricingStrategy)) {
            result = calculateTieredPrice(original, min, ratio);
        } else if ("linear".equalsIgnoreCase(pricingStrategy)) {
            result = min.add(original.subtract(min).multiply(ratio));
        } else {
            result = calculateExponentialPrice(original, min, progress, categoryFactor);
        }
        if (result.compareTo(original) > 0) {
            result = original;
        }
        if (result.compareTo(min) < 0) {
            result = min;
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateExponentialPrice(BigDecimal original,
                                                 BigDecimal min,
                                                 BigDecimal progress,
                                                 BigDecimal categoryFactor) {
        double factor = categoryFactor == null
                ? 1.0
                : Math.max(0.6, Math.min(1.8, categoryFactor.doubleValue() / 2.0));
        double lambda = Math.max(0.2, baseLambda * factor);
        double x = Math.max(0d, Math.min(1d, progress.doubleValue()));
        double decay = Math.exp(-lambda * x);
        BigDecimal span = original.subtract(min);
        return min.add(span.multiply(BigDecimal.valueOf(decay)));
    }

    /**
     * 阶梯折扣：按剩余保质期比例进行离散折扣，并受最低价约束。
     */
    private BigDecimal calculateTieredPrice(BigDecimal original, BigDecimal min, BigDecimal remainRatio) {
        double r = Math.max(0d, Math.min(1d, remainRatio.doubleValue()));
        BigDecimal discountRate;
        if (r > 0.30d) {
            discountRate = new BigDecimal("0.95");
        } else if (r > 0.20d) {
            discountRate = new BigDecimal("0.85");
        } else if (r > 0.05d) {
            discountRate = new BigDecimal("0.50");
        } else {
            discountRate = new BigDecimal("0.20");
        }
        BigDecimal tierPrice = original.multiply(discountRate);
        if (tierPrice.compareTo(min) < 0) {
            return min;
        }
        return tierPrice;
    }

    /**
     * 管理端/论文辅助：生成价格曲线采样点。
     */
    public List<Map<String, Object>> buildPricingCurve(BigDecimal original,
                                                       BigDecimal min,
                                                       int totalHours,
                                                       BigDecimal categoryFactor,
                                                       String strategy) {
        int safeHours = Math.max(1, totalHours);
        LocalDateTime expire = LocalDateTime.now().plusHours(safeHours);
        LocalDateTime start = LocalDateTime.now();
        String useStrategy = (strategy == null || strategy.isBlank()) ? pricingStrategy : strategy;
        java.util.ArrayList<Map<String, Object>> points = new java.util.ArrayList<>();
        for (int h = 0; h <= safeHours; h++) {
            LocalDateTime now = start.plusHours(h);
            BigDecimal price = calculateDynamicPriceByStrategy(
                    original,
                    min,
                    start,
                    expire,
                    now,
                    categoryFactor,
                    useStrategy
            );
            points.add(Map.of(
                    "hour", h,
                    "price", price
            ));
        }
        return points;
    }

    private BigDecimal calculateDynamicPriceByStrategy(BigDecimal original,
                                                       BigDecimal min,
                                                       LocalDateTime createTime,
                                                       LocalDateTime expireTime,
                                                       LocalDateTime now,
                                                       BigDecimal categoryFactor,
                                                       String strategy) {
        if (expireTime == null || !expireTime.isAfter(now)) {
            return min;
        }
        LocalDateTime start = createTime != null && createTime.isBefore(expireTime)
                ? createTime
                : expireTime.minusHours(DEFAULT_PRICING_WINDOW_HOURS);
        long totalSeconds = Math.max(1, Duration.between(start, expireTime).getSeconds());
        long remainSeconds = Math.max(0, Duration.between(now, expireTime).getSeconds());
        BigDecimal ratio = BigDecimal.valueOf(remainSeconds)
                .divide(BigDecimal.valueOf(totalSeconds), 6, RoundingMode.HALF_UP);
        BigDecimal progress = BigDecimal.ONE.subtract(ratio);

        BigDecimal result;
        if ("tiered".equalsIgnoreCase(strategy)) {
            result = calculateTieredPrice(original, min, ratio);
        } else if ("linear".equalsIgnoreCase(strategy)) {
            result = min.add(original.subtract(min).multiply(ratio));
        } else {
            result = calculateExponentialPrice(original, min, progress, categoryFactor);
        }
        if (result.compareTo(original) > 0) {
            result = original;
        }
        if (result.compareTo(min) < 0) {
            result = min;
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }
}
