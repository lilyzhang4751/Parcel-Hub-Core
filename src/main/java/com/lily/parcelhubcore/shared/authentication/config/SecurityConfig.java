package com.lily.parcelhubcore.shared.authentication.config;

import com.lily.parcelhubcore.shared.filter.JwtAuthenticationTokenFilter;
import com.lily.parcelhubcore.shared.handler.CustomAccessDeniedHandler;
import com.lily.parcelhubcore.shared.handler.CustomAuthenticationEntryPoint;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Resource
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Resource
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Resource
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Resource
    private CorsConfigurationSource corsConfigurationSource;

    // 配置核心过滤链
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                // 1. CORS 配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // 2. 关闭 CSRF（测试阶段，Postman/Hoppscotch 需要）
                .csrf(AbstractHttpConfigurer::disable)

                // 设置会话为无状态（JWT 场景）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 权限配置（关键修正）
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/hello").permitAll() // 允许所有人访问
                        .requestMatchers("/user/login").anonymous()  //  未登录状态才能访问
                        .anyRequest().authenticated()
                )

                .httpBasic(Customizer.withDefaults()); // 可选：基本认证

        // 将自定义的过滤器放到UsernamePasswordAuthenticationFilter前面
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // 自定义异常处理
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler));
        return http.build();
    }

    // 配置密码编码器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        // Spring自动从容器找 UserDetailsService、PasswordEncoder 组装
        return config.getAuthenticationManager();
    }
}
