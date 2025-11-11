package vn.cineshow.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.cineshow.dto.request.AccountCreationRequest;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.enums.UserRole;
import vn.cineshow.exception.ResourceNotFoundException;
import vn.cineshow.model.Account;
import vn.cineshow.model.Role;
import vn.cineshow.model.User;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.RoleRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitializer {

    RoleRepository roleRepository;
    AccountRepository accountRepository;
    PasswordEncoder passwordEncoder;

    @Value("${admin.email_default}")
    @NonFinal
    String EMAIL_ADMIN;

    @Value("${admin.password_default}")
    @NonFinal
    String PASSWORD_ADMIN;

    @Value("${user-account-test.email}")
    @NonFinal
    String EMAIL_USER;

    @Value("${user-account-test.password}")
    @NonFinal
    String PASSWORD_USER;

    @Value("${staff-account-test.email}")
    @NonFinal
    String EMAIL_STAFF;

    @Value("${staff-account-test.password}")
    @NonFinal
    String PASSWORD_STAFF;

    @Value("${operation-account-test.email}")
    @NonFinal
    String EMAIL_OPERATION;

    @Value("${operation-account-test.password}")
    @NonFinal
    String PASSWORD_OPERATION;

    @Value("${business-account-test.email}")
    @NonFinal
    String EMAIL_BUSINESS;

    @Value("${business-account-test.password}")
    @NonFinal
    String PASSWORD_BUSINESS;

    @Bean
    public ApplicationRunner initData() {
        return args -> {
            initRoles();
            initAccounts();
        };
    }

    void initRoles() {
        List<Role> roles = List.of(
                new Role("ADMIN", "The administrator"),
                new Role("CUSTOMER", "The customer using system"),
                new Role("OPERATION", "The operation manager using system"),
                new Role("STAFF", "The staff using system"),
                new Role("BUSINESS", "The business manager using system")
        );

        roles.forEach(role -> roleRepository.findByRoleName(role.getRoleName())
                .orElseGet(() -> {
                    log.info("Initialized role: {}", role.getRoleName());
                    return roleRepository.save(role);
                }));
    }

    void initAccounts() {

        AccountCreationRequest admin = new AccountCreationRequest(EMAIL_ADMIN, PASSWORD_ADMIN, "System Admin", "Ha Noi");
        AccountCreationRequest customer = new AccountCreationRequest(EMAIL_USER, PASSWORD_USER, "System Customer", "Da Nang");
        AccountCreationRequest staff = new AccountCreationRequest(EMAIL_STAFF, PASSWORD_STAFF, "System Staff", "Ho Chi Minh");
        AccountCreationRequest manager = new AccountCreationRequest(EMAIL_OPERATION, PASSWORD_OPERATION, "Operations Manager", "Hai Phong");
        AccountCreationRequest manager2 = new AccountCreationRequest(EMAIL_BUSINESS, PASSWORD_BUSINESS, "Business Manager", "Hai Duong");

        createAccountIfNotExists(admin, UserRole.ADMIN.name(), UserRole.BUSINESS.name(), UserRole.CUSTOMER.name());
        createAccountIfNotExists(customer, UserRole.CUSTOMER.name());
        createAccountIfNotExists(staff, UserRole.STAFF.name(), UserRole.CUSTOMER.name());
        createAccountIfNotExists(manager, UserRole.OPERATION.name(), UserRole.CUSTOMER.name());
        createAccountIfNotExists(manager2, UserRole.BUSINESS.name(), UserRole.CUSTOMER.name());
    }

    void createAccountIfNotExists(AccountCreationRequest request, String... roleNames) {
        Set<Role> roles = Arrays.stream(roleNames)
                .map(name -> roleRepository.findByRoleName(name)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name)))
                .collect(Collectors.toSet());

        accountRepository.findAccountByEmail(request.getEmail())
                .ifPresentOrElse(existing -> {
                    boolean updated = existing.getRoles().addAll(roles);
                    if (updated) {
                        accountRepository.save(existing);
                        log.info("Updated roles for existing account: {}", request.getEmail());
                    } else {
                        log.warn("Account already exists with required roles: {}", request.getEmail());
                    }
                }, () -> {
                    User user = User.builder()
                            .name(request.getName())
                            .address(request.getAddress())
                            .loyalPoint(0)
                            .build();

                    Account account = Account.builder()
                            .email(request.getEmail())
                            .password(passwordEncoder.encode(request.getPassword()))
                            .roles(roles)
                            .user(user)
                            .status(AccountStatus.ACTIVE)
                            .build();

                    user.setAccount(account);
                    accountRepository.save(account);
                    log.info("Created account: {}", request.getEmail());
                });
    }
}
