-- 添加缺失的字段到 biz_merchant 表
ALTER TABLE biz_merchant ADD COLUMN IF NOT EXISTS opening_hours VARCHAR(100) COMMENT '营业时间，如: 08:00-22:00';
ALTER TABLE biz_merchant ADD COLUMN IF NOT EXISTS description TEXT COMMENT '店铺描述';
ALTER TABLE biz_merchant ADD COLUMN IF NOT EXISTS community_id BIGINT COMMENT '社区ID (主要服务社区)';
ALTER TABLE biz_merchant ADD COLUMN IF NOT EXISTS create_by VARCHAR(50) COMMENT '创建者';
ALTER TABLE biz_merchant ADD COLUMN IF NOT EXISTS update_by VARCHAR(50) COMMENT '更新者';
ALTER TABLE biz_merchant ADD COLUMN IF NOT EXISTS deleted INT DEFAULT 0 COMMENT '删除标志 0-正常 1-已删除';
