package com.food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.entity.Community;
import com.food.mapper.CommunityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 社区服务
 */
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityMapper communityMapper;

    /**
     * 获取所有启用的社区
     */
    public List<Community> getAllCommunities() {
        LambdaQueryWrapper<Community> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Community::getStatus, 1).orderByAsc(Community::getCommunityName);
        return communityMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取社区
     */
    public Community getCommunityById(Long communityId) {
        return communityMapper.selectById(communityId);
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
        communityMapper.deleteById(communityId);
    }

    /**
     * 获取所有社区(管理端)
     */
    public List<Community> getAllCommunitiesForAdmin() {
        return communityMapper.selectList(null);
    }
}
