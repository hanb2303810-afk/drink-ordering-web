package com.group04.config;

import com.group04.auth.AuthInterceptor;
import com.group04.auth.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS cho demo (client dev chạy 5173) + đăng ký interceptor.
 * Route public được khai báo trong AuthInterceptor (menu GET + stream),
 * ở đây chỉ exclude duy nhất khu vực đăng nhập.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String jwtSecret;
    private final long jwtExpirationMs;

    public WebConfig(@Value("${app.jwt.secret}") String jwtSecret,
                     @Value("${app.jwt.expiration-ms:28800000}") long jwtExpirationMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(jwtSecret, jwtExpirationMs);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(jwtUtil()))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/stream/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // Token đi qua header Authorization, không dùng cookie -> không cần credentials.
                .allowCredentials(false);
    }
}
