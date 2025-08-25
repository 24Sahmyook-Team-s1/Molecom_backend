// src/main/java/com/pacs/molecoms/config/SecurityConfig.java
package com.pacs.molecoms.config;

import com.pacs.molecoms.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
    };
    private static final String[] PUBLIC_WHITELIST = {
            "/actuator/health", "/actuator/info", "/error"
            // 필요 시 로그인/회원가입 API 추가: "/api/auth/**"
    };

    @Value("${app.security.cors.allowed-origins:http://localhost:5173}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
                                           AuthenticationEntryPoint entryPoint,
                                           AccessDeniedHandler accessDeniedHandler) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        .requestMatchers(PUBLIC_WHITELIST).permitAll()
                        // 내부 인제스트 훅 예시: 서비스 계정 전용
                        .requestMatchers(HttpMethod.POST, "/internal/ingest/**").hasRole("SERVICE")
                        // OHIF 정적 파일/프론트 리버스 프록시 경로가 있으면 허용
                        //.requestMatchers("/viewer/**").permitAll()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(allowedOrigins);
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (req, res, e) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("""
            {"success":false,"error":{"code":401,"message":"Unauthorized"}}
            """);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (req, res, e) -> {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("""
            {"success":false,"error":{"code":403,"message":"Access Denied"}}
            """);
        };
    }
}
