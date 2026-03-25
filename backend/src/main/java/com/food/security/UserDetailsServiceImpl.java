package com.food.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.food.entity.Merchant;
import com.food.entity.User;
import com.food.mapper.MerchantMapper;
import com.food.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户Details服务实现
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 构建登录用户对象
        LoginUser loginUser = new LoginUser(user);

        // 如果是商户，查询商户ID
        if (user.getUserType() != null && user.getUserType() == 2) {
            LambdaQueryWrapper<Merchant> merchantWrapper = new LambdaQueryWrapper<>();
            merchantWrapper.eq(Merchant::getUserId, user.getUserId());
            Merchant merchant = merchantMapper.selectOne(merchantWrapper);
            if (merchant != null) {
                loginUser.setMerchantId(merchant.getMerchantId());
            }
        }

        return loginUser;
    }
}
