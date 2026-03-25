-- 增量脚本：新增操作审计日志表
-- 适用场景：已有 food_saving 数据库，不想重跑 init.sql

USE food_saving;

CREATE TABLE IF NOT EXISTS biz_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    actor_user_id BIGINT NOT NULL COMMENT '操作者用户ID',
    actor_username VARCHAR(64) NOT NULL COMMENT '操作者用户名',
    actor_role VARCHAR(32) NOT NULL COMMENT '操作者角色',
    action_type VARCHAR(64) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(64) NOT NULL COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    target_name VARCHAR(255) COMMENT '目标名称',
    detail VARCHAR(500) COMMENT '操作详情',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_actor_user_id (actor_user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_target_type (target_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志';

