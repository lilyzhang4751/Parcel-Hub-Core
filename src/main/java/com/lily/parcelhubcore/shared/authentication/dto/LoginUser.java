package com.lily.parcelhubcore.shared.authentication.dto;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.annotation.JSONField;
import com.lily.parcelhubcore.user.infrastructure.persistence.entity.UserInfoDO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private UserInfoDO user;

    private List<String> permissions;

    /**
     * 不序列化
     */
    @JSONField(serialize = false)
    private List<GrantedAuthority> authorities;

    public LoginUser(UserInfoDO user, List<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (CollectionUtils.isEmpty(authorities)) {
            return permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }
}
