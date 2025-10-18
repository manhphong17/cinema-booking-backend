package vn.cineshow.dto.response.showtime;

import com.fasterxml.jackson.annotation.JsonProperty;
import vn.cineshow.model.ShowTime;

import java.time.format.DateTimeFormatter;


public record ShowTimeListDTO(
        Long showtimeId,     // <-- thêm
        Long movieId,
        String movieName,
        @JsonProperty("poster_url") String movieBannerUrl,  // <-- thêm

        Long roomTypeId,
        String roomTypeName,
        Long roomId,
        String roomName,
        Long subtitleId,     // đã thêm
        String subtitleName, // đã thêm
        String startTime,
        String endTime
) {
    public static ShowTimeListDTO from(ShowTime st) {
        var room = st.getRoom();
        var roomType = room != null ? room.getRoomType() : null;
        var movie = st.getMovie();
        var subtitle = st.getSubtitle();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String startTimeStr = st.getStartTime() != null ? st.getStartTime().format(formatter) : null;
        String endTimeStr   = st.getEndTime()   != null ? st.getEndTime().format(formatter)   : null;

        return new ShowTimeListDTO(
                st.getId(),
                movie != null ? movie.getId() : null,
                movie != null ? movie.getName() : null,
                movie != null ? movie.getPosterUrl() : null,      // <-- map thêm
                roomType != null ? roomType.getId() : null,
                roomType != null ? roomType.getName() : null,
                room != null ? room.getId() : null,
                room != null ? room.getName() : null,
                subtitle != null ? subtitle.getId() : null,
                subtitle != null ? subtitle.getName() : null,
                startTimeStr,
                endTimeStr
        );
    }
}
