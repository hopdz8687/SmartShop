package com.smartshop.modules.catalog;

import com.smartshop.modules.catalog.entity.Category;
import com.smartshop.modules.catalog.entity.CategoryStatus;
import com.smartshop.modules.catalog.repository.CategoryRepository;
import com.smartshop.modules.store.entity.Store;
import com.smartshop.modules.store.entity.StoreStatus;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class CatalogIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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

    private User createUser(String roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = User.builder()
            .email(UUID.randomUUID() + "@test.com")
            .passwordHash(passwordEncoder.encode("Pass@1234"))
            .fullName("Test " + roleName)
            .status(UserStatus.ACTIVE)
            .emailVerified(false)
            .build();
        User saved = userRepository.saveAndFlush(user);
        userRoleRepository.saveAndFlush(UserRole.builder()
            .id(new UserRoleId(saved.getId(), role.getId()))
            .user(saved)
            .role(role)
            .build());
        return saved;
    }

    private Store createStoreForSeller(User seller, String name) {
        return storeRepository.saveAndFlush(Store.builder()
            .user(seller)
            .storeName(name)
            .slug("store-" + UUID.randomUUID().toString().substring(0, 8))
            .status(StoreStatus.ACTIVE)
            .build());
    }

    private Category createCategory(String name, CategoryStatus status) {
        return categoryRepository.saveAndFlush(Category.builder()
            .name(name)
            .slug("cat-" + UUID.randomUUID().toString().substring(0, 8))
            .status(status)
            .build());
    }

    private String tokenFor(User user, String... roles) {
        return jwtService.generateToken(user.getId(), user.getEmail(), List.of(roles));
    }

    private Long extractId(String json) {
        int start = json.indexOf("\"id\":") + 5;
        int end = json.indexOf(",", start);
        return Long.parseLong(json.substring(start, end).trim());
    }

    @Nested
    class CategoryFlow {

        @Test
        void adminShouldCrudCategoryAndPublicSeesOnlyActive() throws Exception {
            User admin = createUser("ADMIN");
            String adminToken = tokenFor(admin, "ADMIN");

            String createResp = mockMvc.perform(post("/api/v1/admin/categories")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"name": "Điện thoại & Phụ kiện", "description": "Mobile devices"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.slug").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();

            Long catId = extractId(createResp);

            mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + catId + ")]").exists());

            mockMvc.perform(delete("/api/v1/admin/categories/" + catId)
                    .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + catId + ")]").doesNotExist());
        }

        @Test
        void customerShouldGet403OnAdminCategories() throws Exception {
            User customer = createUser("CUSTOMER");
            String custToken = tokenFor(customer, "CUSTOMER");

            mockMvc.perform(post("/api/v1/admin/categories")
                    .header("Authorization", "Bearer " + custToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"name": "Forbidden Cat"}
                        """))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    class SellerProductAndVariantFlow {

        @Test
        void sellerShouldCreateUpdateAndDeleteOwnProduct() throws Exception {
            User seller = createUser("SELLER");
            createStoreForSeller(seller, "Tech Store");
            String sellerToken = tokenFor(seller, "SELLER");
            Category cat = createCategory("Laptop", CategoryStatus.ACTIVE);
            String sku = "SKU-" + UUID.randomUUID().toString().substring(0, 8);

            String createBody = mockMvc.perform(post("/api/v1/seller/products")
                    .header("Authorization", "Bearer " + sellerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "categoryId": %d,
                          "name": "Laptop Pro 14",
                          "brand": "SmartBrand",
                          "status": "ACTIVE",
                          "variants": [
                            {
                              "sku": "%s",
                              "variantName": "16GB / 512GB",
                              "price": 25000000,
                              "stockQuantity": 10,
                              "isDefault": true
                            }
                          ],
                          "images": [
                            {
                              "imageUrl": "https://cdn.example.com/laptop.jpg",
                              "altText": "Laptop Pro 14",
                              "isPrimary": true
                            }
                          ]
                        }
                        """.formatted(cat.getId(), sku)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Laptop Pro 14"))
                .andExpect(jsonPath("$.data.variants[0].sku").value(sku))
                .andExpect(jsonPath("$.data.images[0].isPrimary").value(true))
                .andReturn().getResponse().getContentAsString();

            Long productId = extractId(createBody);

            // Another seller cannot modify this product
            User otherSeller = createUser("SELLER");
            createStoreForSeller(otherSeller, "Other Store");
            String otherToken = tokenFor(otherSeller, "SELLER");

            mockMvc.perform(put("/api/v1/seller/products/" + productId)
                    .header("Authorization", "Bearer " + otherToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"categoryId": %d, "name": "Hacked Name"}
                        """.formatted(cat.getId())))
                .andExpect(status().isForbidden());

            // Owner deletes product -> public detail returns 404
            mockMvc.perform(delete("/api/v1/seller/products/" + productId)
                    .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/products/" + productId))
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldEnforceVariantInvariants() throws Exception {
            User seller = createUser("SELLER");
            createStoreForSeller(seller, "Variant Store");
            String sellerToken = tokenFor(seller, "SELLER");
            Category cat = createCategory("Audio", CategoryStatus.ACTIVE);

            // Reject creating product with 2 variants and no default
            mockMvc.perform(post("/api/v1/seller/products")
                    .header("Authorization", "Bearer " + sellerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "categoryId": %d,
                          "name": "Headphones",
                          "variants": [
                            {"sku": "HP-1-%s", "price": 100, "stockQuantity": 5, "isDefault": false},
                            {"sku": "HP-2-%s", "price": 120, "stockQuantity": 5, "isDefault": false}
                          ]
                        }
                        """.formatted(cat.getId(), UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_DEFAULT_VARIANT"));

            // Create valid product with 1 variant, then try deactivating the only active/default variant
            String body = mockMvc.perform(post("/api/v1/seller/products")
                    .header("Authorization", "Bearer " + sellerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "categoryId": %d,
                          "name": "Speaker",
                          "variants": [
                            {"sku": "SPK-%s", "price": 500, "stockQuantity": 3, "isDefault": true}
                          ]
                        }
                        """.formatted(cat.getId(), UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

            int variantsIdx = body.indexOf("\"variants\":[{\"id\":");
            int idStart = variantsIdx + "\"variants\":[{\"id\":".length();
            int idEnd = body.indexOf(",", idStart);
            Long variantId = Long.parseLong(body.substring(idStart, idEnd).trim());

            mockMvc.perform(delete("/api/v1/seller/variants/" + variantId)
                    .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CANNOT_DEACTIVATE_DEFAULT_VARIANT"));
        }
    }

    @Nested
    class PublicSearchAndAdminProducts {

        @Test
        void shouldFilterPublicProductsAndAllowAdminStatusUpdate() throws Exception {
            User seller = createUser("SELLER");
            createStoreForSeller(seller, "Search Store");
            String sellerToken = tokenFor(seller, "SELLER");
            Category cat = createCategory("Watches", CategoryStatus.ACTIVE);

            String activeRes = mockMvc.perform(post("/api/v1/seller/products")
                    .header("Authorization", "Bearer " + sellerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "categoryId": %d,
                          "name": "Smart Watch Ultra",
                          "status": "ACTIVE",
                          "variants": [
                            {"sku": "SW-%s", "price": 1500000, "stockQuantity": 8, "isDefault": true}
                          ]
                        }
                        """.formatted(cat.getId(), UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

            Long activeId = extractId(activeRes);

            mockMvc.perform(get("/api/v1/products/search")
                    .param("keyword", "Ultra")
                    .param("categoryId", cat.getId().toString())
                    .param("minPrice", "1000000")
                    .param("maxPrice", "2000000")
                    .param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.id == " + activeId + ")].minPrice").value(1500000));

            User admin = createUser("ADMIN");
            String adminToken = tokenFor(admin, "ADMIN");

            mockMvc.perform(put("/api/v1/admin/products/" + activeId + "/status")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"status": "INACTIVE"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

            mockMvc.perform(get("/api/v1/products/" + activeId))
                .andExpect(status().isNotFound());
        }
    }
}
