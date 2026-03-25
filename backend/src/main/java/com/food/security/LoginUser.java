package com.food.security;

import com.food.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 登录用户信息 - 实现Spring Security的UserDetails
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户类型: 1-居民 2-商户 3-管理员
     */
    private Integer userType;

    /**
     * 商户ID (如果是商户)
     */
    private Long merchantId;

    /**
     * 状态
     */
    private Integer status;

    public LoginUser(User user) {
        this.userId = user.getUserId();
        this.username = user.getUserName();
        this.password = user.getPassword();
        this.userType = user.getUserType();
        this.status = user.getStatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = getRole();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    /**
     * 获取用户角色
     */
    private String getRole() {
        if (userType == 1) {
            return "ROLE_CONSUMER";
        } else if (userType == 2) {
            return "ROLE_MERCHANT";
        } else if (userType == 3) {
            return "ROLE_ADMIN";
        }
        return "ROLE_USER";
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == 0;
    }
}
