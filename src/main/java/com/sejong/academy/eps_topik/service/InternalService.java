package com.sejong.academy.eps_topik.service;

import com.sejong.academy.eps_topik.entities.AdminUser;
import com.sejong.academy.eps_topik.entities.Role;
import com.sejong.academy.eps_topik.enums.ResponseStatus;
import com.sejong.academy.eps_topik.enums.Status;
import com.sejong.academy.eps_topik.model.request.AdminUserRegisterRequest;
import com.sejong.academy.eps_topik.model.request.TokenRequest;
import com.sejong.academy.eps_topik.model.response.CommonResponse;
import com.sejong.academy.eps_topik.model.response.RegisterResponse;
import com.sejong.academy.eps_topik.repository.AdminUserRepository;
import com.sejong.academy.eps_topik.service.crypto.Crypto;
import com.sejong.academy.eps_topik.util.JwtUtil;
import com.sejong.academy.eps_topik.util.ResponseCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalService {

    private final AdminUserRepository adminUserRepository;
    private final RoleService roleService;
    private final Crypto crypto;
    private final JwtUtil jwtUtil;

    public CommonResponse registerAdminUser(AdminUserRegisterRequest request) {
        try {
            // Check if the user already exists
            AdminUser adminUser = adminUserRepository.findAdminUserByNic(request.getNic());

            if (adminUser != null) {
                log.error("❌ User found for this NIC: {}", request.getNic());
                return CommonResponse.builder()
                        .code(ResponseCodeUtil.FAILED_CODE)
                        .title(ResponseStatus.FAILED.name())
                        .message("User already exists for this NIC")
                        .build();
            }

            // Initialize a new AdminUser if not found
            adminUser = new AdminUser();  // Initializing adminUser here

            /*TODO - decrypt password using aes key*/

            // Setting user details
            adminUser.setName(request.getName());
            adminUser.setNic(request.getNic());
            adminUser.setStatus(Status.ACTIVE.name());
            adminUser.setPassword(request.getPassword());  // Assuming no decryption needed now

            // Get role and assign it
            Role role = roleService.getRoleByName("ROLE_ADMIN_USER");
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(role);
            adminUser.setRoles(userRoles);

            // Save the user
            adminUserRepository.save(adminUser);

            log.info("✅ Admin user registered successfully with NIC: {}", request.getNic());

            // Prepare the response with the token data
            RegisterResponse response = new RegisterResponse();

            TokenRequest tokenRequest = TokenRequest.builder()
                    .username(adminUser.getUsername())
                    .role(role.getName())
                    .build();

            // Create JWT token and refresh token
            String token = jwtUtil.createJwtToken(tokenRequest);
            response.setUsername(adminUser.getUsername());
            response.setToken(token);

            // Create new refresh tokens
            String refreshToken = jwtUtil.createRefreshToken(tokenRequest);
            response.setRefreshToken(refreshToken);

            return CommonResponse.builder()
                    .code(ResponseCodeUtil.SUCCESS_CODE)
                    .title(ResponseStatus.SUCCESS.name())
                    .message("User registered successfully")
                    .data(response)
                    .build();

        } catch (Exception e) {
            log.error("❌ Admin user registration failed -> {}", e.getMessage());
            return CommonResponse.builder()
                    .code(ResponseCodeUtil.FAILED_CODE)
                    .title(ResponseStatus.FAILED.name())
                    .message("Error occurred in the process.")
                    .build();
        }
    }


}
