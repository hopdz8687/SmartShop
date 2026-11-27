package com.smartshop.modules.store;

import com.smartshop.modules.store.entity.SellerApplicationStatus;
import com.smartshop.modules.store.entity.StoreStatus;
import com.smartshop.modules.store.repository.SellerApplicationRepository;
import com.smartshop.modules.store.repository.StoreRepository;
import com.smartshop.modules.user.entity.Role;
import com.smartshop.modules.user.entity.User;
import com.smartshop.modules.user.entity.UserRole;
import com.smartshop.modules.user.entity.UserRoleId;
import com.smartshop.modules.user.entity.UserStatus;
import com.smartshop.modules.user.repository.RoleRepository;
import com.smartshop.modules.user.repository.UserRepository;
import com.smartshop.modules.user.repository.UserRoleRepository;
import com.smartshop.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class StoreAndSellerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private SellerApplicationRepository applicationRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private User createUserWithRole(String roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = User.builder()
            .email(UUID.randomUUID() + "@test.com")
            .passwordHash(passwordEncoder.encode("Pass@1234"))
            .fullName("Test " + roleName)
            .status(UserStatus.ACTIVE)
            .emailVerified(false)
            .build();
        User saved = userRepository.saveAndFlush(user);
        UserRole ur = UserRole.builder()
            .id(new UserRoleId(saved.getId(), role.getId()))
            .user(saved)
            .role(role)
            .build();
        userRoleRepository.saveAndFlush(ur);
        return saved;
    }

    private String tokenFor(User user, String... roles) {
        return jwtService.generateToken(user.getId(), user.getEmail(), List.of(roles));
    }

    private Long submitApplication(User customer, String businessName) throws Exception {
        String token = tokenFor(customer, "CUSTOMER");
        String body = mockMvc.perform(post("/api/v1/seller-applications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"businessName": "%s"}
                    """.formatted(businessName)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();

        // Parse id from JSON
        int start = body.indexOf("\"id\":") + 5;
        int end = body.indexOf(",", start);
        return Long.parseLong(body.substring(start, end).trim());
    }

    // ─── 1. Submit Application ───────────────────────────────────────────────

    @Nested
    class SubmitApplication {

        @Test
        void shouldSubmitSuccessfully() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            String token = tokenFor(customer, "CUSTOMER");

            mockMvc.perform(post("/api/v1/seller-applications")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"businessName": "My Shop", "applicantNote": "Opening a bakery"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.businessName").value("My Shop"));
        }

        @Test
        void shouldReturn409WhenAlreadyHasPendingApplication() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            submitApplication(customer, "Shop A");

            String token = tokenFor(customer, "CUSTOMER");
            mockMvc.perform(post("/api/v1/seller-applications")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"businessName": "Shop B"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("PENDING_APPLICATION_EXISTS"));
        }

        @Test
        void shouldReturn400WhenBusinessNameIsBlank() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            String token = tokenFor(customer, "CUSTOMER");

            mockMvc.perform(post("/api/v1/seller-applications")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"businessName": ""}
                        """))
                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(post("/api/v1/seller-applications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"businessName": "Test"}
                        """))
                .andExpect(status().isUnauthorized());
        }
    }

    // ─── 2. Cancel Application ──────────────────────────────────────────────

    @Nested
    class CancelApplication {

        @Test
        void shouldCancelOwnPendingApplication() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            Long appId = submitApplication(customer, "Cancel Me");
            String token = tokenFor(customer, "CUSTOMER");

            mockMvc.perform(put("/api/v1/seller-applications/" + appId + "/cancel")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }

        @Test
        void shouldReturn403WhenCancellingAnotherUsersApplication() throws Exception {
            User customer1 = createUserWithRole("CUSTOMER");
            User customer2 = createUserWithRole("CUSTOMER");
            Long appId = submitApplication(customer1, "Shop Of Customer1");
            String token2 = tokenFor(customer2, "CUSTOMER");

            mockMvc.perform(put("/api/v1/seller-applications/" + appId + "/cancel")
                    .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn409WhenCancellingNonPendingApplication() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            Long appId = submitApplication(customer, "Shop To Cancel Twice");
            String token = tokenFor(customer, "CUSTOMER");

            // First cancel succeeds
            mockMvc.perform(put("/api/v1/seller-applications/" + appId + "/cancel")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

            // Second cancel must fail (already CANCELLED)
            mockMvc.perform(put("/api/v1/seller-applications/" + appId + "/cancel")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_APPLICATION_STATE"));
        }
    }

    // ─── 3. Get My Applications ─────────────────────────────────────────────

    @Nested
    class GetMyApplications {

        @Test
        void shouldReturnListForAuthenticatedCustomer() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            submitApplication(customer, "Shop Alpha");
            String token = tokenFor(customer, "CUSTOMER");

            mockMvc.perform(get("/api/v1/seller-applications/me")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].businessName").value("Shop Alpha"));
        }

        @Test
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/seller-applications/me"))
                .andExpect(status().isUnauthorized());
        }
    }

    // ─── 4. Admin Approve ────────────────────────────────────────────────────

    @Nested
    class AdminApprove {

        @Test
        void shouldApproveAndCreateStoreAndGrantSellerRole() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            Long appId = submitApplication(customer, "Approved Bakery");

            User admin = createUserWithRole("ADMIN");
            String adminToken = tokenFor(admin, "ADMIN");

            mockMvc.perform(put("/api/v1/admin/seller-applications/" + appId + "/approve")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

            // Store must have been created
            assertThat(storeRepository.existsByUserId(customer.getId())).isTrue();

            // SELLER role must have been granted
            Role sellerRole = roleRepository.findByName("SELLER").orElseThrow();
            assertThat(userRoleRepository.existsByIdUserIdAndIdRoleId(
                customer.getId(), sellerRole.getId()
            )).isTrue();
        }

        @Test
        void shouldReturn409WhenApprovingAlreadyApproved() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            Long appId = submitApplication(customer, "Double Approve Shop");

            User admin = createUserWithRole("ADMIN");
            String adminToken = tokenFor(admin, "ADMIN");

            mockMvc.perform(put("/api/v1/admin/seller-applications/" + appId + "/approve")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

            // Try to approve again — should fail (already APPROVED, not PENDING)
            // Because of @Transactional rollback the store is gone, but status changed in same tx.
            // We load the actual DB state after first approve flushes.
            // Attempting to cancel the already-approved application to test idempotency boundary.
            mockMvc.perform(put("/api/v1/admin/seller-applications/" + appId + "/approve")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_APPLICATION_STATE"));
        }

        @Test
        void shouldReturn403WhenCustomerTriesToApprove() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            Long appId = submitApplication(customer, "Shop For Auth Test");

            User customer2 = createUserWithRole("CUSTOMER");
            String custToken = tokenFor(customer2, "CUSTOMER");

            mockMvc.perform(put("/api/v1/admin/seller-applications/" + appId + "/approve")
                    .header("Authorization", "Bearer " + custToken))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldGenerateSlugFromBusinessName() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            Long appId = submitApplication(customer, "Tiệm Bánh Hạnh Phúc");

            User admin = createUserWithRole("ADMIN");
            String adminToken = tokenFor(admin, "ADMIN");

            mockMvc.perform(put("/api/v1/admin/seller-applications/" + appId + "/approve")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

            var store = storeRepository.findByUserId(customer.getId()).orElseThrow();
            assertThat(store.getSlug()).isNotBlank();
            // Vietnamese diacritics stripped → slug should not contain accented chars
            assertThat(store.getSlug()).matches("[a-z0-9\\-]+");
        }
    }

    // ─── 5. Admin Reject ─────────────────────────────────────────────────────

    @Nested
    class AdminReject {

        @Test
        void shouldRejectWithReason() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            Long appId = submitApplication(customer, "Shop To Reject");

            User admin = createUserWithRole("ADMIN");
            String adminToken = tokenFor(admin, "ADMIN");

            mockMvc.perform(put("/api/v1/admin/seller-applications/" + appId + "/reject")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"rejectionReason": "Incomplete documentation"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
        }

        @Test
        void shouldReturn400WhenRejectionReasonIsBlank() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            Long appId = submitApplication(customer, "Shop Missing Reason");

            User admin = createUserWithRole("ADMIN");
            String adminToken = tokenFor(admin, "ADMIN");

            mockMvc.perform(put("/api/v1/admin/seller-applications/" + appId + "/reject")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"rejectionReason": ""}
                        """))
                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn409WhenRejectingNonPendingApplication() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            Long appId = submitApplication(customer, "Shop Already Cancelled");

            // Cancel first
            String custToken = tokenFor(customer, "CUSTOMER");
            mockMvc.perform(put("/api/v1/seller-applications/" + appId + "/cancel")
                    .header("Authorization", "Bearer " + custToken))
                .andExpect(status().isOk());

            User admin = createUserWithRole("ADMIN");
            String adminToken = tokenFor(admin, "ADMIN");

            mockMvc.perform(put("/api/v1/admin/seller-applications/" + appId + "/reject")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"rejectionReason": "Too late"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_APPLICATION_STATE"));
        }
    }

    // ─── 6. Seller Store ─────────────────────────────────────────────────────

    @Nested
    class SellerStore {

        private User approveNewSeller(String businessName) throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            Long appId = submitApplication(customer, businessName);

            User admin = createUserWithRole("ADMIN");
            String adminToken = tokenFor(admin, "ADMIN");
            mockMvc.perform(put("/api/v1/admin/seller-applications/" + appId + "/approve")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

            // Grant SELLER role in memory for token generation
            Role sellerRole = roleRepository.findByName("SELLER").orElseThrow();
            userRoleRepository.saveAndFlush(UserRole.builder()
                .id(new UserRoleId(customer.getId(), sellerRole.getId()))
                .user(customer)
                .role(sellerRole)
                .build());

            return customer;
        }

        @Test
        void shouldReturnSellerStore() throws Exception {
            User seller = approveNewSeller("Seller Alpha Store");
            String sellerToken = tokenFor(seller, "SELLER");

            mockMvc.perform(get("/api/v1/seller/stores/me")
                    .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storeName").value("Seller Alpha Store"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        void shouldUpdateSellerStore() throws Exception {
            User seller = approveNewSeller("Seller Beta Store");
            String sellerToken = tokenFor(seller, "SELLER");

            mockMvc.perform(put("/api/v1/seller/stores/me")
                    .header("Authorization", "Bearer " + sellerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "storeName": "Beta Store Updated",
                          "description": "Best store ever",
                          "contactEmail": "beta@store.com",
                          "contactPhone": "0912345678"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storeName").value("Beta Store Updated"))
                .andExpect(jsonPath("$.data.description").value("Best store ever"));
        }

        @Test
        void shouldReturn403WhenCustomerAccessesSellerRoute() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            String custToken = tokenFor(customer, "CUSTOMER");

            mockMvc.perform(get("/api/v1/seller/stores/me")
                    .header("Authorization", "Bearer " + custToken))
                .andExpect(status().isForbidden());
        }
    }

    // ─── 7. Public Store ─────────────────────────────────────────────────────

    @Nested
    class PublicStore {

        @Test
        void shouldReturnActiveStoreWithoutAuth() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            Long appId = submitApplication(customer, "Public Visible Shop");

            User admin = createUserWithRole("ADMIN");
            String adminToken = tokenFor(admin, "ADMIN");
            mockMvc.perform(put("/api/v1/admin/seller-applications/" + appId + "/approve")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

            var store = storeRepository.findByUserId(customer.getId()).orElseThrow();

            mockMvc.perform(get("/api/v1/stores/" + store.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storeName").value("Public Visible Shop"))
                .andExpect(jsonPath("$.data.userId").doesNotExist());
        }

        @Test
        void shouldReturn404ForNonExistentStore() throws Exception {
            mockMvc.perform(get("/api/v1/stores/999999"))
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenStoreIsInactive() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            Long appId = submitApplication(customer, "Inactive Shop");

            User admin = createUserWithRole("ADMIN");
            String adminToken = tokenFor(admin, "ADMIN");
            mockMvc.perform(put("/api/v1/admin/seller-applications/" + appId + "/approve")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

            var store = storeRepository.findByUserId(customer.getId()).orElseThrow();
            store.setStatus(StoreStatus.INACTIVE);
            storeRepository.saveAndFlush(store);

            mockMvc.perform(get("/api/v1/stores/" + store.getId()))
                .andExpect(status().isNotFound());
        }
    }

    // ─── 8. Admin List Applications ─────────────────────────────────────────

    @Nested
    class AdminListApplications {

        @Test
        void shouldReturnPaginatedApplications() throws Exception {
            User c1 = createUserWithRole("CUSTOMER");
            User c2 = createUserWithRole("CUSTOMER");
            submitApplication(c1, "Shop One");
            submitApplication(c2, "Shop Two");

            User admin = createUserWithRole("ADMIN");
            String adminToken = tokenFor(admin, "ADMIN");

            mockMvc.perform(get("/api/v1/admin/seller-applications")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        void shouldFilterApplicationsByStatus() throws Exception {
            User customer = createUserWithRole("CUSTOMER");
            submitApplication(customer, "Filtered Shop");

            User admin = createUserWithRole("ADMIN");
            String adminToken = tokenFor(admin, "ADMIN");

            mockMvc.perform(get("/api/v1/admin/seller-applications?status=PENDING")
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
        }
    }
}
