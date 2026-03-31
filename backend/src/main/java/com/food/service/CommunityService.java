package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.entity.Community;
import com.food.mapper.CommunityMapper;
import com.food.util.DemoTextNormalizeUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 社区服务
 */
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityMapper communityMapper;
    private final JdbcTemplate jdbcTemplate;

    private static final long GREEN_COMMUNITY_ID = 3L;
    private static final long SUN_COMMUNITY_ID = 4L;
    private static final String GREEN_CODE = "GC_DEMO_001";
    private static final String SUN_CODE = "YG_DEMO_001";

    @PostConstruct
    public void ensureStableCommunityIds() {
        ensureOneStableCommunity(
                GREEN_COMMUNITY_ID,
                GREEN_CODE,
                "绿城小区",
                "上海市",
                "上海市",
                "浦东新区",
                "浦东新区世纪大道100号"
        );
        ensureOneStableCommunity(
                SUN_COMMUNITY_ID,
                SUN_CODE,
                "阳光花园",
                "北京市",
                "北京市",
                "朝阳区",
                "朝阳区建国路88号"
        );
    }

    private void ensureOneStableCommunity(Long fixedId,
                                          String code,
                                          String name,
                                          String province,
                                          String city,
                                          String district,
                                          String address) {
        Long byCodeId = Optional.ofNullable(jdbcTemplate.query(
                        "SELECT community_id FROM sys_community WHERE community_code = ? AND deleted = 0 LIMIT 1",
                        (rs, rowNum) -> rs.getLong("community_id"),
                        code
                ))
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .orElse(null);
        Long byFixedIdCodeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_community WHERE community_id = ? AND deleted = 0",
                Long.class,
                fixedId
        );

        if (byCodeId != null && !byCodeId.equals(fixedId)) {
            // 迁移历史绑定，避免商家“丢社区”
            jdbcTemplate.update("UPDATE biz_merchant SET community_id = ? WHERE community_id = ? AND deleted = 0", fixedId, byCodeId);
            jdbcTemplate.update("UPDATE biz_user_profile SET community_id = ? WHERE community_id = ? AND deleted = 0", fixedId, byCodeId);
            jdbcTemplate.update("UPDATE biz_merchant_community SET community_id = ? WHERE community_id = ? AND deleted = 0", fixedId, byCodeId);
            jdbcTemplate.update("DELETE FROM sys_community WHERE community_id = ?", byCodeId);
        }

        if (byFixedIdCodeCount == null || byFixedIdCodeCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO sys_community (community_id, community_name, community_code, province, city, district, address, status, deleted, create_time, update_time) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, 1, 0, NOW(), NOW())",
                    fixedId, name, code, province, city, district, address
            );
        } else {
            jdbcTemplate.update(
                    "UPDATE sys_community SET community_name=?, community_code=?, province=?, city=?, district=?, address=?, status=1, deleted=0, update_time=NOW() WHERE community_id=?",
                    name, code, province, city, district, address, fixedId
            );
        }
    }

    /**
     * 获取所有启用的社区
     */
    public List<Community> getAllCommunities() {
        LambdaQueryWrapper<Community> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Community::getStatus, 1).orderByAsc(Community::getCommunityName);
        return communityMapper.selectList(wrapper).stream()
                .map(DemoTextNormalizeUtil::normalizeCommunity)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取社区
     */
    public Community getCommunityById(Long communityId) {
        return DemoTextNormalizeUtil.normalizeCommunity(communityMapper.selectById(communityId));
    }

    /**
     * 添加社区
     */
    public Community addCommunity(Community community) {
        communityMapper.insert(community);
        return community;
    }

    /**
     * 更新社区
     */
    public Community updateCommunity(Community community) {
        communityMapper.updateById(community);
        return community;
    }

    /**
     * 删除社区
     */
    public void deleteCommunity(Long communityId) {
        Long merchantCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM biz_merchant WHERE community_id = ? AND deleted = 0",
                Long.class,
                communityId
        );
        if (merchantCount != null && merchantCount > 0) {
            throw new RuntimeException("该社区下仍有商家绑定，无法删除");
        }
        communityMapper.deleteById(communityId);
    }

    /**
     * 获取所有社区(管理端)
     */
    public List<Community> getAllCommunitiesForAdmin() {
        return communityMapper.selectList(null).stream()
                .map(DemoTextNormalizeUtil::normalizeCommunity)
                .collect(Collectors.toList());
    }
}
