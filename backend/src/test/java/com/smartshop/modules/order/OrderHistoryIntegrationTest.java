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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class OrderHistoryIntegrationTest {

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

    private ProductVariant createSampleVariant(Store store, Category category) {
        Product product = productRepository.saveAndFlush(Product.builder()
            .store(store)
            .category(category)
            .name("Sample Product")
            .slug("prod-" + UUID.randomUUID().toString().substring(0, 8))
            .status(ProductStatus.ACTIVE)
            .build());
        return variantRepository.saveAndFlush(ProductVariant.builder()
            .product(product)
            .sku("SKU-" + UUID.randomUUID().toString().substring(0, 8))
            .variantName("Standard")
            .price(new BigDecimal("150000"))
            .stockQuantity(10)
            .isDefault(true)
            .status(VariantStatus.ACTIVE)
            .build());
    }

    private Order createSampleOrder(User customer, Store store, ProductVariant variant, String recipientName) {
        BigDecimal total = new BigDecimal("300000");
        Order order = orderRepository.saveAndFlush(Order.builder()
            .user(customer)
            .orderCode("ORD-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase())
            .recipientName(recipientName)
            .recipientPhone("0909123456")
            .shippingProvince("Ho Chi Minh")
            .shippingDistrict("Quan 1")
            .shippingWard("Ben Nghe")
            .shippingAddress("123 Le Loi")
            .subtotal(total)
            .shippingFee(BigDecimal.ZERO)
            .discountAmount(BigDecimal.ZERO)
            .totalAmount(total)
            .orderStatus(OrderStatus.PENDING)
            .build());

        SellerOrder sellerOrder = sellerOrderRepository.saveAndFlush(SellerOrder.builder()
            .order(order)
            .store(store)
            .sellerOrderCode("SO-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase())
            .subtotal(total)
            .shippingFee(BigDecimal.ZERO)
            .discountAmount(BigDecimal.ZERO)
            .totalAmount(total)
            .status(SellerOrderStatus.PENDING)
            .build());

        orderItemRepository.saveAndFlush(OrderItem.builder()
            .sellerOrder(sellerOrder)
            .variant(variant)
            .productName(variant.getProduct().getName())
            .variantDescription(variant.getVariantName())
            .sku(variant.getSku())
            .unitPrice(variant.getPrice())
            .quantity(2)
            .subtotal(total)
            .build());

        paymentRepository.saveAndFlush(Payment.builder()
            .sellerOrder(sellerOrder)
            .paymentMethod(PaymentMethod.COD)
            .amount(total)
            .paymentStatus(PaymentStatus.PENDING)
            .build());

        return order;
    }

    @Test
    void shouldReturnPaginatedOrderHistoryForCurrentCustomerOnly() throws Exception {
        User customer1 = createUser("CUSTOMER");
        User customer2 = createUser("CUSTOMER");
        User seller = createUser("SELLER");
        Store store = storeRepository.saveAndFlush(Store.builder()
            .user(seller)
            .storeName("History Store")
            .slug("store-" + UUID.randomUUID().toString().substring(0, 8))
            .status(StoreStatus.ACTIVE)
            .build());
        Category cat = categoryRepository.saveAndFlush(Category.builder()
            .name("History Cat")
            .slug("cat-" + UUID.randomUUID().toString().substring(0, 8))
            .status(CategoryStatus.ACTIVE)
            .build());
        ProductVariant variant = createSampleVariant(store, cat);

        createSampleOrder(customer1, store, variant, "Order 1");
        createSampleOrder(customer1, store, variant, "Order 2");
        createSampleOrder(customer1, store, variant, "Order 3");
        createSampleOrder(customer2, store, variant, "Other User Order");

        String token1 = tokenFor(customer1, "CUSTOMER");

        // Page 0, size 2 -> 2 of 3 orders
        mockMvc.perform(get("/api/v1/orders/my")
                .header("Authorization", "Bearer " + token1)
                .param("page", "0")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(3))
            .andExpect(jsonPath("$.data.totalPages").value(2))
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.content[0].sellerOrders[0].items[0].sku").value(variant.getSku()))
            .andExpect(jsonPath("$.data.content[0].sellerOrders[0].payment.paymentMethod").value("COD"));

        // Page 1, size 2 -> remaining 1 order
        mockMvc.perform(get("/api/v1/orders/my")
                .header("Authorization", "Bearer " + token1)
                .param("page", "1")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void shouldReturnOrderDetailAndEnforceOwnershipAndNotFound() throws Exception {
        User customer1 = createUser("CUSTOMER");
        User customer2 = createUser("CUSTOMER");
        User seller = createUser("SELLER");
        Store store = storeRepository.saveAndFlush(Store.builder()
            .user(seller)
            .storeName("Detail Store")
            .slug("store-" + UUID.randomUUID().toString().substring(0, 8))
            .status(StoreStatus.ACTIVE)
            .build());
        Category cat = categoryRepository.saveAndFlush(Category.builder()
            .name("Detail Cat")
            .slug("cat-" + UUID.randomUUID().toString().substring(0, 8))
            .status(CategoryStatus.ACTIVE)
            .build());
        ProductVariant variant = createSampleVariant(store, cat);

        Order order = createSampleOrder(customer1, store, variant, "Nguyen Van Detail");
        String token1 = tokenFor(customer1, "CUSTOMER");
        String token2 = tokenFor(customer2, "CUSTOMER");

        // Owner sees full detail
        mockMvc.perform(get("/api/v1/orders/" + order.getId())
                .header("Authorization", "Bearer " + token1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(order.getId()))
            .andExpect(jsonPath("$.data.recipientName").value("Nguyen Van Detail"))
            .andExpect(jsonPath("$.data.shippingProvince").value("Ho Chi Minh"))
            .andExpect(jsonPath("$.data.sellerOrders[0].storeName").value("Detail Store"))
            .andExpect(jsonPath("$.data.sellerOrders[0].items[0].quantity").value(2))
            .andExpect(jsonPath("$.data.sellerOrders[0].payment.paymentStatus").value("PENDING"));

        // Another user gets 403
        mockMvc.perform(get("/api/v1/orders/" + order.getId())
                .header("Authorization", "Bearer " + token2))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        // Nonexistent order gets 404
        mockMvc.perform(get("/api/v1/orders/999999")
                .header("Authorization", "Bearer " + token1))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("ORDER_NOT_FOUND"));
    }
}
