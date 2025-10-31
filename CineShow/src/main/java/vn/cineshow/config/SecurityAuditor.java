package vn.cineshow.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Lấy userId hiện tại từ SecurityContext (tuỳ bạn map JWT -> Principal). */
@Component
public class SecurityAuditor {
    public Long currentUserIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) throw new RuntimeException("Unauthenticated");
        // tuỳ hệ thống của bạn: principal có thể là CustomUserDetails chứa id
        Object principal = auth.getPrincipal();
        if (principal instanceof HasUserId u) return u.getUserId();
        // fallback: nếu bạn lưu userId trong name
        try { return Long.parseLong(auth.getName()); } catch (Exception e) { throw new RuntimeException("No user id"); }
    }

    /** Contract tối thiểu để cast. Bạn có thể implement ở nơi khác. */
    public interface HasUserId { Long getUserId(); }
}
