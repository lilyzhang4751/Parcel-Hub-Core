package com.lily.parcelhubcore.shared.util;

import com.lily.parcelhubcore.shared.authentication.dto.LoginUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUserUtil {

    public static LoginUser getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("No authenticated user in SecurityContext");
        }

        var principal = authentication.getPrincipal();
        if (!(principal instanceof LoginUser loginUser)) {
            throw new IllegalStateException("Current principal is not LoginUser");
        }
        return loginUser;
    }

    public static String getUserCode() {
        return getCurrentUser().getUserCode();
    }

    public static String getUsername() {
        return getCurrentUser().getUsername();
    }

    public static String getStationCode() {
        return getCurrentUser().getStationCode();
    }
}
