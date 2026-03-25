package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.entity.OperationLog;
import com.food.mapper.OperationLogMapper;
import com.food.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作审计日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public List<OperationLog> listLogs() {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getCreateTime).orderByDesc(OperationLog::getId);
        return operationLogMapper.selectList(wrapper);
    }

    public void logOperation(LoginUser loginUser,
                             String actionType,
                             String targetType,
                             Long targetId,
                             String targetName,
                             String detail) {
        try {
            OperationLog logEntity = new OperationLog();
            logEntity.setActorUserId(loginUser.getUserId());
            logEntity.setActorUsername(loginUser.getUsername());
            logEntity.setActorRole(resolveRole(loginUser.getUserType()));
            logEntity.setActionType(actionType);
            logEntity.setTargetType(targetType);
            logEntity.setTargetId(targetId);
            logEntity.setTargetName(targetName);
            logEntity.setDetail(detail);
            logEntity.setCreateTime(LocalDateTime.now());
            operationLogMapper.insert(logEntity);
        } catch (Exception ex) {
            // 审计日志失败不应影响主业务
            log.warn("记录操作日志失败: actionType={}, targetType={}, targetId={}",
                    actionType, targetType, targetId, ex);
        }
    }

    private String resolveRole(Integer userType) {
        if (userType == null) {
            return "UNKNOWN";
        }
        if (userType == 3) {
            return "ADMIN";
        }
        if (userType == 2) {
            return "MERCHANT";
        }
        if (userType == 1) {
            return "CONSUMER";
        }
        return "UNKNOWN";
    }
}
