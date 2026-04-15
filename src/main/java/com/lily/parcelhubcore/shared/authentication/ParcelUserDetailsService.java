package com.lily.parcelhubcore.shared.authentication;

import static com.lily.parcelhubcore.parcel.domain.enums.ErrorCode.USER_NOT_EXIST;

import java.util.ArrayList;
import java.util.Collections;

import com.lily.parcelhubcore.shared.authentication.dto.LoginUser;
import com.lily.parcelhubcore.shared.exception.BusinessException;
import com.lily.parcelhubcore.user.infrastructure.persistence.repository.UserInfoRepository;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ParcelUserDetailsService implements UserDetailsService {

    @Resource
    private UserInfoRepository userInfoRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        System.out.println("loadUserByUsername 被调用，用户名：" + userName);
        // 查询用户信息
        var user = userInfoRepository.findByUserName(userName);
        if (user == null) {
            throw new BusinessException(USER_NOT_EXIST);
        }
        System.out.println("查到用户了");
        //  对应的权限信息，此处简化了授权，仅使用role来存储角色名进行控制
        var permissions = new ArrayList<>(Collections.singletonList(user.getRole()));
        return new LoginUser(user, permissions);
    }
}
