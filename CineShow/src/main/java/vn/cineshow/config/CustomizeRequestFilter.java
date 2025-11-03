package vn.cineshow.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.cineshow.enums.TokenType;
import vn.cineshow.exception.TokenExpiredException;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.impl.AccountDetailsService;

import java.io.IOException;

@Component
@Slf4j(topic = "CUSTOMIZE-REQUEST-FILTER")
@RequiredArgsConstructor

public class CustomizeRequestFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final AccountDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("{} {}", request.getMethod(), request.getRequestURI());

        //TODO: check authority by request url
        String authHeader = request.getHeader("Authorization");
        log.info("authHeader: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("Bearer authHeader: {}", authHeader.substring(0, 20));
            try {
                String username = jwtService.extractUsername(token, TokenType.ACCESS_TOKEN);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (TokenExpiredException e) {
                log.warn("Token expired: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                return;
            } catch (AccessDeniedException e) {
                log.warn("Invalid JWT: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Bỏ qua preflight và handshake WebSocket
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        if ("websocket".equalsIgnoreCase(request.getHeader("Upgrade"))) return true; // native WS
        // Bỏ qua các endpoint WS/STOMP
        return uri.startsWith("/ws-native")
                || uri.startsWith("/ws")
                || uri.startsWith("/sockjs")
                || uri.startsWith("/stomp")
                || uri.startsWith("/websocket"); // đường dẫn nội bộ của SockJS
    }


}
