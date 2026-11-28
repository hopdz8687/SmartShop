package com.smartshop.modules.order;

import com.smartshop.modules.catalog.entity.Category;
import com.smartshop.modules.catalog.entity.CategoryStatus;
import com.smartshop.modules.catalog.entity.Product;
import com.smartshop.modules.catalog.entity.ProductStatus;
import com.smartshop.modules.catalog.entity.ProductVariant;
import com.smartshop.modules.catalog.entity.VariantStatus;
import com.smartshop.modules.catalog.repository.CategoryRepository;
import com.smartshop.modules.catalog.repository.ProductRepository;
import com.smartshop.modules.catalog.repository.ProductVariantRepository;
import com.smartshop.modules.order.entity.Order;
import com.smartshop.modules.order.entity.OrderItem;
import com.smartshop.modules.order.entity.OrderStatus;
import com.smartshop.modules.order.entity.Payment;
import com.smartshop.modules.order.entity.PaymentMethod;
import com.smartshop.modules.order.entity.PaymentStatus;
import com.smartshop.modules.order.entity.SellerOrder;
import com.smartshop.modules.order.entity.SellerOrderStatus;
import com.smartshop.modules.order.repository.OrderItemRepository;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class SellerOrderIntegrationTest {

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
    private OrderRepository orderRepository;

    @Autowired
    private SellerOrderRepository sellerOrderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

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
        User user = userRepository.saveAndFlush(User.builder()
            .email(UUID.randomUUID() + "@test.com")
            .passwordHash(passwordEncoder.encode("Pass@1234"))
            .fullName("Test " + roleName)
            .status(UserStatus.ACTIVE)
            .emailVerified(false)
            .build());
        userRoleRepository.saveAndFlush(UserRole.builder()
            .id(new UserRoleId(user.getId(), role.getId()))
            .user(user)
            .role(role)
            .build());
        return user;
    }

    private String tokenFor(User user, String... roles) {
        return jwtService.generateToken(user.getId(), user.getEmail(), List.of(roles));
    }

    private SellerOrder createSellerOrder(User customer, Store store, String storePrefix) {
        Category cat = categoryRepository.saveAndFlush(Category.builder()
            .name("Cat-" + storePrefix)
            .slug("cat-" + UUID.randomUUID().toString().substring(0, 8))
            .status(CategoryStatus.ACTIVE)
            .build());
        Product product = productRepository.saveAndFlush(Product.builder()
            .store(store)
            .category(cat)
            .name("Product-" + storePrefix)
            .slug("prod-" + UUID.randomUUID().toString().substring(0, 8))
            .status(ProductStatus.ACTIVE)
            .build());
        ProductVariant variant = variantRepository.saveAndFlush(ProductVariant.builder()
            .product(product)
            .sku("SKU-" + UUID.randomUUID().toString().substring(0, 8))
            .variantName("Default")
            .price(new BigDecimal("200000"))
            .stockQuantity(10)
            .isDefault(true)
            .status(VariantStatus.ACTIVE)
            .build());

        BigDecimal total = new BigDecimal("400000");
        Order parent = orderRepository.saveAndFlush(Order.builder()
            .user(customer)
            .orderCode("ORD-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase())
            .recipientName("Customer A")
            .recipientPhone("0909123456")
            .shippingProvince("HCM")
            .shippingWard("Ben Nghe")
            .shippingAddress("123 Le Loi")
            .subtotal(total)
            .shippingFee(BigDecimal.ZERO)
            .discountAmount(BigDecimal.ZERO)
            .totalAmount(total)
            .orderStatus(OrderStatus.PENDING)
            .build());

        SellerOrder so = sellerOrderRepository.saveAndFlush(SellerOrder.builder()
            .order(parent)
            .store(store)
            .sellerOrderCode("SO-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase())
            .subtotal(total)
            .shippingFee(BigDecimal.ZERO)
            .discountAmount(BigDecimal.ZERO)
            .totalAmount(total)
            .status(SellerOrderStatus.PENDING)
            .build());

        orderItemRepository.saveAndFlush(OrderItem.builder()
            .sellerOrder(so)
            .variant(variant)
            .productName(product.getName())
            .variantDescription(variant.getVariantName())
            .sku(variant.getSku())
            .unitPrice(variant.getPrice())
            .quantity(2)
            .subtotal(total)
            .build());

        paymentRepository.saveAndFlush(Payment.builder()
            .sellerOrder(so)
            .paymentMethod(PaymentMethod.COD)
            .amount(total)
            .paymentStatus(PaymentStatus.PENDING)
            .build());

        return so;
    }

    @Test
    void sellerShouldListDetailConfirmAndProcessOwnOrderAndSyncParentStatus() throws Exception {
        User customer = createUser("CUSTOMER");
        User seller1 = createUser("SELLER");
        Store store1 = storeRepository.saveAndFlush(Store.builder()
            .user(seller1)
            .storeName("Store One")
            .slug("store1-" + UUID.randomUUID().toString().substring(0, 8))
            .status(StoreStatus.ACTIVE)
            .build());

        User seller2 = createUser("SELLER");
        Store store2 = storeRepository.saveAndFlush(Store.builder()
            .user(seller2)
            .storeName("Store Two")
            .slug("store2-" + UUID.randomUUID().toString().substring(0, 8))
            .status(StoreStatus.ACTIVE)
            .build());

        SellerOrder so1 = createSellerOrder(customer, store1, "S1");
        createSellerOrder(customer, store2, "S2");

        String token1 = tokenFor(seller1, "SELLER");

        // List returns only Store One's order
        mockMvc.perform(get("/api/v1/seller/orders")
                .header("Authorization", "Bearer " + token1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].id").value(so1.getId()))
            .andExpect(jsonPath("$.data.content[0].recipientName").value("Customer A"))
            .andExpect(jsonPath("$.data.content[0].items[0].quantity").value(2));

        // Detail
        mockMvc.perform(get("/api/v1/seller/orders/" + so1.getId())
                .header("Authorization", "Bearer " + token1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PENDING"));

        // Invalid jump: PENDING -> PROCESSING directly must fail (409)
        mockMvc.perform(put("/api/v1/seller/orders/" + so1.getId() + "/processing")
                .header("Authorization", "Bearer " + token1))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value("INVALID_ORDER_STATE"));

        // Valid transition: PENDING -> CONFIRMED
        mockMvc.perform(put("/api/v1/seller/orders/" + so1.getId() + "/confirm")
                .header("Authorization", "Bearer " + token1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        // Parent order status should now be PROCESSING
        assertThat(orderRepository.findById(so1.getOrder().getId()).orElseThrow().getOrderStatus())
            .isEqualTo(OrderStatus.PROCESSING);

        // Repeat confirm must fail (409)
        mockMvc.perform(put("/api/v1/seller/orders/" + so1.getId() + "/confirm")
                .header("Authorization", "Bearer " + token1))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value("INVALID_ORDER_STATE"));

        // Valid transition: CONFIRMED -> PROCESSING
        mockMvc.perform(put("/api/v1/seller/orders/" + so1.getId() + "/processing")
                .header("Authorization", "Bearer " + token1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PROCESSING"));
    }

    @Test
    void shouldEnforceStoreOwnershipAndRoleBasedAccess() throws Exception {
        User customer = createUser("CUSTOMER");
        User admin = createUser("ADMIN");
        User seller1 = createUser("SELLER");
        Store store1 = storeRepository.saveAndFlush(Store.builder()
            .user(seller1)
            .storeName("Owner Store")
            .slug("owner-" + UUID.randomUUID().toString().substring(0, 8))
            .status(StoreStatus.ACTIVE)
            .build());
        User seller2 = createUser("SELLER");
        storeRepository.saveAndFlush(Store.builder()
            .user(seller2)
            .storeName("Other Store")
            .slug("other-" + UUID.randomUUID().toString().substring(0, 8))
            .status(StoreStatus.ACTIVE)
            .build());

        SellerOrder so1 = createSellerOrder(customer, store1, "OWN");

        String seller2Token = tokenFor(seller2, "SELLER");
        String customerToken = tokenFor(customer, "CUSTOMER");
        String adminToken = tokenFor(admin, "ADMIN");

        // Another seller gets 403 on detail, confirm, and processing
        mockMvc.perform(get("/api/v1/seller/orders/" + so1.getId())
                .header("Authorization", "Bearer " + seller2Token))
            .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/seller/orders/" + so1.getId() + "/confirm")
                .header("Authorization", "Bearer " + seller2Token))
            .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/seller/orders/" + so1.getId() + "/processing")
                .header("Authorization", "Bearer " + seller2Token))
            .andExpect(status().isForbidden());

        // Customer and Admin get 403 on /api/v1/seller/orders
        mockMvc.perform(get("/api/v1/seller/orders")
                .header("Authorization", "Bearer " + customerToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/seller/orders")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isForbidden());

        // Unauthenticated gets 401
        mockMvc.perform(get("/api/v1/seller/orders"))
            .andExpect(status().isUnauthorized());
    }
}
