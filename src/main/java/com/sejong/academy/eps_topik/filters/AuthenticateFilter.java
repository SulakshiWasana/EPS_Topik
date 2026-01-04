package com.sejong.academy.eps_topik.filters;

import com.sejong.academy.eps_topik.entities.Parameter;
import com.sejong.academy.eps_topik.exception.ApiFailureException;
import com.sejong.academy.eps_topik.repository.ParameterRepository;
import com.sejong.academy.eps_topik.util.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(4)
public class AuthenticateFilter extends OncePerRequestFilter {

    private final ParameterRepository parameterRepository;

    // List of protected endpoints
    private final List<String> protectedEndpoints = List.of(

    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String endpointPath = requestUri.substring(contextPath.length());

        log.debug("Processing request for endpoint: {}", endpointPath);

        if (isProtectedPath(endpointPath)) {
            log.debug("Protected endpoint detected: {}", endpointPath);

            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
                log.warn("Missing or invalid Authorization header for endpoint: {}", endpointPath);
                respondWithUnauthorized(response, "Authentication required");
                return;
            }

            try {
                if (!isValidCredentials(authorizationHeader)) {
                    log.warn("Authentication failed for endpoint: {}", endpointPath);
                    respondWithUnauthorized(response, "Invalid or expired credentials");
                    return;
                }

                log.info("Authentication successful for endpoint: {}", endpointPath);

            } catch (IllegalArgumentException e) {
                log.error("Error decoding Authorization header at endpoint: {}", endpointPath, e);
                respondWithUnauthorized(response, "Invalid authentication format");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }


    private boolean isProtectedPath(String endpointPath) {
        return protectedEndpoints.stream().anyMatch(endpointPath::startsWith);
    }

    private boolean isValidCredentials(String authorizationHeader) {
        String base64Credentials = authorizationHeader.substring(6);
        String decodedCredentials = new String(Base64.getDecoder().decode(base64Credentials));
        String[] credentials = decodedCredentials.split(":", 2);

        if (credentials.length != 2) {
            log.debug("Credentials format invalid: {}", decodedCredentials);
            return false;
        }

        String authUsername = getBasicAuthParameter(AppConstants.BASIC_AUTH_USERNAME);
        String authPassword = getBasicAuthParameter(AppConstants.BASIC_AUTH_PASSWORD);

        return credentials[0].equals(authUsername) && credentials[1].equals(authPassword);
    }

    private void respondWithUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(message);
        response.getWriter().flush();
        log.debug("Unauthorized response sent: {}", message);
    }

    public String getBasicAuthParameter(String name) {
        Parameter parameterByName = parameterRepository.findParameterByName(name);

        if (parameterByName == null) {
            throw new ApiFailureException("Parameter not found: " + name);
        }

        return parameterByName.getValue();
    }
}
