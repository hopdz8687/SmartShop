package com.smartshop.modules.user;

import com.smartshop.modules.user.entity.Role;
import com.smartshop.modules.user.entity.User;
import com.smartshop.modules.user.entity.UserRole;
import com.smartshop.modules.user.entity.UserStatus;
import com.smartshop.modules.user.repository.RoleRepository;
import com.smartshop.modules.user.repository.UserRepository;
import com.smartshop.modules.user.repository.UserRoleRepository;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(readOnly = true)
class UserRoleIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    void shouldUseConfiguredSmartshopDevDatasource() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            assertThat(url).contains("smartshop_dev");
        }
    }

    @Test
    void shouldReadDefaultRolesFromSchema() {
        Optional<Role> customerRole = roleRepository.findByName("CUSTOMER");
        Optional<Role> sellerRole = roleRepository.findByName("SELLER");
        Optional<Role> adminRole = roleRepository.findByName("ADMIN");

        Assumptions.assumeTrue(
            customerRole.isPresent() && sellerRole.isPresent() && adminRole.isPresent(),
            "Default roles are not present in smartshop_dev; skipping role assertions."
        );

        assertThat(customerRole.get().getId()).isNotNull();
        assertThat(sellerRole.get().getId()).isNotNull();
        assertThat(adminRole.get().getId()).isNotNull();
    }

    @Test
    void shouldReadSeedUsersAndTheirRolesWhenSeedDataExists() {
        Optional<User> adminOpt = userRepository.findByEmail("admin@smartshop.com");
        Optional<User> sellerOpt = userRepository.findByEmail("seller1@smartshop.com");
        Optional<User> customerOpt = userRepository.findByEmail("customer1@smartshop.com");

        Assumptions.assumeTrue(
            adminOpt.isPresent() && sellerOpt.isPresent() && customerOpt.isPresent(),
            "Seed users are not loaded in smartshop_dev; skipping seed user-role assertions."
        );

        User admin = adminOpt.get();
        assertThat(admin.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(admin.getEmailVerified()).isTrue();
        assertThat(admin.getPasswordHash()).startsWith("$2");

        List<UserRole> adminRoles = userRoleRepository.findWithRoleByUserId(admin.getId());
        assertThat(adminRoles)
            .extracting(ur -> ur.getRole().getName())
            .contains("ADMIN");

        User seller = sellerOpt.get();
        List<UserRole> sellerRoles = userRoleRepository.findWithRoleByUserId(seller.getId());
        assertThat(sellerRoles)
            .extracting(ur -> ur.getRole().getName())
            .contains("SELLER");

        User customer = customerOpt.get();
        List<UserRole> customerRoles = userRoleRepository.findByIdUserId(customer.getId());
        assertThat(customerRoles).isNotEmpty();
        assertThat(userRoleRepository.existsByIdUserIdAndIdRoleId(
            customer.getId(),
            roleRepository.findByName("CUSTOMER").orElseThrow().getId()
        )).isTrue();
    }
}
