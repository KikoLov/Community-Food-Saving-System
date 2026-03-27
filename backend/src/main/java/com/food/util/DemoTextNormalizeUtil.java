package com.food.util;

import com.food.entity.Category;
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
}

