package com.community.food;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class TdpAlgorithmTest {
    public static void main(String[] args) {
        System.out.println("====== 开始 TDP 算法 48小时极速推演测试 ======");
        BigDecimal originalPrice = new BigDecimal("60.00");
        BigDecimal bottomPrice = new BigDecimal("15.00");
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 20, 8, 0);
        LocalDateTime expireTime = LocalDateTime.of(2026, 4, 22, 8, 0);
        for (int h = 0; h <= 48; h += 6) {
            LocalDateTime mockNow = startTime.plusHours(h);
            BigDecimal currentPrice = simulateTdpAlgorithm(originalPrice, bottomPrice, h, 48);

            System.out.printf("流逝时间: %2d 小时 | 模拟当前时间: %s | 系统实时报价: %s 元%n",
                    h, mockNow.toString().replace("T", " "), currentPrice);
        }

        System.out.println("====== 推演测试结束，价格成功拦截于底价 ======");
    }

    // 注意：因为要在 main 里调用，这个方法加了 static 修饰符
    private static BigDecimal simulateTdpAlgorithm(BigDecimal orig, BigDecimal bottom, int currentH, int totalH) {
        if (currentH >= totalH) return bottom;
        double progress = (double) currentH / totalH;
        double price = bottom.doubleValue() +
                (orig.doubleValue() - bottom.doubleValue()) * Math.exp(-1.5 * progress);
        return new BigDecimal(price).setScale(2, RoundingMode.HALF_UP);
    }
}