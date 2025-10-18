package vn.cineshow.exception.ShowTimeException;

import lombok.Getter;

import java.util.Map;
@Getter
public class AppException  extends RuntimeException{
    private final ErrorCodShowTime errorCode;
    private final Map<String, Object> data;

    public AppException(ErrorCodShowTime ec) {
        super(ec.getDefaultMessage());
        this.errorCode = ec;
        this.data = Map.of();
    }
    public AppException(ErrorCodShowTime ec, String message) {
        super(message);
        this.errorCode = ec;
        this.data = Map.of();
    }
    public AppException(ErrorCodShowTime ec, Map<String, Object> data) {
        super(ec.getDefaultMessage());
        this.errorCode = ec;
        this.data = data;
    }
}
