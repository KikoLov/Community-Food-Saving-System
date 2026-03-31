-- 将常见演示商品过期时间顺延（居民端列表要求 expire_datetime > NOW()）
-- 可单独执行，也可由 setup-demo.ps1 在种子之后自动执行
USE food_saving;

UPDATE biz_product
SET expire_date     = DATE_ADD(CURDATE(), INTERVAL 730 DAY),
    expire_datetime = DATE_ADD(NOW(), INTERVAL 730 DAY),
    update_time     = NOW()
WHERE deleted = 0
  AND status = 1
  AND product_name IN (
    '演示面包',
    '演示果汁',
    '新鲜面包片',
    '有机苹果',
    '纯牛奶',
    '橙汁饮料'
  );
