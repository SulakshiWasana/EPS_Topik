package com.sejong.academy.eps_topik.configuration;

import com.sejong.academy.eps_topik.filters.JWTRequestFilter;
import com.sejong.academy.eps_topik.service.AuthUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor

@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {
    private final JWTRequestFilter jwtRequestFilter;
    private final AuthUserDetailsService userDetailsService;
    private final PasswordConfiguration passwordConfiguration;

    @Bean
    public DaoAuthenticationProvider databaseAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordConfiguration.getEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                "/user/register/**",
                                "/check-service",
                                "/app-user/detail",
                                "/app-user/license/update",
                                "/internal/save-email",
                                "/internal/reset-password",
                                "internal/points/balance",
                                "/internal/points/update",
                                "internal/mobile-check",
                                "internal/update/referral-info",
                                "/internal/fetch/referral-info",
                                "/internal/get/email",
                                "/internal/reject/images",
                                "/internal/upload/images",
                                "/internal/change/user-group",
                                "internal/fetch/image-history",
                                "/internal/fetch/reject-reason",
                                "/internal/get-challenge",
                                "/internal/change/status",
                                "/internal/change/status/pending-users",
                                "/internal/change/user-group/temp",
                                "/internal/fetch/user-details",
                                "/user/login/**",
                                "/user/device-verify",
                                "/user/refresh-token",
                                "/user/forgot/password",
                                "/user/forgot/password/verify",
                                "/user/mobile/change/password",
                                "/user/token-validation",
                                "/user/log-out",
                                "user/resend-otp",
                                "user/save-tnc",
                                "/internal/user/status-update")
                        .permitAll())
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/user/**").hasAuthority("APP_USER")
                                .anyRequest().authenticated())

                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class).build();
    }

}
