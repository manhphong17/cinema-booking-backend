package vn.cineshow.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.enums.AuthProvider;
import vn.cineshow.enums.UserRole;
import vn.cineshow.model.Account;
import vn.cineshow.model.AccountProvider;
import vn.cineshow.model.Role;
import vn.cineshow.model.User;
import vn.cineshow.repository.AccountProviderRepository;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.RoleRepository;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.RefreshTokenService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AccountProviderRepository accountProviderRepository;
    private final RefreshTokenService refreshTokenService;

    private static final String REDIRECT_URI = "http://localhost:3000/customer";

    /**
     * Handles successful OAuth2 authentication.
     *
     * <p>This method is invoked when Spring Security completes an OAuth2 login flow successfully.
     * It ensures that the authenticated user is properly linked to an internal account, creates
     * the account if necessary, attaches the OAuth2 provider, generates JWT tokens, and finally
     * redirects the user to the frontend with the tokens.</p>
     *
     * <p>Processing steps:</p>
     * <ol>
     *   <li>Extract user information from {@link OAuth2AuthenticationToken}.</li>
     *   <li>Find the account in the database by email.</li>
     *   <li>If found, verify that the account has the GOOGLE provider; create it if missing.</li>
     *   <li>If not found:
     *       <ul>
     *         <li>Block the request if the path is restricted (e.g., "/google-admin").</li>
     *         <li>Otherwise, create a new account with default CUSTOMER role and attach GOOGLE provider.</li>
     *       </ul>
     *   </li>
     *   <li>Generate an access token and refresh token using {@link JWTService}.</li>
     *   <li>Redirect to the frontend callback URL with the tokens as query parameters.</li>
     * </ol>
     *
     * @param request        the current HTTP request
     * @param response       the current HTTP response
     * @param authentication the {@link Authentication} object, expected to be an instance of
     *                       {@link OAuth2AuthenticationToken} containing the authenticated user
     * @throws IOException      if an input or output error occurs
     * @throws ServletException if a servlet error occurs during handling
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attributes = authToken.getPrincipal().getAttributes();

        // information google returns
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String sub = (String) attributes.get("sub");

        String registrationId = authToken.getAuthorizedClientRegistrationId(); // "google-user" or "google-admin"

        // 1. Find account by email
        Optional<Account> accountOptional = accountRepository.findAccountByEmail(email);
        Account account;

        if (accountOptional.isPresent()) {
            account = accountOptional.get();

            // 1. check account status
            if (account.getStatus() == AccountStatus.DEACTIVATED || account.isDeleted()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Account inactive or deleted. Please contact support.");
                return;
            }

            if (account.getStatus() == AccountStatus.PENDING) {
                // if login with google -> active
                account.setStatus(AccountStatus.ACTIVE);
                accountRepository.save(account);
            }

            // 2. Đảm bảo account có provider GOOGLE
            accountProviderRepository.findByAccountAndProvider(account, AuthProvider.GOOGLE)
                    .orElseGet(() -> {
                        AccountProvider googleProvider = AccountProvider.builder()
                                .account(account)
                                .provider(AuthProvider.GOOGLE)
                                .providerId(sub)
                                .build();
                        return accountProviderRepository.save(googleProvider);
                    });

        } else {
            //if not exist account -> new account
            if ("google-admin".equals(registrationId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Admin must exist before you can login");
                return;
            }

            Role role = roleRepository.findByRoleName(UserRole.CUSTOMER.name())
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            User user = User.builder()
                    .name(name)
                    .loyalPoint(0)
                    .build();

            account = Account.builder()
                    .email(email)
                    .roles(Set.of(role))
                    .user(user)
                    .status(AccountStatus.ACTIVE)
                    .build();

            AccountProvider googleProvider = AccountProvider.builder()
                    .account(account)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(sub)
                    .build();
            account.setProviders(List.of(googleProvider));
            accountRepository.save(account);
        }

        List<String> roleNames = account.getRoles()
                .stream().map(role -> role.getRoleName())
                .toList();

        // 4. create refresh token
        String refreshToken = jwtService.generateRefreshToken(
                email,
                roleNames);

        refreshTokenService.replaceRefreshToken(account, refreshToken, jwtService.getRefreshTokenExpiryInSecond());

        // Set refresh token as HttpOnly cookie
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) jwtService.getRefreshTokenExpiryInSecond());
        response.addCookie(refreshCookie);

        response.sendRedirect(REDIRECT_URI);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain chain, Authentication authentication) throws IOException, ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }
}
