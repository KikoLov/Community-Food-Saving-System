package com.food.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.entity.Category;
import com.food.entity.Community;
import com.food.entity.User;
import com.food.mapper.CategoryMapper;
import com.food.mapper.CommunityMapper;
import com.food.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
public class DataInitController {

    private final UserMapper userMapper;
    private final CommunityMapper communityMapper;
    private final CategoryMapper categoryMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/force-init")
    public Map<String, Object> forceInit() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Delete existing data
            userMapper.delete(new LambdaQueryWrapper<>());
            communityMapper.delete(new LambdaQueryWrapper<>());
            categoryMapper.delete(new LambdaQueryWrapper<>());

            // 1. Create users
            String pwd = passwordEncoder.encode("admin");

            User admin = new User();
            admin.setUserName("admin");
            admin.setPassword(pwd);
            admin.setNickName("Administrator");
            admin.setUserType(3);
            admin.setStatus(0);
            admin.setDelFlag(0);
            userMapper.insert(admin);

            User consumer = new User();
            consumer.setUserName("consumer");
            consumer.setPassword(pwd);
            consumer.setNickName("Consumer User");
            consumer.setUserType(1);
            consumer.setStatus(0);
            consumer.setDelFlag(0);
            userMapper.insert(consumer);

            User merchant = new User();
            merchant.setUserName("merchant");
            merchant.setPassword(pwd);
            merchant.setNickName("Merchant User");
            merchant.setUserType(2);
            merchant.setStatus(0);
            merchant.setDelFlag(0);
            userMapper.insert(merchant);

            // 2. Create communities
            Community c1 = new Community();
            c1.setCommunityName("阳光花园");
            c1.setCommunityCode("SG001");
            c1.setProvince("北京市");
            c1.setCity("北京市");
            c1.setDistrict("朝阳区");
            c1.setAddress("朝阳区建国路88号");
            c1.setStatus(1);
            communityMapper.insert(c1);

            Community c2 = new Community();
            c2.setCommunityName("绿城小区");
            c2.setCommunityCode("GC002");
            c2.setProvince("上海市");
            c2.setCity("上海市");
            c2.setDistrict("浦东新区");
            c2.setAddress("浦东新区世纪大道100号");
            c2.setStatus(1);
            communityMapper.insert(c2);

            // 3. Create categories
            Category cat1 = new Category();
            cat1.setCategoryName("烘焙");
            cat1.setCategoryCode("BAKERY");
            cat1.setCarbonFactor(2.5);
            cat1.setSortOrder(1);
            cat1.setStatus(1);
            categoryMapper.insert(cat1);

            Category cat2 = new Category();
            cat2.setCategoryName("乳制品");
            cat2.setCategoryCode("DAIRY");
            cat2.setCarbonFactor(1.8);
            cat2.setSortOrder(2);
            cat2.setStatus(1);
            categoryMapper.insert(cat2);

            Category cat3 = new Category();
            cat3.setCategoryName("水果");
            cat3.setCategoryCode("FRUITS");
            cat3.setCarbonFactor(1.2);
            cat3.setSortOrder(3);
            cat3.setStatus(1);
            categoryMapper.insert(cat3);

            result.put("code", 200);
            result.put("msg", "Data initialized successfully");
            result.put("data", "Created: 3 users, 2 communities, 3 categories");

        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "Error: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}
