package com.food.util;

import com.food.entity.Category;
import com.food.entity.Community;
import com.food.entity.Merchant;
import com.food.entity.Order;
import com.food.entity.Product;

import java.util.Map;

/**
 * 演示数据文案统一转换（英文 -> 中文）
 */
public final class DemoTextNormalizeUtil {

    private DemoTextNormalizeUtil() {
    }

    private static final Map<String, String> PRODUCT_NAME_MAP = Map.of(
            "Greencity Milk", "绿城新鲜牛奶",
            "Greencity Whole Wheat Bread", "绿城全麦面包",
            "Greencity Apple Slices", "绿城苹果果切",
            "SunGarden Yogurt", "阳光原味酸奶",
            "SunGarden Cake Roll", "阳光蛋糕卷",
            "SunGarden Orange Juice", "阳光鲜橙汁"
    );

    private static final Map<String, String> MERCHANT_NAME_MAP = Map.of(
            "Greencity Demo Store", "绿城小区社区便利店",
            "SunGarden Demo Store", "阳光花园社区便利店"
    );

    private static final Map<String, String> CATEGORY_NAME_MAP = Map.of(
            "Bakery", "烘焙",
            "Dairy", "乳品",
            "Fruits", "水果",
            "Vegetables", "蔬菜",
            "Meat", "肉类",
            "Drink", "饮品",
            "Fresh", "生鲜",
            "Food", "食品"
    );

    public static String normalizeProductName(String name) {
        if (name == null) return null;
        return PRODUCT_NAME_MAP.getOrDefault(name, name);
    }

    public static String normalizeMerchantName(String name) {
        if (name == null) return null;
        return MERCHANT_NAME_MAP.getOrDefault(name, name);
    }

    public static Merchant normalizeMerchant(Merchant m) {
        if (m == null) {
            return null;
        }
        m.setMerchantName(normalizeMerchantName(m.getMerchantName()));
        return m;
    }

    public static String normalizeCategoryName(String name) {
        if (name == null) return null;
        return CATEGORY_NAME_MAP.getOrDefault(name, name);
    }

    public static Product normalizeProduct(Product p) {
        if (p == null) return null;
        p.setProductName(normalizeProductName(p.getProductName()));
        p.setMerchantName(normalizeMerchantName(p.getMerchantName()));
        p.setCategoryName(normalizeCategoryName(p.getCategoryName()));
        return p;
    }

    public static Category normalizeCategory(Category c) {
        if (c == null) return null;
        c.setCategoryName(normalizeCategoryName(c.getCategoryName()));
        return c;
    }

    public static Order normalizeOrder(Order o) {
        if (o == null) return null;
        o.setProductName(normalizeProductName(o.getProductName()));
        o.setMerchantName(normalizeMerchantName(o.getMerchantName()));
        return o;
    }

    /**
     * 演示社区省市区地址在部分环境下会写入乱码或问号，按编码/ID 回退为正确中文。
     */
    public static Community normalizeCommunity(Community c) {
        if (c == null) {
            return null;
        }
        String code = c.getCommunityCode();
        if (code != null) {
            String upper = code.trim();
            if ("SG001".equalsIgnoreCase(upper)) {
                applySunGardenCommunity(c);
                return c;
            }
            if ("GC002".equalsIgnoreCase(upper)) {
                applyGreencityCommunity(c);
                return c;
            }
        }
        Long id = c.getCommunityId();
        if (id != null) {
            switch (id.intValue()) {
                case 1 -> {
                    applySunGardenCommunity(c);
                    return c;
                }
                case 2 -> {
                    applyGreencityCommunity(c);
                    return c;
                }
                case 3 -> {
                    applyGreencityCommunity(c);
                    return c;
                }
                case 4 -> {
                    applySunGardenCommunity(c);
                    return c;
                }
                default -> {
                    // fall through
                }
            }
        }
        if (needsCommunityRegionFallback(c)) {
            String name = c.getCommunityName();
            if (name != null) {
                if (name.contains("阳光") || name.contains("Sun") || name.contains("花园")) {
                    applySunGardenCommunity(c);
                    return c;
                }
                if (name.contains("绿城") || name.contains("Green")) {
                    applyGreencityCommunity(c);
                    return c;
                }
            }
        }
        return c;
    }

    private static boolean needsCommunityRegionFallback(Community c) {
        return isGarbledOrMeaningless(c.getProvince())
                || isGarbledOrMeaningless(c.getCity())
                || isGarbledOrMeaningless(c.getDistrict())
                || isGarbledOrMeaningless(c.getAddress());
    }

    private static boolean isGarbledOrMeaningless(String s) {
        if (s == null || s.isBlank()) {
            return true;
        }
        String t = s.trim();
        if (t.contains("?") || t.contains("？")) {
            return true;
        }
        if (t.contains("�")) {
            return true;
        }
        if (t.matches("^\\d+$")) {
            return true;
        }
        return false;
    }

    private static void applySunGardenCommunity(Community c) {
        c.setCommunityName("阳光花园");
        c.setProvince("北京市");
        c.setCity("北京市");
        c.setDistrict("朝阳区");
        c.setAddress("朝阳区建国路88号");
    }

    private static void applyGreencityCommunity(Community c) {
        c.setCommunityName("绿城小区");
        c.setProvince("上海市");
        c.setCity("上海市");
        c.setDistrict("浦东新区");
        c.setAddress("浦东新区世纪大道100号");
    }
}

