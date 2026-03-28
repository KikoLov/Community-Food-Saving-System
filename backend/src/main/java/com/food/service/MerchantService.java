package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.dto.MerchantAdminStatsDTO;
import com.food.entity.Merchant;
import com.food.entity.Order;
import com.food.entity.Product;
import com.food.entity.User;
import com.food.mapper.MerchantMapper;
import com.food.mapper.OrderMapper;
import com.food.mapper.ProductMapper;
import com.food.mapper.UserMapper;
import com.food.util.DemoTextNormalizeUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户服务
 */
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantMapper merchantMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Value("${file.public.base-url:http://localhost:8080}")
    private String filePublicBaseUrl;

    /**
     * 根据用户ID获取商户
     */
    public Merchant getMerchantByUserId(Long userId) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getUserId, userId);
        return merchantMapper.selectOne(wrapper);
    }

    /**
     * 更新商户信息
     */
    public Merchant updateMerchant(Long userId, Merchant merchant) {
        Merchant existMerchant = getMerchantByUserId(userId);
        if (existMerchant == null) {
            throw new RuntimeException("商户不存在");
        }

        if (merchant.getMerchantName() != null) {
            existMerchant.setMerchantName(merchant.getMerchantName());
        }
        if (merchant.getContactPhone() != null) {
            existMerchant.setContactPhone(merchant.getContactPhone());
        }
        if (merchant.getAddress() != null) {
            existMerchant.setAddress(merchant.getAddress());
        }
        if (merchant.getOpeningHours() != null) {
            existMerchant.setOpeningHours(merchant.getOpeningHours());
        }
        if (merchant.getDescription() != null) {
            existMerchant.setDescription(merchant.getDescription());
        }
        if (merchant.getCommunityId() != null) {
            existMerchant.setCommunityId(merchant.getCommunityId());
        }
        existMerchant.setUpdateTime(LocalDateTime.now());
        merchantMapper.updateById(existMerchant);

        return existMerchant;
    }

    /**
     * 上传营业执照
     */
    public String uploadLicense(Long userId, MultipartFile file) {
        Merchant merchant = getMerchantByUserId(userId);
        if (merchant == null) {
            throw new RuntimeException("商户不存在");
        }

        // 简单处理：实际项目中应该保存文件到磁盘或OSS
        String fileName = "license_" + userId + "_" + System.currentTimeMillis() + ".jpg";
        String fileUrl = "/uploads/" + fileName;

        merchant.setBusinessLicense(fileUrl);
        merchant.setUpdateTime(LocalDateTime.now());
        merchantMapper.updateById(merchant);

        return fileUrl;
    }

    /**
     * 上传商品图片（保存到本地目录，返回可访问URL）
     */
    public String uploadProductImage(Long userId, MultipartFile file) {
        Merchant merchant = getMerchantByUserId(userId);
        if (merchant == null) {
            throw new RuntimeException("商户不存在");
        }
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("图片文件不能为空");
        }
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
        if (!(contentType.contains("jpeg") || contentType.contains("jpg") || contentType.contains("png") || contentType.contains("webp"))) {
            throw new RuntimeException("仅支持 JPG/PNG/WebP 图片");
        }

        try {
            Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path dir = root.resolve("products");
            Files.createDirectories(dir);

            String ext = resolveExt(file.getOriginalFilename(), contentType);
            String fileName = "product_" + userId + "_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 100000) + ext;
            Path target = dir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String base = filePublicBaseUrl.endsWith("/") ? filePublicBaseUrl.substring(0, filePublicBaseUrl.length() - 1) : filePublicBaseUrl;
            return base + "/uploads/products/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除已上传的商品图片（仅允许删除 /uploads/products 下文件）
     */
    public boolean deleteUploadedProductImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return false;
        }
        String marker = "/uploads/products/";
        int idx = imageUrl.indexOf(marker);
        if (idx < 0) {
            return false;
        }
        String fileName = imageUrl.substring(idx + marker.length());
        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            return false;
        }
        try {
            Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
            Path filePath = root.resolve("products").resolve(fileName).normalize();
            Path allowedDir = root.resolve("products").normalize();
            if (!filePath.startsWith(allowedDir)) {
                return false;
            }
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    private String resolveExt(String originalName, String contentType) {
        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            if (".jpg".equals(ext) || ".jpeg".equals(ext) || ".png".equals(ext) || ".webp".equals(ext)) {
                return ext;
            }
        }
        if (contentType.contains("png")) return ".png";
        if (contentType.contains("webp")) return ".webp";
        return ".jpg";
    }

    /**
     * 提交资质审核
     */
    public void submitLicenseAudit(Long userId) {
        Merchant merchant = getMerchantByUserId(userId);
        if (merchant == null) {
            throw new RuntimeException("商户不存在");
        }

        if (merchant.getBusinessLicense() == null || merchant.getBusinessLicense().isEmpty()) {
            throw new RuntimeException("请先上传营业执照");
        }

        merchant.setLicenseStatus(0); // 待审核
        merchant.setUpdateTime(LocalDateTime.now());
        merchantMapper.updateById(merchant);
    }

    /**
     * 审核商户资质
     */
    public void auditMerchant(Long merchantId, Integer status) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new RuntimeException("商户不存在");
        }
        merchant.setLicenseStatus(status);
        merchant.setUpdateTime(LocalDateTime.now());
        merchantMapper.updateById(merchant);
    }

    /**
     * 批量导入商品 (Excel)
     */
    @Transactional
    public Map<String, Object> importProducts(Long merchantId, MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            // 跳过标题行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // 读取Excel数据
                    String productName = getCellValue(row.getCell(0));
                    BigDecimal originalPrice = new BigDecimal(getCellValue(row.getCell(1)));
                    BigDecimal discountPrice = new BigDecimal(getCellValue(row.getCell(2)));
                    Integer stock = Integer.parseInt(getCellValue(row.getCell(3)));
                    String expireDateStr = getCellValue(row.getCell(4));
                    String expireTimeStr = getCellValue(row.getCell(5));

                    // 解析过期日期和时间
                    LocalDate expireDate = LocalDate.parse(expireDateStr);
                    LocalTime expireTime = LocalTime.parse(expireTimeStr);
                    LocalDateTime expireDateTime = LocalDateTime.of(expireDate, expireTime);

                    // 创建商品
                    Product product = new Product();
                    product.setMerchantId(merchantId);
                    product.setProductName(productName);
                    product.setOriginalPrice(originalPrice);
                    product.setDiscountPrice(discountPrice);
                    product.setStock(stock);
                    product.setUnit("件");
                    product.setExpireDate(expireDate);
                    product.setExpireDatetime(expireDateTime);
                    product.setWarningHours(24);
                    product.setStatus(1);
                    product.setCreateTime(LocalDateTime.now());
                    productMapper.insert(product);

                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行导入失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("解析Excel文件失败: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("errors", errors);
        return result;
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    /**
     * 获取所有商户
     */
    public List<Merchant> getAllMerchants() {
        return merchantMapper.selectList(null);
    }

    /**
     * 根据ID获取商户
     */
    public Merchant getMerchantById(Long merchantId) {
        return merchantMapper.selectById(merchantId);
    }

    /**
     * 管理端：商户经营统计 + 基本信息
     */
    public MerchantAdminStatsDTO getMerchantAdminStats(Long merchantId) {
        Merchant m = merchantMapper.selectById(merchantId);
        if (m == null) {
            throw new RuntimeException("商户不存在");
        }
        DemoTextNormalizeUtil.normalizeMerchant(m);
        BigDecimal todaySales = orderMapper.sumTodaySalesByMerchant(merchantId);
        if (todaySales == null) {
            todaySales = BigDecimal.ZERO;
        }
        Long todayCnt = orderMapper.countTodayOrdersByMerchant(merchantId);
        BigDecimal monthSales = orderMapper.sumMonthSalesByMerchant(merchantId);
        if (monthSales == null) {
            monthSales = BigDecimal.ZERO;
        }
        Long monthCnt = orderMapper.countMonthOrdersByMerchant(merchantId);
        Long pending = orderMapper.countPendingByMerchant(merchantId);
        Long verified = orderMapper.countVerifiedByMerchant(merchantId);
        long onSale = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .eq(Product::getStatus, 1));
        return new MerchantAdminStatsDTO(
                m,
                todaySales,
                todayCnt != null ? todayCnt : 0L,
                monthSales,
                monthCnt != null ? monthCnt : 0L,
                pending != null ? pending : 0L,
                verified != null ? verified : 0L,
                onSale
        );
    }

    /**
     * 管理端：删除商户。
     * force=false：仅当无任何订单时删除（并删除该商户下商品）。
     * force=true：级联逻辑删除该商户下订单、商品后再删除商户。
     * 演示账号 merchant1/merchant2：仅当商户名称非乱码时禁止删除（便于清理同名用户下的重复乱码行）。
     */
    @Transactional
    public void deleteMerchantForAdmin(Long merchantId, boolean force) {
        Merchant m = merchantMapper.selectById(merchantId);
        if (m == null) {
            throw new RuntimeException("商户不存在");
        }
        User u = m.getUserId() != null ? userMapper.selectById(m.getUserId()) : null;
        if (u != null && ("merchant1".equals(u.getUserName()) || "merchant2".equals(u.getUserName()))) {
            if (!isGarbledMerchantName(m.getMerchantName())) {
                throw new RuntimeException("演示账号 merchant1 / merchant2 对应商户不可删除");
            }
        }
        if (force) {
            orderMapper.delete(new LambdaQueryWrapper<Order>().eq(Order::getMerchantId, merchantId));
            productMapper.delete(new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchantId));
            merchantMapper.deleteById(merchantId);
            return;
        }
        long orderCnt = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId));
        if (orderCnt > 0) {
            throw new RuntimeException("该商户已有订单记录，无法删除。请勾选强制删除或改用「删除（含订单）」");
        }
        productMapper.delete(new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchantId));
        merchantMapper.deleteById(merchantId);
    }

    /**
     * 清理名称疑似乱码的商户（级联删除其订单与商品；merchant1/merchant2 下仅删乱码行，保留正常名称行）
     */
    @Transactional
    public int pruneGarbledMerchantsWithoutOrders() {
        List<Merchant> list = merchantMapper.selectList(null);
        int removed = 0;
        for (Merchant m : list) {
            if (m == null || m.getUserId() == null) {
                continue;
            }
            if (!isGarbledMerchantName(m.getMerchantName())) {
                continue;
            }
            deleteMerchantForAdmin(m.getMerchantId(), true);
            removed++;
        }
        return removed;
    }

    private static boolean isGarbledMerchantName(String name) {
        if (name == null || name.isBlank()) {
            return true;
        }
        if (name.contains("�") || name.contains("\uFFFD")) {
            return true;
        }
        if (name.matches(".*[ÃÂÐ].*")) {
            return true;
        }
        long qMarks = name.chars().filter(c -> c == '?' || c == '？').count();
        if (qMarks >= 2) {
            return true;
        }
        return name.matches("^\\?+$") || name.matches("^[？]+$");
    }
}
