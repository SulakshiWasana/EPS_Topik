package com.sejong.academy.eps_topik.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sejong.academy.eps_topik.entities.AppUser;
import com.sejong.academy.eps_topik.enums.ResponseStatus;
import com.sejong.academy.eps_topik.enums.Status;
import com.sejong.academy.eps_topik.exception.DeviceIdMisMatchException;
import com.sejong.academy.eps_topik.exception.UserDisabledException;
import com.sejong.academy.eps_topik.model.response.DefaultResponse;
import com.sejong.academy.eps_topik.repository.AppUserRepository;
import com.sejong.academy.eps_topik.repository.TokenBlackListRepository;
import com.sejong.academy.eps_topik.service.AuthUserDetailsService;
import com.sejong.academy.eps_topik.util.LogMessageUtil;
import com.sejong.academy.eps_topik.util.ResponseCodeUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(3)
public class JWTRequestFilter extends OncePerRequestFilter {

    private static final String MDC_UID_KEY = "uid";

    private final AuthUserDetailsService userService;
    private final AppUserRepository userRepository;
    private final TokenBlackListRepository tokenBlackListRepository;

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            if (skipRefreshToken(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authentication = getAuthentication(authHeader, request);

            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (TokenExpiredException e) {

            sendError(response,
                    ResponseCodeUtil.JWT_TOKEN_EXPIRED_ERROR_CODE,
                    LogMessageUtil.INVALID_CREDENTIAL,
                    HttpServletResponse.SC_UNAUTHORIZED);

        } catch (DeviceIdMisMatchException e) {

            sendError(response,
                    ResponseCodeUtil.DEVICE_ID_MISMATCH_ERROR_CODE,
                    LogMessageUtil.USER_ALREADY_LOGGED_IN_ANOTHER_DEVICE,
                    402);

        } catch (UserDisabledException e) {

            sendError(response,
                    ResponseCodeUtil.JWT_TOKEN_VALIDATE_ERROR_CODE,
                    e.getMessage(),
                    HttpServletResponse.SC_UNAUTHORIZED);

        } catch (JWTVerificationException e) {

            sendError(response,
                    ResponseCodeUtil.JWT_TOKEN_VALIDATE_ERROR_CODE,
                    LogMessageUtil.INVALID_CREDENTIAL,
                    HttpServletResponse.SC_UNAUTHORIZED);

        } catch (Exception e) {

            log.error("JWT filter error", e);
            sendError(response,
                    ResponseCodeUtil.JWT_TOKEN_VALIDATE_ERROR_CODE,
                    LogMessageUtil.INVALID_CREDENTIAL,
                    HttpServletResponse.SC_UNAUTHORIZED);

        } finally {
            MDC.remove(MDC_UID_KEY);
        }
    }

    // ===================== AUTH =====================

    private UsernamePasswordAuthenticationToken getAuthentication(
            String authHeader,
            HttpServletRequest request) {

        try {
            String token = authHeader.substring(7); // remove "Bearer "

            DecodedJWT decodedJWT = JWT
                    .require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(token);

            String username = decodedJWT.getSubject();
            if (username == null) return null;

            UserDetails userDetails = userService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            validateUser(token, username, request);

            return authentication;

        } catch (JWTVerificationException e) {
            return null;
        }
    }

    // ===================== VALIDATION =====================

    private void validateUser(String token, String username, HttpServletRequest request) {

        AppUser appUser = userRepository.findOneByUsername(username);
        if (appUser == null) return;

        String clientType = request.getHeader("X-Client-Type");
        String deviceId = request.getHeader("Device-Id");

        boolean isMobileClient = clientType != null && clientType.equalsIgnoreCase("MOBILE");

        // 🔐 Device-ID ONLY for MOBILE
        if (isMobileClient) {

            if (deviceId == null) {
                throw new JWTVerificationException(LogMessageUtil.DEVICE_ID_NOT_FOUND);
            }

            if (!deviceId.equals(appUser.getDeviceId())) {
                throw new DeviceIdMisMatchException(LogMessageUtil.DEVICE_ID_MISMATCH);
            }
        }

        // 🚫 User status checks (web + mobile)
        if (Status.DISABLED.name().equals(appUser.getStatus())) {
            throw new UserDisabledException(LogMessageUtil.USER_DISABLED);
        }

        if (Status.LOCKED.name().equals(appUser.getStatus())) {
            throw new UserDisabledException(LogMessageUtil.USER_LOCKED);
        }

        // ⛔ Token blacklist
        if (tokenBlackListRepository.existsByToken(token)) {
            throw new TokenExpiredException("Token blacklisted", Instant.now());
        }

        request.setAttribute("user", appUser);
        MDC.put(MDC_UID_KEY, appUser.getUsername());
    }

    // ===================== SKIP PATHS =====================

    private boolean skipRefreshToken(HttpServletRequest request) {
        String[] paths = {
                "/user/refresh-token"
        };
        for (String path : paths) {
            Matcher matcher =
                    Pattern.compile(path).matcher(request.getServletPath());
            if (matcher.find()) return true;
        }
        return false;
    }

    // ===================== ERROR RESPONSE =====================

    private void sendError(
            HttpServletResponse response,
            String code,
            String message,
            int status
    ) throws IOException {

        DefaultResponse defaultResponse = DefaultResponse.builder()
                .code(code)
                .title(ResponseStatus.FAILED.name())
                .message(message)
                .build();

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        writer.print(new ObjectMapper().writeValueAsString(defaultResponse));
        writer.flush();
    }
}
