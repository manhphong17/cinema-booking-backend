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
    CONCESSION_ALREADY_DELETED(1018, "Concession has been deleted"),
    ACCOUNT_NOT_FOUND(1019, "Account not found"),
    CONCESSION_TYPE_NOT_FOUND(1025, "Concession type not found"),
    CONCESSION_TYPE_IN_USE(1026, "Concession has been in use"),
    CONCESSION_TYPE_EXISTED(1027, "Concession already existed"),
    HOLIDAY_EXISTED(1028, "Holiday has been added"),
    HOLIDAY_NOT_FOUND(1029, "Holiday not found"),
    SEAT_TYPE_NOT_FOUND(1030, "Seat type not found"),
    ROOM_TYPE_NOT_FOUND(1031, "Room type not found"),

    SHOW_TIME_NOT_FOUND(3001, "Show time not found"),
    MOVIE_BANNER_NOT_FOUND(3002, "Movie Banner not found");


    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}