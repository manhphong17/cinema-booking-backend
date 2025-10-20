package vn.cineshow.exception.ShowTimeException;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodShowTime {
    // ===== NEW: /createShowtime =====
    VALIDATION_FAILED(1101, HttpStatus.BAD_REQUEST, "Validation failed"),
    INVALID_JSON_OR_FORMAT(1102, HttpStatus.BAD_REQUEST, "Invalid request body or data format"),
    TYPE_MISMATCH(1103, HttpStatus.BAD_REQUEST, "Parameter type mismatch"),

    MOVIE_NOT_FOUND(1201, HttpStatus.NOT_FOUND, "Movie not found"),
    ROOM_NOT_FOUND(1202, HttpStatus.NOT_FOUND, "Room not found"),
    SUBTITLE_NOT_FOUND(1203, HttpStatus.NOT_FOUND, "Subtitle not found"),

    ROOM_INACTIVE(1301, HttpStatus.BAD_REQUEST, "Room is inactive"),
    START_AFTER_END(1302, HttpStatus.BAD_REQUEST, "startTime must be before endTime"),
    END_TOO_EARLY(1303, HttpStatus.BAD_REQUEST, "endTime must be greater than startTime + movie.duration"),
    SHOWTIME_CONFLICT(1304, HttpStatus.CONFLICT, "Showtime conflicts with an existing show in the same room"),

    INTERNAL_ERROR(1999, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error"),
    SHOWTIME_NOT_FOUND(1204, HttpStatus.NOT_FOUND, "Showtime not found");

    private final int code;
    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCodShowTime(int code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
