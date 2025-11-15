package vn.cineshow.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // USER_EXISTED(1001, "User existed"),
    USER_EXISTED(1001, "Người dùng đã tồn tại"),
    
    // EMAIL_EXISTED(1002, "Email existed"),
    EMAIL_EXISTED(1002, "Email đã tồn tại"),
    
    // EMAIL_UN_VERIFIED(1003, "Email has not been verified"),
    EMAIL_UN_VERIFIED(1003, "Email chưa được xác minh"),
    
    // ACCOUNT_INACTIVE(1004, "Account inactive"),
    ACCOUNT_INACTIVE(1004, "Tài khoản không hoạt động"),
    
    // INVALID_CREDENTIALS(1005, "Invalid email or password"),
    INVALID_CREDENTIALS(1005, "Email hoặc mật khẩu không đúng"),
    
    // OTP_NOT_FOUND(1006, "OTP not found"),
    OTP_NOT_FOUND(1006, "Không tìm thấy mã OTP"),
    
    // OTP_SEND_FAILED(1015, "Failed to send OTP"),
    OTP_SEND_FAILED(1015, "Gửi mã OTP thất bại"),
    
    // OTP_INVALID(1007, "Invalid OTP"),
    OTP_INVALID(1007, "Mã OTP không hợp lệ"),
    
    // OTP_EXPIRED(1008, "OTP expired"),
    OTP_EXPIRED(1008, "Mã OTP đã hết hạn"),
    
    // INVALID_SORT_ORDER(1009, "Invalid Sort Order Exception"),
    INVALID_SORT_ORDER(1009, "Thứ tự sắp xếp không hợp lệ"),
    
    // MOVIE_NOT_FOUND(1010, "Movie not found"),
    MOVIE_NOT_FOUND(1010, "Không tìm thấy phim"),
    
    // LANGUAGE_NOT_FOUND(1011, "Language not found"),
    LANGUAGE_NOT_FOUND(1011, "Không tìm thấy ngôn ngữ"),
    
    // COUNTRY_NOT_FOUND(1012, "Country not found"),
    COUNTRY_NOT_FOUND(1012, "Không tìm thấy quốc gia"),
    
    // MOVIE_GENRE_NOT_FOUND(1013, "Movie Genre not found"),
    MOVIE_GENRE_NOT_FOUND(1013, "Không tìm thấy thể loại phim"),
    
    // FILE_UPLOAD_FAILED(1014, "File upload failed"),
    FILE_UPLOAD_FAILED(1014, "Tải file lên thất bại"),
    
    // CONCESSION_NOT_FOUND(1016, "Concession not found"),
    CONCESSION_NOT_FOUND(1016, "Không tìm thấy đồ ăn thức uống"),
    
    // INVALID_QUANTITY(1017, "Invalid quantity to add"),
    INVALID_QUANTITY(1017, "Số lượng không hợp lệ"),
    
    // INVALID_PARAMETER(1021, "Invalid parameter"),
    INVALID_PARAMETER(1021, "Tham số không hợp lệ"),
    
    // CONCESSION_ALREADY_DELETED(1018, "Concession has been deleted"),
    CONCESSION_ALREADY_DELETED(1018, "Đồ ăn thức uống đã bị xóa"),
    
    // ACCOUNT_NOT_FOUND(1019, "Account not found"),
    ACCOUNT_NOT_FOUND(1019, "Không tìm thấy tài khoản"),
    
    // PASSWORD_RESET_TOKEN_NOT_FOUND(1033, "Password reset token not found"),
    PASSWORD_RESET_TOKEN_NOT_FOUND(1033, "Không tìm thấy token đặt lại mật khẩu"),
    
    // PASSWORD_RESET_TOKEN_INVALID(1034, "Password reset token is invalid or has expired"),
    PASSWORD_RESET_TOKEN_INVALID(1034, "Token đặt lại mật khẩu không hợp lệ hoặc đã hết hạn"),

    // CONCESSION_TYPE_NOT_FOUND(1025, "Concession type not found"),
    CONCESSION_TYPE_NOT_FOUND(1025, "Không tìm thấy loại đồ ăn thức uống"),
    
    // CONCESSION_TYPE_IN_USE(1026, "Concession has been in use"),
    CONCESSION_TYPE_IN_USE(1026, "Loại đồ ăn thức uống đang được sử dụng"),
    
    // CONCESSION_TYPE_EXISTED(1027, "Concession already existed"),
    CONCESSION_TYPE_EXISTED(1027, "Loại đồ ăn thức uống đã tồn tại"),
    
    // HOLIDAY_EXISTED(1028, "Holiday has been added"),
    HOLIDAY_EXISTED(1028, "Ngày lễ đã được thêm"),
    
    // HOLIDAY_NOT_FOUND(1029, "Holiday not found"),
    HOLIDAY_NOT_FOUND(1029, "Không tìm thấy ngày lễ"),
    
    // SEAT_TYPE_NOT_FOUND(1030, "Seat type not found"),
    SEAT_TYPE_NOT_FOUND(1030, "Không tìm thấy loại ghế"),
    
    // ROOM_TYPE_NOT_FOUND(1031, "Room type not found"),
    ROOM_TYPE_NOT_FOUND(1031, "Không tìm thấy loại phòng"),
    
    // TICKET_PRICE_NOT_FOUND(1032, "Ticket price not found"),
    TICKET_PRICE_NOT_FOUND(1032, "Không tìm thấy giá vé"),
    
    // ORDER_SESSION_NOT_FOUND(1033, "Order-session not found"),
    ORDER_SESSION_NOT_FOUND(1033, "Không tìm thấy phiên đặt hàng"),
    
    // USER_NOT_FOUND(1034, "User not found"),
    USER_NOT_FOUND(1034, "Không tìm thấy người dùng"),
    
    // TICKET_NOT_FOUND(1035, "Ticket not found"),
    TICKET_NOT_FOUND(1035, "Không tìm thấy vé"),
    
    // PAYMENT_METHOD_NOT_FOUND(1036, "Payment method not found"),
    PAYMENT_METHOD_NOT_FOUND(1036, "Không tìm thấy phương thức thanh toán"),
    
    // PAYMENT_URL_GENERATION_FAILED(1037, "Payment URL generation failed"),
    PAYMENT_URL_GENERATION_FAILED(1037, "Tạo URL thanh toán thất bại"),

    // SEAT_TYPE_ALREADY_EXISTED(1038, "Seat type with the same name already exists"),
    SEAT_TYPE_ALREADY_EXISTED(1038, "Loại ghế với tên này đã tồn tại"),
    
    // SEAT_TYPE_IN_USE(1039, "Seat type is in use and cannot be deleted"),
    SEAT_TYPE_IN_USE(1039, "Loại ghế đang được sử dụng và không thể xóa"),
    
    // ROOM_TYPE_ALREADY_EXISTED(1040, "Room type with the same name already exists"),
    ROOM_TYPE_ALREADY_EXISTED(1040, "Loại phòng với tên này đã tồn tại"),
    
    // ROOM_TYPE_IN_USE(1041, "Room type is in use and cannot be deleted"),
    ROOM_TYPE_IN_USE(1041, "Loại phòng đang được sử dụng và không thể xóa"),
    
    // ROOM_ALREADY_EXISTED(1042, "Room with the same name already exists"),
    ROOM_ALREADY_EXISTED(1042, "Phòng với tên này đã tồn tại"),
    
    // ROOM_IN_USE(1043, "Room is in use and cannot be deleted"),
    ROOM_IN_USE(1043, "Phòng đang được sử dụng và không thể xóa"),
    
    // ROOM_TYPE_HAS_SHOWTIMES(1045, "Cannot modify or deactivate room type because it is associated with active showtimes"),
    ROOM_TYPE_HAS_SHOWTIMES(1045, "Không thể chỉnh sửa hoặc vô hiệu hóa loại phòng vì đang được liên kết với các suất chiếu đang hoạt động"),
    
    // ROOM_SIZE_EXCEEDED(1044, "Room size exceeds the maximum"),
    ROOM_SIZE_EXCEEDED(1044, "Kích thước phòng vượt quá giới hạn tối đa"),
    
    // ROOM_TYPE_INACTIVE(1046, "Cannot activate room because its room type is inactive"),
    ROOM_TYPE_INACTIVE(1046, "Không thể kích hoạt phòng vì loại phòng của nó không hoạt động"),

    // SHOW_TIME_NOT_FOUND(3001, "Show time not found"),
    SHOW_TIME_NOT_FOUND(3001, "Không tìm thấy suất chiếu"),
    
    // MOVIE_BANNER_NOT_FOUND(3001, "Movie banner not found"),
    MOVIE_BANNER_NOT_FOUND(3001, "Không tìm thấy banner phim"),

    // REDIS_KEY_NOT_FOUND(3002, "Redis key not found"),
    REDIS_KEY_NOT_FOUND(3002, "Không tìm thấy Redis key"),

    //ShowTime error
    // ROOM_NOT_FOUND(2003, "Room not found"),
    ROOM_NOT_FOUND(2003, "Không tìm thấy phòng"),
    
    // SUBTITLE_NOT_FOUND(2002, "Subtitle not found"),
    SUBTITLE_NOT_FOUND(2002, "Không tìm thấy phụ đề"),

    // ROOM_INACTIVE(2033, "Room is inactive"),
    ROOM_INACTIVE(2033, "Phòng không hoạt động"),
    
    // START_AFTER_END(2034, "startTime must be before endTime"),
    START_AFTER_END(2034, "Thời gian bắt đầu phải trước thời gian kết thúc"),
    
    // END_TOO_EARLY(2035, "endTime must be greater than startTime + movie.duration"),
    END_TOO_EARLY(2035, "Thời gian kết thúc phải lớn hơn thời gian bắt đầu + thời lượng phim"),
    
    // SHOWTIME_CONFLICT(2036, "Showtime conflicts with an existing show in the same room"),
    SHOWTIME_CONFLICT(2036, "Suất chiếu xung đột với suất chiếu hiện có trong cùng phòng"),

    // INTERNAL_ERROR(2037, "Unexpected error"),
    INTERNAL_ERROR(2037, "Lỗi hệ thống không mong muốn"),
    
    // SHOWTIME_NOT_FOUND(2038, "Showtime not found"),
    SHOWTIME_NOT_FOUND(2038, "Không tìm thấy suất chiếu"),
    
    // ALREADY_DELETED(2039, "Showtime isdeleted"),
    ALREADY_DELETED(2039, "Suất chiếu đã bị xóa"),
    
    // CANNOT_DELETE_STARTED(2040, "Can not deleted"),
    CANNOT_DELETE_STARTED(2040, "Không thể xóa"),
    
    // INVALID_ENDTIME(2024, "End time must be greater than start time + movie duration"),
    INVALID_ENDTIME(2024, "Thời gian kết thúc phải lớn hơn thời gian bắt đầu + thời lượng phim"),

    //Order
    // ORDER_NOT_FOUND(3101, "Order không tồn tại"),
    ORDER_NOT_FOUND(3101, "Đơn hàng không tồn tại"),
    
    // NOT_ORDER_OWNER(3102, "Bạn không có quyền xem đơn này"),
    NOT_ORDER_OWNER(3102, "Bạn không có quyền xem đơn này"),
    
    // ORDER_NOT_PAID(3103, "Đơn hàng chưa được thanh toán"),
    ORDER_NOT_PAID(3103, "Đơn hàng chưa được thanh toán"),
    
    // ORDER_CANCELED(3104, "Đơn hàng đã bị huỷ"),
    ORDER_CANCELED(3104, "Đơn hàng đã bị hủy"),
    
    // QR_EXPIRED(3105, "Mã QR đã hết hạn"),
    QR_EXPIRED(3105, "Mã QR đã hết hạn"),
    
    // QR_REGENERATE_LIMIT(3106, "Tạo lại QR quá giới hạn"),
    QR_REGENERATE_LIMIT(3106, "Tạo lại QR quá giới hạn"),
    
    // SHOWTIME_PASSED(3107, "Suất chiếu đã diễn ra"),
    SHOWTIME_PASSED(3107, "Suất chiếu đã diễn ra"),
    
    // ORDER_ALREADY_CHECKED_IN(3108, "Đơn hàng đã được check-in rồi"),
    ORDER_ALREADY_CHECKED_IN(3108, "Đơn hàng đã được check-in rồi"),
    
    // ORDER_NOT_COMPLETED(3109, "Đơn hàng phải hoàn thành thanh toán trước khi check-in"),
    ORDER_NOT_COMPLETED(3109, "Đơn hàng phải hoàn thành thanh toán trước khi check-in"),
    
    //Password
    // PASSWORD_TOO_WEAK(1020, "Mật khẩu quá yếu"),
    PASSWORD_TOO_WEAK(1020, "Mật khẩu quá yếu"),

    // ===== THEATER (mới, 4001–4010) =====
    // THEATER_NOT_FOUND(4001, "Theater details not found"),
    THEATER_NOT_FOUND(4001, "Không tìm thấy thông tin rạp"),
    
    // THEATER_INVALID_EMAIL(4002, "Invalid theater contact email"),
    THEATER_INVALID_EMAIL(4002, "Email liên hệ rạp không hợp lệ"),
    
    // THEATER_INVALID_PHONE(4003, "Invalid theater phone number"),
    THEATER_INVALID_PHONE(4003, "Số điện thoại rạp không hợp lệ"),
    
    // THEATER_INVALID_URL(4004, "Invalid theater URL"),
    THEATER_INVALID_URL(4004, "URL rạp không hợp lệ"),
    
    // THEATER_INVALID_OPEN_CLOSE(4005, "closeTime must be after openTime (or enable overnight)"),
    THEATER_INVALID_OPEN_CLOSE(4005, "Thời gian đóng cửa phải sau thời gian mở cửa (hoặc bật qua đêm)"),
    
    // THEATER_BANNER_UPLOAD_FAILED(4006, "Theater banner upload failed"),
    THEATER_BANNER_UPLOAD_FAILED(4006, "Tải banner rạp lên thất bại"),
    
    // THEATER_CONFLICT_VERSION(4007, "Theater details were updated by another user");
    THEATER_CONFLICT_VERSION(4007, "Thông tin rạp đã được cập nhật bởi người dùng khác");
    
    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
