package com.smartshop.modules.order;

import com.smartshop.modules.cart.entity.CartItem;
import com.smartshop.modules.cart.repository.CartItemRepository;
import com.smartshop.modules.catalog.entity.Category;
import com.smartshop.modules.catalog.entity.CategoryStatus;
import com.smartshop.modules.catalog.entity.Product;
import com.smartshop.modules.catalog.entity.ProductImage;
import com.smartshop.modules.catalog.entity.ProductStatus;
import com.smartshop.modules.catalog.entity.ProductVariant;
import com.smartshop.modules.catalog.entity.VariantStatus;
import com.smartshop.modules.catalog.repository.CategoryRepository;
import com.smartshop.modules.catalog.repository.ProductImageRepository;
import com.smartshop.modules.catalog.repository.ProductRepository;
import com.smartshop.modules.catalog.repository.ProductVariantRepository;
import com.smartshop.modules.order.repository.OrderRepository;
import com.smartshop.modules.order.repository.PaymentRepository;
import com.smartshop.modules.order.repository.SellerOrderRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class CartAndCheckoutIntegrationTest {

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
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ProductImageRepository imageRepository;

    @Autowired
    private com.smartshop.modules.cart.repository.CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SellerOrderRepository sellerOrderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

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

    private Store createStore(User seller, String name) {
        return storeRepository.saveAndFlush(Store.builder()
            .user(seller)
            .storeName(name)
            .slug("store-" + UUID.randomUUID().toString().substring(0, 8))
            .status(StoreStatus.ACTIVE)
            .build());
    }

    private Category createCategory(String name) {
        return categoryRepository.saveAndFlush(Category.builder()
            .name(name)
            .slug("cat-" + UUID.randomUUID().toString().substring(0, 8))
            .status(CategoryStatus.ACTIVE)
            .build());
    }

    private ProductVariant createProductWithVariant(
        Store store,
        Category category,
        String productName,
        String variantName,
        BigDecimal price,
        int stock
    ) {
        Product product = productRepository.saveAndFlush(Product.builder()
            .store(store)
            .category(category)
            .name(productName)
            .slug("prod-" + UUID.randomUUID().toString().substring(0, 8))
            .status(ProductStatus.ACTIVE)
            .build());

        imageRepository.saveAndFlush(ProductImage.builder()
            .product(product)
            .imageUrl("https://cdn.example.com/" + product.getId() + ".jpg")
            .isPrimary(true)
            .displayOrder(0)
            .build());

        return variantRepository.saveAndFlush(ProductVariant.builder()
            .product(product)
            .sku("SKU-" + UUID.randomUUID().toString().substring(0, 8))
            .variantName(variantName)
            .price(price)
            .stockQuantity(stock)
            .isDefault(true)
            .status(VariantStatus.ACTIVE)
            .build());
    }

    private String tokenFor(User user, String... roles) {
        return jwtService.generateToken(user.getId(), user.getEmail(), List.of(roles));
    }

    private Long addItemAndGetCartItemId(String token, Long variantId, int qty) throws Exception {
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"variantId": %d, "quantity": %d}
                    """.formatted(variantId, qty)))
            .andExpect(status().isCreated());

        ProductVariant v = variantRepository.findById(variantId).orElseThrow();
        return cartItemRepository.findAll().stream()
            .filter(ci -> ci.getVariant().getId().equals(v.getId()))
            .map(CartItem::getId)
            .findFirst()
            .orElseThrow();
    }

    @Nested
    class CartCrudAndOwnership {

        @Test
        void shouldCrudCartAndEnforceOwnership() throws Exception {
            User customer = createUser("CUSTOMER");
            User otherCustomer = createUser("CUSTOMER");
            String token = tokenFor(customer, "CUSTOMER");
            String otherToken = tokenFor(otherCustomer, "CUSTOMER");

            User seller = createUser("SELLER");
            Store store = createStore(seller, "Cart Store");
            Category cat = createCategory("Electronics");
            ProductVariant variant = createProductWithVariant(
                store, cat, "Wireless Mouse", "Black", new BigDecimal("250000"), 10
            );

            // Empty cart
            mockMvc.perform(get("/api/v1/cart")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").value(0));

            // Add item qty=2
            Long cartItemId = addItemAndGetCartItemId(token, variant.getId(), 2);

            // Add same variant qty=3 -> merges to 5
            mockMvc.perform(post("/api/v1/cart/items")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"variantId": %d, "quantity": 3}
                        """.formatted(variant.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.totalItems").value(5))
                .andExpect(jsonPath("$.data.subtotal").value(1250000));

            // Update quantity to 4
            mockMvc.perform(put("/api/v1/cart/items/" + cartItemId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"quantity": 4}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").value(4));

            // Other customer cannot update or delete
            mockMvc.perform(put("/api/v1/cart/items/" + cartItemId)
                    .header("Authorization", "Bearer " + otherToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"quantity": 1}
                        """))
                .andExpect(status().isForbidden());

            mockMvc.perform(delete("/api/v1/cart/items/" + cartItemId)
                    .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());

            // Owner removes item
            mockMvc.perform(delete("/api/v1/cart/items/" + cartItemId)
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").value(0));
        }

        @Test
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class MultiStoreCheckout {

        @Test
        void shouldCheckoutSelectedItemsAcrossTwoStoresAndKeepUnselected() throws Exception {
            User customer = createUser("CUSTOMER");
            String token = tokenFor(customer, "CUSTOMER");

            User seller1 = createUser("SELLER");
            Store store1 = createStore(seller1, "Store One");
            User seller2 = createUser("SELLER");
            Store store2 = createStore(seller2, "Store Two");
            Category cat = createCategory("Gadgets");

            ProductVariant v1 = createProductWithVariant(
                store1, cat, "Keyboard", "Blue Switch", new BigDecimal("500000"), 10
            );
            ProductVariant v2 = createProductWithVariant(
                store2, cat, "Monitor", "27 inch", new BigDecimal("3000000"), 5
            );
            ProductVariant v3Unselected = createProductWithVariant(
                store1, cat, "USB Cable", "1m", new BigDecimal("50000"), 20
            );

            Long item1Id = addItemAndGetCartItemId(token, v1.getId(), 2); // 1,000,000
            Long item2Id = addItemAndGetCartItemId(token, v2.getId(), 1); // 3,000,000
            Long item3Id = addItemAndGetCartItemId(token, v3Unselected.getId(), 1);

            mockMvc.perform(post("/api/v1/orders")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "cartItemIds": [%d, %d],
                          "recipientName": "Nguyen Van A",
                          "recipientPhone": "0909123456",
                          "shippingProvince": "Ho Chi Minh",
                          "shippingDistrict": "Quan 1",
                          "shippingWard": "Ben Nghe",
                          "shippingAddress": "123 Le Loi",
                          "customerNote": "Giao gio hanh chinh"
                        }
                        """.formatted(item1Id, item2Id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(4000000))
                .andExpect(jsonPath("$.data.recipientName").value("Nguyen Van A"))
                .andExpect(jsonPath("$.data.sellerOrders.length()").value(2))
                .andExpect(jsonPath("$.data.sellerOrders[0].payment.paymentMethod").value("COD"))
                .andExpect(jsonPath("$.data.sellerOrders[0].payment.paymentStatus").value("PENDING"));

            // Verify stock decremented for v1 (10->8) and v2 (5->4), untouched for v3 (20)
            assertThat(variantRepository.findById(v1.getId()).orElseThrow().getStockQuantity()).isEqualTo(8);
            assertThat(variantRepository.findById(v2.getId()).orElseThrow().getStockQuantity()).isEqualTo(4);
            assertThat(variantRepository.findById(v3Unselected.getId()).orElseThrow().getStockQuantity()).isEqualTo(20);

            // Verify selected cart items deleted, unselected item remains
            assertThat(cartItemRepository.findById(item1Id)).isEmpty();
            assertThat(cartItemRepository.findById(item2Id)).isEmpty();
            assertThat(cartItemRepository.findById(item3Id)).isPresent();
        }

        @Test
        void shouldRollbackEverythingWhenStockIsInsufficient() throws Exception {
            User customer = createUser("CUSTOMER");
            String token = tokenFor(customer, "CUSTOMER");

            User seller1 = createUser("SELLER");
            Store store1 = createStore(seller1, "Rollback Store 1");
            User seller2 = createUser("SELLER");
            Store store2 = createStore(seller2, "Rollback Store 2");
            Category cat = createCategory("Rollback Cat");

            ProductVariant v1 = createProductWithVariant(
                store1, cat, "Item Valid", "Std", new BigDecimal("100000"), 5
            );
            ProductVariant v2 = createProductWithVariant(
                store2, cat, "Item Low Stock", "Std", new BigDecimal("200000"), 3
            );

            Long item1Id = addItemAndGetCartItemId(token, v1.getId(), 2);
            Long item2Id = addItemAndGetCartItemId(token, v2.getId(), 3);

            // Simulate concurrent stock reduction on v2 before checkout
            v2.setStockQuantity(1);
            variantRepository.saveAndFlush(v2);

            long orderCountBefore = orderRepository.count();
            long sellerOrderCountBefore = sellerOrderRepository.count();
            long paymentCountBefore = paymentRepository.count();

            mockMvc.perform(post("/api/v1/orders")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "cartItemIds": [%d, %d],
                          "recipientName": "Nguyen Van B",
                          "recipientPhone": "0909999888",
                          "shippingProvince": "Ha Noi",
                          "shippingWard": "Dich Vong",
                          "shippingAddress": "10 Cau Giay"
                        }
                        """.formatted(item1Id, item2Id)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_STOCK"));

            // Everything must be rolled back
            assertThat(orderRepository.count()).isEqualTo(orderCountBefore);
            assertThat(sellerOrderRepository.count()).isEqualTo(sellerOrderCountBefore);
            assertThat(paymentRepository.count()).isEqualTo(paymentCountBefore);
            assertThat(variantRepository.findById(v1.getId()).orElseThrow().getStockQuantity()).isEqualTo(5);
            assertThat(cartItemRepository.findById(item1Id)).isPresent();
            assertThat(cartItemRepository.findById(item2Id)).isPresent();
        }

        @Test
        void shouldForbidCheckoutOfAnotherUsersCartItem() throws Exception {
            User customer1 = createUser("CUSTOMER");
            User customer2 = createUser("CUSTOMER");
            String token1 = tokenFor(customer1, "CUSTOMER");
            String token2 = tokenFor(customer2, "CUSTOMER");

            User seller = createUser("SELLER");
            Store store = createStore(seller, "Auth Store");
            Category cat = createCategory("Auth Cat");
            ProductVariant v = createProductWithVariant(
                store, cat, "Auth Item", "Std", new BigDecimal("100000"), 5
            );

            Long item1Id = addItemAndGetCartItemId(token1, v.getId(), 1);

            mockMvc.perform(post("/api/v1/orders")
                    .header("Authorization", "Bearer " + token2)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "cartItemIds": [%d],
                          "recipientName": "Hacker",
                          "recipientPhone": "0900000000",
                          "shippingProvince": "HCM",
                          "shippingWard": "W1",
                          "shippingAddress": "Addr"
                        }
                        """.formatted(item1Id)))
                .andExpect(status().isForbidden());
        }
    }
}
