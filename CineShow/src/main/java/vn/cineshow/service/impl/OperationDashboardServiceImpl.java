package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.response.dashboard.*;
import vn.cineshow.enums.MovieStatus;
import vn.cineshow.enums.OrderStatus;
import vn.cineshow.enums.RoomStatus;
import vn.cineshow.enums.TicketStatus;
import vn.cineshow.model.Movie;
import vn.cineshow.model.Order;
import vn.cineshow.model.Room;
import vn.cineshow.model.ShowTime;
import vn.cineshow.repository.MovieRepository;
import vn.cineshow.repository.OrderRepository;
import vn.cineshow.repository.RoomRepository;
import vn.cineshow.repository.ShowTimeRepository;
import vn.cineshow.repository.TicketRepository;
import vn.cineshow.service.OperationDashboardService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "OPERATION_DASHBOARD_SERVICE")
public class OperationDashboardServiceImpl implements OperationDashboardService {

    private final MovieRepository movieRepository;
    private final ShowTimeRepository showTimeRepository;
    private final RoomRepository roomRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public OperationDashboardStatsResponse getDashboardStats() {
        log.info("Getting dashboard statistics");

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime weekStart = todayStart.minusDays(7);
        LocalDateTime weekEnd = todayEnd.plusDays(7);

        // Get current year movies
        int currentYear = today.getYear();
        List<Movie> allMovies = movieRepository.findAll().stream()
                .filter(m -> !m.isDeleted())
                .filter(m -> {
                    if (m.getReleaseDate() == null) return true;
                    return m.getReleaseDate().getYear() == currentYear;
                })
                .toList();

        log.info("Movies found for year {}: total={}, playing={}, upcoming={}, ended={}", 
                currentYear, allMovies.size(),
                allMovies.stream().filter(m -> MovieStatus.PLAYING.equals(m.getStatus())).count(),
                allMovies.stream().filter(m -> MovieStatus.UPCOMING.equals(m.getStatus())).count(),
                allMovies.stream().filter(m -> MovieStatus.ENDED.equals(m.getStatus())).count());

        // Movie stats
        MovieStatsResponse movieStats = calculateMovieStats(allMovies);
        log.info("Calculated movie stats: total={}, playing={}, upcoming={}, ended={}", 
                movieStats.getTotal(), movieStats.getPlaying(), 
                movieStats.getUpcoming(), movieStats.getEnded());

        // Showtime stats
        ShowtimeStatsResponse showtimeStats = calculateShowtimeStats(todayStart, todayEnd, weekEnd, now);

        // Room stats
        RoomStatsResponse roomStats = calculateRoomStats();

        // Today's showtimes
        List<TodayShowtimeResponse> todayShowtimes = getTodayShowtimes(todayStart, todayEnd, now);

        // Playing movies
        List<PlayingMovieResponse> playingMovies = getPlayingMovies(allMovies);

        // Hot movies this week
        List<HotMovieResponse> hotMovies = getHotMovies(weekStart, now, allMovies);

        // Alerts and insights
        OperationDashboardInsightsResponse insights = calculateInsights(allMovies, todayStart, todayEnd, now);
        List<OperationDashboardAlertResponse> alerts = generateAlerts(insights);

        log.info("Dashboard statistics retrieved successfully");
        log.info("Final movieStats in response - total: {}, playing: {}, upcoming: {}, ended: {}", 
                movieStats.getTotal(), movieStats.getPlaying(), 
                movieStats.getUpcoming(), movieStats.getEnded());

        OperationDashboardStatsResponse response = OperationDashboardStatsResponse.builder()
                .movieStats(movieStats)
                .showtimeStats(showtimeStats)
                .roomStats(roomStats)
                .todayShowtimes(todayShowtimes)
                .playingMovies(playingMovies)
                .hotMovies(hotMovies)
                .alerts(alerts)
                .insights(insights)
                .build();

        log.info("OperationDashboardStatsResponse built - movieStats.total: {}", 
                response.getMovieStats() != null ? response.getMovieStats().getTotal() : "null");

        return response;
    }

    private MovieStatsResponse calculateMovieStats(List<Movie> movies) {
        long total = movies.size();
        long playing = movies.stream().filter(m -> MovieStatus.PLAYING.equals(m.getStatus())).count();
        long upcoming = movies.stream().filter(m -> MovieStatus.UPCOMING.equals(m.getStatus())).count();
        long ended = movies.stream().filter(m -> MovieStatus.ENDED.equals(m.getStatus())).count();

        log.info("Movie stats calculation - total: {}, playing: {}, upcoming: {}, ended: {}", 
                total, playing, upcoming, ended);

        MovieStatsResponse stats = MovieStatsResponse.builder()
                .total(total)
                .playing(playing)
                .upcoming(upcoming)
                .ended(ended)
                .build();

        log.info("MovieStatsResponse built - total: {}, playing: {}, upcoming: {}, ended: {}", 
                stats.getTotal(), stats.getPlaying(), stats.getUpcoming(), stats.getEnded());

        return stats;
    }

    private ShowtimeStatsResponse calculateShowtimeStats(LocalDateTime todayStart, LocalDateTime todayEnd,
                                                         LocalDateTime weekEnd, LocalDateTime now) {
        List<ShowTime> allShowtimes = showTimeRepository.findAllBy().stream()
                .filter(st -> !st.getIsDeleted())
                .toList();

        long today = allShowtimes.stream()
                .filter(st -> {
                    LocalDateTime startTime = st.getStartTime();
                    return !startTime.isBefore(todayStart) && startTime.isBefore(todayEnd);
                })
                .count();

        long thisWeek = allShowtimes.stream()
                .filter(st -> {
                    LocalDateTime startTime = st.getStartTime();
                    return !startTime.isBefore(todayStart) && !startTime.isAfter(weekEnd);
                })
                .count();

        long upcoming = allShowtimes.stream()
                .filter(st -> st.getStartTime().isAfter(now))
                .count();

        long total = allShowtimes.size();

        return ShowtimeStatsResponse.builder()
                .today(today)
                .thisWeek(thisWeek)
                .upcoming(upcoming)
                .total(total)
                .build();
    }

    private RoomStatsResponse calculateRoomStats() {
        List<Room> allRooms = roomRepository.findAll();
        long total = allRooms.size();
        long active = allRooms.stream().filter(r -> RoomStatus.ACTIVE.equals(r.getStatus())).count();
        long inactive = allRooms.stream().filter(r -> !RoomStatus.ACTIVE.equals(r.getStatus())).count();

        return RoomStatsResponse.builder()
                .total(total)
                .active(active)
                .inactive(inactive)
                .build();
    }

    private List<TodayShowtimeResponse> getTodayShowtimes(LocalDateTime todayStart, LocalDateTime todayEnd,
                                                           LocalDateTime now) {
        List<ShowTime> todayShowtimes = showTimeRepository.findAllBy().stream()
                .filter(st -> !st.getIsDeleted())
                .filter(st -> {
                    LocalDateTime startTime = st.getStartTime();
                    return !startTime.isBefore(todayStart) && startTime.isBefore(todayEnd);
                })
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .limit(5)
                .toList();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        return todayShowtimes.stream()
                .map(st -> {
                    LocalDateTime startTime = st.getStartTime();
                    LocalDateTime endTime = st.getEndTime();

                    String status;
                    if (now.isBefore(startTime)) {
                        status = "upcoming";
                    } else if (now.isAfter(endTime)) {
                        status = "ended";
                    } else {
                        status = "playing";
                    }

                    // Calculate occupancy rate
                    Long soldTickets = ticketRepository.countByShowTime_IdAndStatus(st.getId(), TicketStatus.BOOKED);
                    Integer capacity = st.getRoom() != null ? st.getRoom().getCapacity() : null;
                    Long totalCapacity = capacity != null ? (long) capacity : 0L;
                    Double occupancyRate = totalCapacity > 0 ? (soldTickets.doubleValue() / totalCapacity.doubleValue()) * 100 : 0.0;

                    // Null safety checks
                    String movieName = (st.getMovie() != null && st.getMovie().getName() != null) 
                            ? st.getMovie().getName() : "N/A";
                    String roomName = (st.getRoom() != null && st.getRoom().getName() != null) 
                            ? st.getRoom().getName() : "N/A";

                    return TodayShowtimeResponse.builder()
                            .id(st.getId())
                            .movieName(movieName)
                            .roomName(roomName)
                            .startTime(startTime.format(timeFormatter))
                            .endTime(endTime.format(timeFormatter))
                            .status(status)
                            .occupancyRate(occupancyRate)
                            .soldTickets(soldTickets)
                            .totalCapacity(totalCapacity)
                            .build();
                })
                .toList();
    }

    private List<PlayingMovieResponse> getPlayingMovies(List<Movie> movies) {
        return movies.stream()
                .filter(m -> MovieStatus.PLAYING.equals(m.getStatus()))
                .limit(5)
                .map(m -> {
                    long showtimeCount = showTimeRepository.findAllBy().stream()
                            .filter(st -> !st.getIsDeleted())
                            .filter(st -> st.getMovie().getId().equals(m.getId()))
                            .count();

                    return PlayingMovieResponse.builder()
                            .id(m.getId())
                            .name(m.getName())
                            .posterUrl(m.getPosterUrl())
                            .showtimeCount(showtimeCount)
                            .status(m.getStatus().name())
                            .build();
                })
                .toList();
    }

    private List<HotMovieResponse> getHotMovies(LocalDateTime weekStart, LocalDateTime now, List<Movie> movies) {
        // Get showtimes from last 7 days
        List<ShowTime> weekShowtimes = showTimeRepository.findAllBy().stream()
                .filter(st -> !st.getIsDeleted())
                .filter(st -> {
                    if (st.getStartTime() == null) return false;
                    LocalDateTime startTime = st.getStartTime();
                    return !startTime.isBefore(weekStart) && !startTime.isAfter(now);
                })
                .toList();

        log.info("Week showtimes found: {}", weekShowtimes.size());

        // Get bookings for these showtimes
        List<Order> allOrders = orderRepository.findAll();
        List<Order> completedOrders = allOrders.stream()
                .filter(o -> {
                    if (o.getCreatedAt() == null) return false;
                    LocalDateTime createdAt = o.getCreatedAt();
                    return createdAt.isAfter(weekStart) && createdAt.isBefore(now);
                })
                .filter(o -> OrderStatus.COMPLETED.equals(o.getOrderStatus()))
                .toList();

        log.info("Completed orders in last 7 days: {}", completedOrders.size());

        // Count bookings per movie
        Map<Long, Long> movieBookingCounts = completedOrders.stream()
                .flatMap(order -> order.getTickets() != null ? order.getTickets().stream() : java.util.stream.Stream.empty())
                .filter(ticket -> ticket.getShowTime() != null 
                        && ticket.getShowTime().getMovie() != null 
                        && ticket.getShowTime().getMovie().getId() != null)
                .map(ticket -> ticket.getShowTime().getMovie().getId())
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

        // Count showtimes per movie (tất cả showtimes, không chỉ 7 ngày qua)
        // Hot movies: phim có nhiều showtime nhất trong tuần qua HOẶC có nhiều bookings nhất
        Map<Long, Long> movieShowtimeCounts = weekShowtimes.stream()
                .filter(st -> st.getMovie() != null && st.getMovie().getId() != null)
                .collect(Collectors.groupingBy(st -> st.getMovie().getId(), Collectors.counting()));

        log.info("Movies with bookings: {}, Movies with showtimes: {}", 
                movieBookingCounts.size(), movieShowtimeCounts.size());

        // Build hot movies list: ưu tiên phim có bookings, sau đó phim có nhiều showtimes
        List<HotMovieResponse> hotMoviesList = movies.stream()
                .filter(m -> {
                    // Bao gồm phim có bookings HOẶC có showtimes trong tuần
                    return movieBookingCounts.containsKey(m.getId()) || movieShowtimeCounts.containsKey(m.getId());
                })
                .map(m -> {
                    Long bookingCount = movieBookingCounts.getOrDefault(m.getId(), 0L);
                    Long showtimeCount = movieShowtimeCounts.getOrDefault(m.getId(), 0L);

                    return HotMovieResponse.builder()
                            .id(m.getId())
                            .name(m.getName())
                            .posterUrl(m.getPosterUrl())
                            .bookingCount(bookingCount)
                            .showtimeCount(showtimeCount)
                            .build();
                })
                .sorted((a, b) -> {
                    // Sort: ưu tiên bookingCount, nếu bằng nhau thì ưu tiên showtimeCount
                    int bookingCompare = Long.compare(b.getBookingCount(), a.getBookingCount());
                    if (bookingCompare != 0) return bookingCompare;
                    return Long.compare(b.getShowtimeCount(), a.getShowtimeCount());
                })
                .limit(5)
                .toList();

        log.info("Hot movies found: {}", hotMoviesList.size());
        return hotMoviesList;
    }

    private OperationDashboardInsightsResponse calculateInsights(List<Movie> movies, LocalDateTime todayStart,
                                                          LocalDateTime todayEnd, LocalDateTime now) {
        // Movies ending soon: Phim đang chiếu (PLAYING) và chỉ còn suất chiếu trong 3 ngày gần nhất
        // Logic: Phim được coi là "sắp kết thúc" nếu:
        // - Status = PLAYING
        // - Có ít nhất 1 showtime trong 3 ngày tới
        // - Không còn showtime nào sau 3 ngày tính từ hiện tại
        LocalDateTime threeDaysLater = now.plusDays(3);
        LocalDateTime threeDaysLaterEnd = threeDaysLater.with(LocalTime.MAX);
        
        long upcomingMoviesEnding = movies.stream()
                .filter(m -> MovieStatus.PLAYING.equals(m.getStatus()))
                .filter(m -> {
                    try {
                        // Lấy tất cả showtimes của phim này chưa bị xóa
                        List<ShowTime> movieShowtimes = showTimeRepository.findAllBy().stream()
                                .filter(st -> !st.getIsDeleted())
                                .filter(st -> st.getMovie() != null && st.getMovie().getId().equals(m.getId()))
                                .filter(st -> st.getStartTime() != null)
                                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                                .toList();
                        
                        if (movieShowtimes.isEmpty()) {
                            return false; // Không có showtime nào thì không coi là sắp kết thúc
                        }
                        
                        // Kiểm tra xem có showtime nào trong 3 ngày tới không
                        boolean hasShowtimeInNext3Days = movieShowtimes.stream()
                                .anyMatch(st -> {
                                    LocalDateTime startTime = st.getStartTime();
                                    return startTime.isAfter(now) && !startTime.isAfter(threeDaysLaterEnd);
                                });
                        
                        // Kiểm tra xem có showtime nào sau 3 ngày không
                        boolean hasShowtimeAfter3Days = movieShowtimes.stream()
                                .anyMatch(st -> st.getStartTime().isAfter(threeDaysLaterEnd));
                        
                        // Phim sắp kết thúc nếu: có showtime trong 3 ngày tới VÀ không còn showtime sau 3 ngày
                        return hasShowtimeInNext3Days && !hasShowtimeAfter3Days;
                    } catch (Exception e) {
                        log.error("Error checking if movie {} is ending soon: {}", m.getId(), e.getMessage());
                        return false;
                    }
                })
                .count();
        
        log.info("Movies ending soon (within 3 days): {}", upcomingMoviesEnding);

        // Rooms without showtime today
        List<Room> activeRooms = roomRepository.findAll().stream()
                .filter(r -> RoomStatus.ACTIVE.equals(r.getStatus()))
                .toList();

        Set<Long> roomsWithShowtime = showTimeRepository.findAllBy().stream()
                .filter(st -> !st.getIsDeleted())
                .filter(st -> st.getStartTime() != null)
                .filter(st -> {
                    LocalDateTime startTime = st.getStartTime();
                    return !startTime.isBefore(todayStart) && startTime.isBefore(todayEnd);
                })
                .filter(st -> st.getRoom() != null && st.getRoom().getId() != null)
                .map(st -> st.getRoom().getId())
                .collect(Collectors.toSet());

        long roomsWithoutShowtime = activeRooms.stream()
                .filter(r -> !roomsWithShowtime.contains(r.getId()))
                .count();

        // Upcoming movies without showtime
        Set<Long> moviesWithShowtime = showTimeRepository.findAllBy().stream()
                .filter(st -> !st.getIsDeleted())
                .filter(st -> st.getMovie() != null && st.getMovie().getId() != null)
                .map(st -> st.getMovie().getId())
                .collect(Collectors.toSet());

        long upcomingMoviesWithoutShowtime = movies.stream()
                .filter(m -> MovieStatus.UPCOMING.equals(m.getStatus()))
                .filter(m -> !moviesWithShowtime.contains(m.getId()))
                .count();

        return OperationDashboardInsightsResponse.builder()
                .upcomingMoviesEnding(upcomingMoviesEnding)
                .roomsWithoutShowtime(roomsWithoutShowtime)
                .upcomingMoviesWithoutShowtime(upcomingMoviesWithoutShowtime)
                .build();
    }

    private List<OperationDashboardAlertResponse> generateAlerts(OperationDashboardInsightsResponse insights) {
        List<OperationDashboardAlertResponse> alerts = new ArrayList<>();

        if (insights.getUpcomingMoviesEnding() > 0) {
            alerts.add(OperationDashboardAlertResponse.builder()
                    .type("warning")
                    .message(insights.getUpcomingMoviesEnding() + " phim sắp kết thúc, cần có phim thay thế")
                    .build());
        }

        if (insights.getRoomsWithoutShowtime() > 0) {
            alerts.add(OperationDashboardAlertResponse.builder()
                    .type("info")
                    .message(insights.getRoomsWithoutShowtime() + " phòng hoạt động chưa có lịch chiếu hôm nay")
                    .build());
        }

        if (insights.getUpcomingMoviesWithoutShowtime() > 0) {
            alerts.add(OperationDashboardAlertResponse.builder()
                    .type("warning")
                    .message(insights.getUpcomingMoviesWithoutShowtime() + " phim sắp chiếu chưa có lịch chiếu")
                    .build());
        }

        return alerts;
    }
}

