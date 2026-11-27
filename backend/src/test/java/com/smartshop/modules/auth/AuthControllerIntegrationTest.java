package com.smartshop.modules.auth;

import com.smartshop.modules.user.entity.Role;
import com.smartshop.modules.user.entity.User;
import com.smartshop.modules.user.entity.UserRole;
import com.smartshop.modules.user.entity.UserRoleId;
import com.smartshop.modules.user.entity.UserStatus;
import com.smartshop.modules.user.repository.RoleRepository;
import com.smartshop.modules.user.repository.UserRepository;
import com.smartshop.modules.user.repository.UserRoleRepository;
import com.smartshop.security.JwtProperties;
import com.smartshop.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtProperties jwtProperties;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
    }

    @Test
    void shouldRegisterCustomerWithBcryptAndIgnoreClientRole() throws Exception {
        String email = "new.customer." + UUID.randomUUID() + "@smartshop.com";
        String payload = """
            {
              "email": "%s",
              "password": "Password@123",
              "fullName": "Test Customer",
              "phone": "0909123456",
              "role": "ADMIN"
            }
            """.formatted(email);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value(email))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andExpect(jsonPath("$.data.roles[0]").value("CUSTOMER"))
            .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).doesNotContain("passwordHash");

        User saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo("Password@123");
        assertThat(passwordEncoder.matches("Password@123", saved.getPasswordHash())).isTrue();

        List<UserRole> roles = userRoleRepository.findWithRoleByUserId(saved.getId());
        assertThat(roles).extracting(ur -> ur.getRole().getName()).containsExactly("CUSTOMER");
    }

    @Test
    void shouldRejectDuplicateEmailRegistration() throws Exception {
        String email = "dup." + UUID.randomUUID() + "@smartshop.com";
        String payload = """
            {
              "email": "%s",
              "password": "Password@123",
              "fullName": "First User"
            }
            """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void shouldLoginAndAccessMeEndpointWithoutExposingPasswordHash() throws Exception {
        String email = "login." + UUID.randomUUID() + "@smartshop.com";
        User user = createTestUser(email, "Password@123", UserStatus.ACTIVE, null, "CUSTOMER");

        String loginPayload = """
            {
              "email": "%s",
              "password": "Password@123"
            }
            """.formatted(email);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.data.user.email").value(email))
            .andExpect(jsonPath("$.data.user.passwordHash").doesNotExist())
            .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        assertThat(body).doesNotContain("passwordHash");

        String token = jwtService.generateToken(user.getId(), user.getEmail(), List.of("CUSTOMER"));

        mockMvc.perform(get("/api/v1/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(user.getId()))
            .andExpect(jsonPath("$.data.email").value(email))
            .andExpect(jsonPath("$.data.roles[0]").value("CUSTOMER"))
            .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldRejectWrongPasswordAndInactiveOrDeletedAccounts() throws Exception {
        String activeEmail = "active." + UUID.randomUUID() + "@smartshop.com";
        createTestUser(activeEmail, "Password@123", UserStatus.ACTIVE, null, "CUSTOMER");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "%s", "password": "WrongPassword!"}
                    """.formatted(activeEmail)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));

        String lockedEmail = "locked." + UUID.randomUUID() + "@smartshop.com";
        createTestUser(lockedEmail, "Password@123", UserStatus.LOCKED, null, "CUSTOMER");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "%s", "password": "Password@123"}
                    """.formatted(lockedEmail)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_ACTIVE"));

        String deletedEmail = "deleted." + UUID.randomUUID() + "@smartshop.com";
        createTestUser(deletedEmail, "Password@123", UserStatus.ACTIVE, LocalDateTime.now(), "CUSTOMER");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "%s", "password": "Password@123"}
                    """.formatted(deletedEmail)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_ACTIVE"));
    }

    @Test
    void shouldRejectInvalidOrExpiredJwtAndLockedUserWithExistingToken() throws Exception {
        String email = "revoked." + UUID.randomUUID() + "@smartshop.com";
        User user = createTestUser(email, "Password@123", UserStatus.ACTIVE, null, "CUSTOMER");
        String validToken = jwtService.generateToken(user.getId(), user.getEmail(), List.of("CUSTOMER"));

        // Lock user after token was issued; DB re-check in filter must reject old token
        user.setStatus(UserStatus.LOCKED);
        userRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/v1/users/me")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isUnauthorized());

        // Expired token
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
            .subject(String.valueOf(user.getId()))
            .claim("email", user.getEmail())
            .claim("roles", List.of("CUSTOMER"))
            .issuedAt(new Date(System.currentTimeMillis() - 120_000L))
            .expiration(new Date(System.currentTimeMillis() - 60_000L))
            .signWith(key, Jwts.SIG.HS256)
            .compact();

        mockMvc.perform(get("/api/v1/users/me")
                .header("Authorization", "Bearer " + expiredToken))
            .andExpect(status().isUnauthorized());

        // Tampered token
        mockMvc.perform(get("/api/v1/users/me")
                .header("Authorization", "Bearer " + validToken + "tampered"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldEnforceRoleBasedAuthorizationOnProtectedRoutes() throws Exception {
        String customerEmail = "cust." + UUID.randomUUID() + "@smartshop.com";
        User customer = createTestUser(customerEmail, "Password@123", UserStatus.ACTIVE, null, "CUSTOMER");
        String customerToken = jwtService.generateToken(customer.getId(), customer.getEmail(), List.of("CUSTOMER"));

        mockMvc.perform(get("/api/v1/admin/ping")
                .header("Authorization", "Bearer " + customerToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/seller/ping")
                .header("Authorization", "Bearer " + customerToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    private User createTestUser(
        String email,
        String rawPassword,
        UserStatus status,
        LocalDateTime deletedAt,
        String roleName
    ) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = User.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(rawPassword))
            .fullName("Test User")
            .status(status)
            .emailVerified(Boolean.FALSE)
            .deletedAt(deletedAt)
            .build();
        User saved = userRepository.saveAndFlush(user);

        UserRole userRole = UserRole.builder()
            .id(new UserRoleId(saved.getId(), role.getId()))
            .user(saved)
            .role(role)
            .build();
        userRoleRepository.saveAndFlush(userRole);
        return saved;
    }
}
