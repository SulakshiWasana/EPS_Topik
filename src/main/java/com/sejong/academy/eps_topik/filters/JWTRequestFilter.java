package com.sejong.academy.eps_topik.filters;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
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
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.security.config.Elements.JWT;

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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {
        try {

            boolean refreshToken = skipRefreshToken(request);

            if (refreshToken) {
                filterChain.doFilter(request, response);
                return;
            }
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authentication = getAuthentication(header, request);
            if (Objects.isNull(authentication)) {
                log.info("Not authenticated. Public request.");
            } else {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);

        } catch (AlgorithmMismatchException e) {
            log.error("doFilterInternal-> Exception: JWT algorithm mismatched");
            DefaultResponse defaultResponse = DefaultResponse.builder()
                    .code(ResponseCodeUtil.JWT_TOKEN_VALIDATE_ERROR_CODE)
                    .title(ResponseStatus.FAILED.name())
                    .message(LogMessageUtil.INVALID_CREDENTIAL)
                    .build();
            generateErrorResponse(response, defaultResponse);
        } catch (SignatureVerificationException e) {
            log.error("doFilterInternal-> Exception: JWT signature verification failed");
            DefaultResponse defaultResponse = DefaultResponse.builder()
                    .code(ResponseCodeUtil.JWT_TOKEN_VALIDATE_ERROR_CODE)
                    .title(ResponseStatus.FAILED.name())
                    .message(LogMessageUtil.INVALID_CREDENTIAL)
                    .build();
            generateErrorResponse(response, defaultResponse);
        } catch (TokenExpiredException e) {
            log.error("doFilterInternal-> Exception: JWT expired");
            DefaultResponse defaultResponse = DefaultResponse.builder()
                    .code(ResponseCodeUtil.JWT_TOKEN_EXPIRED_ERROR_CODE)
                    .title(ResponseStatus.FAILED.name())
                    .message(LogMessageUtil.INVALID_CREDENTIAL)
                    .build();
            generateErrorResponse(response, defaultResponse);
        } catch (InvalidClaimException e) {
            log.error("doFilterInternal-> Exception: JWT claim not valid");
            DefaultResponse defaultResponse = DefaultResponse.builder()
                    .code(ResponseCodeUtil.JWT_TOKEN_VALIDATE_ERROR_CODE)
                    .title(ResponseStatus.FAILED.name())
                    .message(LogMessageUtil.INVALID_CREDENTIAL)
                    .build();
            generateErrorResponse(response, defaultResponse);
        } catch (JWTVerificationException e) {
            log.error("doFilterInternal-> Exception: JWT verification failed");
            DefaultResponse defaultResponse = DefaultResponse.builder()
                    .code(ResponseCodeUtil.JWT_TOKEN_VALIDATE_ERROR_CODE)
                    .title(ResponseStatus.FAILED.name())
                    .message(LogMessageUtil.INVALID_CREDENTIAL)
                    .build();
            generateErrorResponse(response, defaultResponse);
        } catch (DeviceIdMisMatchException e) {
            log.error("doFilterInternal-> Exception: Device Id mismatch.");
            DefaultResponse defaultResponse = DefaultResponse.builder()
                    .code(ResponseCodeUtil.DEVICE_ID_MISMATCH_ERROR_CODE)
                    .title(ResponseStatus.FAILED.name())
                    .message(LogMessageUtil.USER_ALREADY_LOGGED_IN_ANOTHER_DEVICE)
                    .build();
            generateErrorResponse(response, defaultResponse, 402);
        } catch (Exception e) {
            log.error("doFilterInternal-> Exception: " + e.getMessage(), e);
            DefaultResponse defaultResponse = DefaultResponse.builder()
                    .code(ResponseCodeUtil.JWT_TOKEN_VALIDATE_ERROR_CODE)
                    .title(ResponseStatus.FAILED.name())
                    .message(LogMessageUtil.INVALID_CREDENTIAL)
                    .build();
            generateErrorResponse(re sponse, defaultResponse);
        } finally {
            MDC.remove(MDC_UID_KEY);
            request.removeAttribute("JWTRequestFilter.FILTERED");
        }
    }

    // Reads the JWT from the Authorization header, and then uses JWT to validate the token
    public UsernamePasswordAuthenticationToken getAuthentication(String token, HttpServletRequest request) {
        if (token != null) {
            // parse the token.
            String username = JWT.require(Algorithm.HMAC512(secretKey.getBytes()))
                    .build()
                    .verify(token.replace("Bearer ", ""))
                    .getSubject();

            if (username != null) {
                UserDetails userDetails = userService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                AppUser appUser = userRepository.findOneByUsername(username);
                String deviceId = request.getHeader("Device-Id");
                if (appUser != null) {
                    if (deviceId != null) {
                        if (deviceId.equals(appUser.getDeviceId())) {
                            if (appUser.getStatus().equals(Status.DISABLED.name())) {
                                log.warn(LogMessageUtil.USER_DISABLED);
                                throw new UserDisabledException(LogMessageUtil.USER_DISABLED);
                            }

                            if (appUser.getStatus().equals(Status.LOCKED.name())) {
                                log.warn(LogMessageUtil.USER_LOCKED);
                                throw new UserDisabledException(LogMessageUtil.USER_LOCKED);
                            }

                            if (tokenBlackListRepository.existsByToken(token.replace("Bearer ", ""))) {
                                log.warn("Token was blacklisted");
                                throw new TokenExpiredException("The token was expired", Instant.now());
                            }

                            request.setAttribute("user", appUser);
                            MDC.put(MDC_UID_KEY, appUser.getUsername());
                        } else {
                            log.warn(LogMessageUtil.DEVICE_ID_MISMATCH);
                            throw new DeviceIdMisMatchException(LogMessageUtil.DEVICE_ID_MISMATCH);
                        }
                    } else {
                        log.error(LogMessageUtil.DEVICE_ID_NOT_FOUND);
                        throw new JWTVerificationException(LogMessageUtil.BAD_REQUEST);
                    }
//                    }
                }
                return usernamePasswordAuthenticationToken;
            }
            return null;
        }
        return null;
    }

    private boolean skipRefreshToken(HttpServletRequest httpServletRequest) {
        String[] regs = {
                "/user/refresh-token"
        };
        Matcher matcher;
        for (String pathExpr : regs) {
            matcher = Pattern.compile(pathExpr).matcher(httpServletRequest.getServletPath());
            if (matcher.find()) {
                log.info("Request: PATH: " + httpServletRequest.getServletPath());
                return true;
            }
        }
        return false;
    }

    public void generateErrorResponse(HttpServletResponse response, DefaultResponse defaultResponse) throws IOException {
        generateErrorResponse(response, defaultResponse, HttpServletResponse.SC_UNAUTHORIZED);
    }

    private void generateErrorResponse(HttpServletResponse response, DefaultResponse defaultResponse, int httpStatus) throws IOException {
        PrintWriter writer = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(httpStatus);
        writer.print(new ObjectMapper().writeValueAsString(defaultResponse));
    }

}
