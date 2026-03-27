package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.dto.MerchantRatingSummaryDTO;
import com.food.dto.ReviewCreateDTO;
import com.food.entity.Order;
import com.food.entity.Review;
import com.food.mapper.OrderMapper;
import com.food.mapper.ReviewMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final JdbcTemplate jdbcTemplate;

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Value("${file.public.base-url:http://localhost:8080}")
    private String filePublicBaseUrl;

    @PostConstruct
    public void initTable() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS biz_review (
              review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
              order_id BIGINT NOT NULL,
              user_id BIGINT NOT NULL,
              merchant_id BIGINT NOT NULL,
              product_id BIGINT NOT NULL,
              product_name VARCHAR(100),
              rating INT NOT NULL,
              content VARCHAR(1000),
              image_url VARCHAR(500),
              reply_content VARCHAR(1000),
              reply_time DATETIME,
              create_by VARCHAR(50),
              create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
              update_by VARCHAR(50),
              update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
              deleted INT DEFAULT 0,
              UNIQUE KEY uk_review_order_user (order_id, user_id)
            )
            """);
    }

    @Transactional
    public Review createReview(Long userId, ReviewCreateDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权限评价该订单");
        }
        if (order.getOrderStatus() == null || order.getOrderStatus() != 1) {
            throw new RuntimeException("仅已核销订单可评价");
        }
        Long existed = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, dto.getOrderId())
                .eq(Review::getUserId, userId));
        if (existed != null && existed > 0) {
            throw new RuntimeException("该订单已评价，请勿重复提交");
        }

        Review review = new Review();
        BeanUtils.copyProperties(dto, review);
        review.setUserId(userId);
        review.setOrderId(order.getOrderId());
        review.setMerchantId(order.getMerchantId());
        review.setProductId(order.getProductId());
        review.setProductName(order.getProductName());
        review.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(review);
        return review;
    }

    public List<Review> getMerchantReviews(Long merchantId) {
        return reviewMapper.selectByMerchant(merchantId);
    }

    public List<Review> getUserReviews(Long userId) {
        return reviewMapper.selectByUser(userId);
    }

    @Transactional
    public Review replyReview(Long merchantId, Long reviewId, String replyContent) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        if (!review.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权限回复该评价");
        }
        review.setReplyContent(replyContent);
        review.setReplyTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        reviewMapper.updateById(review);
        return review;
    }

    public MerchantRatingSummaryDTO getMerchantSummary(Long merchantId) {
        List<Review> all = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getMerchantId, merchantId)
                        .eq(Review::getDeleted, 0)
        );
        if (all.isEmpty()) {
            return new MerchantRatingSummaryDTO(merchantId, 0, 0.0, 0.0);
        }
        int total = all.size();
        int good = (int) all.stream().filter(r -> r.getRating() != null && r.getRating() >= 4).count();
        BigDecimal avg = all.stream()
                .map(r -> BigDecimal.valueOf(r.getRating() == null ? 0 : r.getRating()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        BigDecimal goodRate = BigDecimal.valueOf(good * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
        return new MerchantRatingSummaryDTO(merchantId, total, avg.doubleValue(), goodRate.doubleValue());
    }

    public List<Review> getLatestMerchantReviews(Long merchantId, Integer limit) {
        int realLimit = (limit == null || limit <= 0) ? 5 : Math.min(limit, 20);
        return reviewMapper.selectLatestByMerchant(merchantId, realLimit);
    }

    public String uploadReviewImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("图片文件不能为空");
        }
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
        if (!(contentType.contains("jpeg") || contentType.contains("jpg") || contentType.contains("png") || contentType.contains("webp"))) {
            throw new RuntimeException("仅支持 JPG/PNG/WebP 图片");
        }
        try {
            Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path dir = root.resolve("reviews");
            Files.createDirectories(dir);
            String ext = ".jpg";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            }
            String fileName = "review_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 100000) + ext;
            Path target = dir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String base = filePublicBaseUrl.endsWith("/") ? filePublicBaseUrl.substring(0, filePublicBaseUrl.length() - 1) : filePublicBaseUrl;
            return base + "/uploads/reviews/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }
}

