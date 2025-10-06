package vn.cineshow.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnknownException(Exception e, WebRequest request) {
        log.info("================> UNKNOW ERROR");
        ErrorResponse errorResponse = new ErrorResponse();
        setResponseBadRequest(errorResponse, request);

        if (e != null) {
            errorResponse.setMessage(e.getMessage());
        }
        return errorResponse;

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation Error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "400 Response",
                                    summary = "Dữ liệu đầu vào không hợp lệ",
                                    value = """
                                            {
                                              "timestamp": "2023-10-19T06:07:35.321+00:00",
                                              "status": 400,
                                              "path": "/api/v1/...",
                                              "error": "Field 'email' is invalid"
                                            }
                                            """
                            )
                    )
            )
    })
    public ErrorResponse handleValidationError(MethodArgumentNotValidException e, WebRequest req) {
        log.info("================> BAD REQUEST");
        ErrorResponse errorResponse = new ErrorResponse();
        setResponseBadRequest(errorResponse, req);

        String message = e.getMessage();
        int start = message.lastIndexOf("[");
        int end = message.lastIndexOf("]");
        message = message.substring(start + 1, end - 1);
        errorResponse.setMessage(message);

        errorResponse.setMessage(message);
        return errorResponse;
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "500",
                    description = "Type Mismatch Error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "500 Response",
                                    summary = "Kiểu dữ liệu không khớp",
                                    value = """
                                            {
                                              "timestamp": "2023-10-19T06:07:35.321+00:00",
                                              "status": 500,
                                              "path": "/api/v1/...",
                                              "error": "Parameter 'id' should be of type Long"
                                            }
                                            """
                            )
                    )
            )
    })
    public ErrorResponse handleInternalServerError(MethodArgumentTypeMismatchException e, WebRequest req) {
        log.info("================> INTERNAL SERVER ERROR");
        ErrorResponse errorResponse = new ErrorResponse();
        setResponseBadRequest(errorResponse, req);

        if (e != null) {
            errorResponse.setMessage(e.getMessage());
        }
        return errorResponse;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource Not Found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "404 Response",
                                    summary = "Không tìm thấy tài nguyên",
                                    value = """
                                            {
                                              "timestamp": "2023-10-19T06:07:35.321+00:00",
                                              "status": 404,
                                              "path": "/api/v1/resource/123",
                                              "error": "Resource with id 123 not found"
                                            }
                                            """
                            )
                    )
            )
    })
    public ErrorResponse handleResourceNotFoundError(ResourceNotFoundException e, WebRequest req) {
        log.info("================> RESOURCE NOT FOUND ERROR");
        ErrorResponse errorResponse = new ErrorResponse();
        setResponseBadRequest(errorResponse, req);

        if (e != null) {
            errorResponse.setMessage(e.getMessage());
        }
        return errorResponse;
    }


    @ExceptionHandler(AuthenticatedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "401 Response",
                                    summary = "Unauthenticated user",
                                    value = """
                                            {
                                              "timestamp": "2023-10-19T06:07:35.321+00:00",
                                              "status": 401,
                                              "path": "/api/v1/secure-resource",
                                              "error": "Authentication required"
                                            }
                                            """
                            )
                    )
            )
    })
    public ErrorResponse handleAuthenticatedError(AuthenticatedException e, WebRequest req) {
        log.info("================> Authenticated ERROR");
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTitle(HttpStatus.UNAUTHORIZED.getReasonPhrase()); // "Bad Request"
        errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        errorResponse.setInstance(req.getDescription(false).replace("uri=", ""));
        errorResponse.setTimestamp(LocalDateTime.now());

        if (e != null) {
            errorResponse.setMessage(e.getMessage());
        }
        return errorResponse;
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "404",
                    description = "Username Not Found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "404 Response",
                                    summary = "User not found error",
                                    value = """
                                            {
                                              "timestamp": "2023-10-19T06:07:35.321+00:00",
                                              "status": 404,
                                              "path": "/api/v1/...",
                                              "error": "User with email abc@gmail.com not found"
                                            }
                                            """
                            )
                    )
            )
    })
    public ErrorResponse handleAuthenticatedError(UsernameNotFoundException e, WebRequest req) {
        log.info("================> USERNAME NOT FOUND ERROR");
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTitle(HttpStatus.NOT_FOUND.getReasonPhrase()); // "NOT_FOUND"
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setInstance(req.getDescription(false).replace("uri=", ""));
        errorResponse.setTimestamp(LocalDateTime.now());

        if (e != null) {
            errorResponse.setMessage(e.getMessage());
        }
        return errorResponse;
    }

    @ExceptionHandler({NullPointerException.class, InvalidParameterException.class, DuplicateResourceException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request Error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "400 Response",
                                    summary = "Invalid request error",
                                    value = """
                                            {
                                              "timestamp": "2023-10-19T06:07:35.321+00:00",
                                              "status": 400,
                                              "path": "/api/v1/...",
                                              "error": "Invalid parameter"
                                            }
                                            """
                            )
                    )
            )
    })
    public ErrorResponse handleNullPointerError(Exception e, WebRequest req) {
        log.info("================> ERROR");
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase()); // "NOT_FOUND"
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setInstance(req.getDescription(false).replace("uri=", ""));
        errorResponse.setTimestamp(LocalDateTime.now());

        if (e != null) {
            errorResponse.setMessage(e.getMessage());
        }
        return errorResponse;
    }


    /**
     * Handle exception when access forbidden
     *
     * @param e       Exception
     * @param request WebRequest
     * @return ErrorResponse
     */
    @ExceptionHandler({HttpClientErrorException.Forbidden.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "403",
                    description = "Access Denied",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "403 Response",
                                    summary = "Handle exception when access forbidden",
                                    value = """
                                            {
                                              "timestamp": "2023-10-19T06:07:35.321+00:00",
                                              "status": 403,
                                              "path": "/api/v1/...",
                                              "error": "Access Denied"
                                            }
                                            """
                            )
                    )
            )
    })
    public ErrorResponse handleForBiddenException(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setInstance(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(HttpStatus.FORBIDDEN.value());
        errorResponse.setTitle(HttpStatus.FORBIDDEN.getReasonPhrase());
        errorResponse.setMessage(e.getMessage());
        return errorResponse;
    }


    /**
     * Handle exception when access forbidden
     *
     * @param e       Exception
     * @param request WebRequest
     * @return ErrorResponse
     */
    @ExceptionHandler({AuthenticationServiceException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "401 Response",
                                    summary = "Handle exception when user not unauthenticated",
                                    value = """
                                            {
                                              "timestamp": "2023-10-19T06:07:35.321+00:00",
                                              "status": 403,
                                              "path": "/api/v1/...",
                                              "error": "Email or password is incorrect"
                                            }
                                            """
                            )
                    )
            )
    })
    public ErrorResponse handleAuthenticationServiceException(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setInstance(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        errorResponse.setTitle(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        errorResponse.setMessage("Email or password is incorrect");
        return errorResponse;
    }


    @ExceptionHandler(AppException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleAppException(AppException e, WebRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setInstance(request.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(errorCode.getCode());
        errorResponse.setTitle(errorCode.name());
        errorResponse.setMessage(errorCode.getMessage());
        return errorResponse;
    }

    private void setResponseBadRequest(ErrorResponse errorResponse, WebRequest req) {
        errorResponse.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase()); // "Bad Request"
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setInstance(req.getDescription(false).replace("uri=", ""));
        errorResponse.setTimestamp(LocalDateTime.now());
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    @ResponseStatus // Status HTTP sẽ lấy từ ex.getStatusCode()
    public ErrorResponse handleResponseStatusException(
            org.springframework.web.server.ResponseStatusException ex,
            WebRequest req
    ) {
        HttpStatusCode statusCode = ex.getStatusCode();
        int code = statusCode.value();

        // Resolve sang HttpStatus (có thể null nếu code lạ)
        HttpStatus httpStatus = HttpStatus.resolve(code);
        String title = (httpStatus != null) ? httpStatus.getReasonPhrase() : "Error";

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setInstance(req.getDescription(false).replace("uri=", ""));
        errorResponse.setStatus(code);
        errorResponse.setTitle(title);

        // Fallback VN message theo mã lỗi (nếu reason trống)
        String fallback = switch (code) {
            case 400 -> "Tham số không hợp lệ";
            case 401 -> "Bạn chưa được xác thực";
            case 403 -> "Truy cập bị từ chối";
            case 404 -> "Không tìm thấy tài nguyên";
            case 500 -> "Lỗi máy chủ, vui lòng thử lại";
            default -> "Đã xảy ra lỗi";
        };

        String reason = ex.getReason();
        errorResponse.setMessage((reason == null || reason.isBlank()) ? fallback : reason);
        return errorResponse;
    }
}