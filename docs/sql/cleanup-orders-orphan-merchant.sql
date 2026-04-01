-- 清理「无有效商家」的订单（居民端显示「未知商家」、且多为历史演示脏数据）
-- 使用逻辑删除，与 MyBatis-Plus @TableLogic 一致。
-- 执行前请备份数据库；可按需去掉 AND 条件缩小范围。
--
-- 说明：若订单的 merchant_id 在 biz_merchant 中不存在，或对应商户已被逻辑删除，
--       则无法解析商家名称；此类订单可安全标记为 deleted=1，不再出现在列表中。

UPDATE biz_order o
LEFT JOIN biz_merchant m
  ON m.merchant_id = o.merchant_id
  AND (m.deleted IS NULL OR m.deleted = 0)
SET o.deleted = 1,
    o.update_time = NOW()
WHERE (o.deleted IS NULL OR o.deleted = 0)
  AND m.merchant_id IS NULL;
