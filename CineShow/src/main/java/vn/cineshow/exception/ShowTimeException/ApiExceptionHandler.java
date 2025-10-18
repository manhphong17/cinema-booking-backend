package vn.cineshow.exception.ShowTimeException;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "api/showtimes/createShowtime")
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private Map<String, Object> baseBody(String path, ErrorCodShowTime ec, String msg) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("timestamp", OffsetDateTime.now().toString());
        b.put("path", path);
        b.put("status", ec.getStatus().value());
        b.put("code", ec.getCode());           // mã SỐ
        b.put("error", ec.name());
        b.put("message", msg != null ? msg : ec.getDefaultMessage());
        return b;
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String,Object>> handleApp(AppException ex, ServletWebRequest req) {
        log.error("[{}] {}", ex.getErrorCode().getCode(), ex.getMessage());
        var body = baseBody(req.getRequest().getRequestURI(), ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleBeanValidation(MethodArgumentNotValidException ex,
                                                                   ServletWebRequest req) {
        var ec = ErrorCodShowTime.VALIDATION_FAILED;
        var body = baseBody(req.getRequest().getRequestURI(), ec, null);
        body.put("fields", ex.getBindingResult().getFieldErrors().stream()
                .map(f -> Map.of("field", f.getField(), "message", f.getDefaultMessage()))
                .toList());
        return ResponseEntity.status(ec.getStatus()).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String,Object>> handleBadJson(HttpMessageNotReadableException ex,
                                                            ServletWebRequest req) {
        var ec = ErrorCodShowTime.INVALID_JSON_OR_FORMAT;
        return ResponseEntity.status(ec.getStatus()).body(baseBody(req.getRequest().getRequestURI(), ec, null));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, ConstraintViolationException.class})
    public ResponseEntity<Map<String,Object>> handleTypeMismatch(Exception ex, ServletWebRequest req) {
        var ec = ErrorCodShowTime.TYPE_MISMATCH;
        return ResponseEntity.status(ec.getStatus()).body(baseBody(req.getRequest().getRequestURI(), ec, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleUnknown(Exception ex, ServletWebRequest req) {
        var ec = ErrorCodShowTime.INTERNAL_ERROR;
        return ResponseEntity.status(ec.getStatus()).body(baseBody(req.getRequest().getRequestURI(), ec, null));
    }
}
