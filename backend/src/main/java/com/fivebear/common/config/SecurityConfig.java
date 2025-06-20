package com.fivebear.common.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fivebear.common.filter.JwtAuthenticationFilter;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（因为我们使用JWT）
            .csrf(csrf -> csrf.disable())
            
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理（无状态）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 配置请求授权
            .authorizeHttpRequests(authz -> authz
                // 允许认证相关的端点
                .requestMatchers("/auth/**").permitAll()
                // 允许系统监控API（无需认证）- 必须放在/api/**之前
                .requestMatchers("/api/system/status").permitAll()
                .requestMatchers("/api/system/**").permitAll()
                // 允许WebSocket端点及SockJS相关路径
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/ws/*/info").permitAll()
                .requestMatchers("/ws/*/websocket").permitAll()
                .requestMatchers("/ws/*/xhr").permitAll()
                .requestMatchers("/ws/*/xhr_send").permitAll()
                .requestMatchers("/ws/*/xhr_streaming").permitAll()
                .requestMatchers("/ws/*/jsonp").permitAll()
                .requestMatchers("/ws/*/jsonp_send").permitAll()
                .requestMatchers("/ws/*/eventsource").permitAll()
                .requestMatchers("/ws/*/htmlfile").permitAll()
                // 允许Swagger文档
                .requestMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                // 允许静态资源
                .requestMatchers("/static/**", "/public/**").permitAll()
                // 其他API需要认证
                .requestMatchers("/api/**").authenticated()
                // 其他请求需要认证
                .anyRequest().authenticated())
            
            // 禁用默认登录页面
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());
            
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // CORS配置支持开发环境端口
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000", 
            "http://127.0.0.1:3000",
            "http://localhost:3001", 
            "http://127.0.0.1:3001"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 