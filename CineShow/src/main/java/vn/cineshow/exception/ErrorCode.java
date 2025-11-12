package vn.cineshow.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_EXISTED(1001, "User existed"),
    EMAIL_EXISTED(1002, "Email existed"),
    EMAIL_UN_VERIFIED(1003, "Email has not been verified"),
    ACCOUNT_INACTIVE(1004, "Account inactive"),
    INVALID_CREDENTIALS(1005, "Invalid email or password"),
    OTP_NOT_FOUND(1006, "OTP not found"),
    OTP_SEND_FAILED(1015, "Failed to send OTP"),
    OTP_INVALID(1007, "Invalid OTP"),
    OTP_EXPIRED(1008, "OTP expired"),
    INVALID_SORT_ORDER(1009, "Invalid Sort Order Exception"),
    MOVIE_NOT_FOUND(1010, "Movie not found"),
    LANGUAGE_NOT_FOUND(1011, "Language not found"),
    COUNTRY_NOT_FOUND(1012, "Country not found"),
    MOVIE_GENRE_NOT_FOUND(1013, "Movie Genre not found"),
    FILE_UPLOAD_FAILED(1014, "File upload failed"),
    CONCESSION_NOT_FOUND(1016, "Concession not found"),
    INVALID_QUANTITY(1017, "Invalid quantity to add"),
    INVALID_PARAMETER(1021, "Invalid parameter"),
    CONCESSION_ALREADY_DELETED(1018, "Concession has been deleted"),
    ACCOUNT_NOT_FOUND(1019, "Account not found"),
    PASSWORD_RESET_TOKEN_NOT_FOUND(1033, "Password reset token not found"),
    PASSWORD_RESET_TOKEN_INVALID(1034, "Password reset token is invalid or has expired"),

    CONCESSION_TYPE_NOT_FOUND(1025, "Concession type not found"),
    CONCESSION_TYPE_IN_USE(1026, "Concession has been in use"),
    CONCESSION_TYPE_EXISTED(1027, "Concession already existed"),
    HOLIDAY_EXISTED(1028, "Holiday has been added"),
    HOLIDAY_NOT_FOUND(1029, "Holiday not found"),
    SEAT_TYPE_NOT_FOUND(1030, "Seat type not found"),
    ROOM_TYPE_NOT_FOUND(1031, "Room type not found"),
    TICKET_PRICE_NOT_FOUND(1032, "Ticket price not found"),
    ORDER_SESSION_NOT_FOUND(1033, "Order-session not found"),
    USER_NOT_FOUND(1034, "User not found"),
    TICKET_NOT_FOUND(1035, "Ticket not found"),
    PAYMENT_METHOD_NOT_FOUND(1036, "Payment method not found"),
    PAYMENT_URL_GENERATION_FAILED(1037, "Payment URL generation failed"),

    SEAT_TYPE_ALREADY_EXISTED(1038, "Seat type with the same name already exists"),
    SEAT_TYPE_IN_USE(1039, "Seat type is in use and cannot be deleted"),
    ROOM_TYPE_ALREADY_EXISTED(1040, "Room type with the same name already exists"),
    ROOM_TYPE_IN_USE(1041, "Room type is in use and cannot be deleted"),
    ROOM_ALREADY_EXISTED(1042, "Room with the same name already exists"),
    ROOM_IN_USE(1043, "Room is in use and cannot be deleted"),

    SHOW_TIME_NOT_FOUND(3001, "Show time not found"),
    MOVIE_BANNER_NOT_FOUND(3001, "Movie banner not found"),

    REDIS_KEY_NOT_FOUND(3002, "Redis key not found"),


    //ShowTime error
    ROOM_NOT_FOUND(2003, "Room not found"),
    SUBTITLE_NOT_FOUND(2002, "Subtitle not found"),

    ROOM_INACTIVE(2033, "Room is inactive"),
    START_AFTER_END(2034, "startTime must be before endTime"),
    END_TOO_EARLY(2035, "endTime must be greater than startTime + movie.duration"),
    SHOWTIME_CONFLICT(2036, "Showtime conflicts with an existing show in the same room"),


    INTERNAL_ERROR(2037, "Unexpected error"),
    SHOWTIME_NOT_FOUND(2038, "Showtime not found"),
    ALREADY_DELETED(2039, "Showtime isdeleted"),
    CANNOT_DELETE_STARTED(2040, "Can not deleted"),
    INVALID_ENDTIME(2024, "End time must be greater than start time + movie duration"),


    //Order
    ORDER_NOT_FOUND(3101, "Order không tồn tại"),
    NOT_ORDER_OWNER(3102, "Bạn không có quyền xem đơn này"),
    ORDER_NOT_PAID(3103, "Đơn hàng chưa được thanh toán"),
    ORDER_CANCELED(3104, "Đơn hàng đã bị huỷ"),
    QR_EXPIRED(3105, "Mã QR đã hết hạn"),
    QR_REGENERATE_LIMIT(3106, "Tạo lại QR quá giới hạn"),
    SHOWTIME_PASSED(3107, "Suất chiếu đã diễn ra"),
    
    //Password
    PASSWORD_TOO_WEAK(1020, "Mật khẩu quá yếu"),


    // ===== THEATER (mới, 4001–4010) =====
    THEATER_NOT_FOUND(4001, "Theater details not found"),
    THEATER_INVALID_EMAIL(4002, "Invalid theater contact email"),
    THEATER_INVALID_PHONE(4003, "Invalid theater phone number"),
    THEATER_INVALID_URL(4004, "Invalid theater URL"),
    THEATER_INVALID_OPEN_CLOSE(4005, "closeTime must be after openTime (or enable overnight)"),
    THEATER_BANNER_UPLOAD_FAILED(4006, "Theater banner upload failed"),
    THEATER_CONFLICT_VERSION(4007, "Theater details were updated by another user");
    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}