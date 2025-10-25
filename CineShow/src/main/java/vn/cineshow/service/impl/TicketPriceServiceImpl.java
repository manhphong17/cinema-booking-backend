package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.ticketPrice.TicketPriceRequest;
import vn.cineshow.dto.response.ticketPrice.TicketPriceResponse;
import vn.cineshow.enums.DayType;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.*;
import vn.cineshow.repository.*;
import vn.cineshow.service.HolidayService;
import vn.cineshow.service.TicketPriceService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketPriceServiceImpl implements TicketPriceService {

    private final TicketPriceRepository ticketPriceRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final SeatRepository seatRepository;
    private final ShowTimeRepository showTimeRepository;
    private final HolidayService holidayService;

    @Transactional
    @Override
    public TicketPrice createOrUpdatePrice(TicketPriceRequest req) {
        SeatType seatType = seatTypeRepository.findById(req.getSeatTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND));
        RoomType roomType = roomTypeRepository.findById(req.getRoomTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        TicketPrice price = ticketPriceRepository
                .findBySeatTypeIdAndRoomTypeIdAndDayType(req.getSeatTypeId(), req.getRoomTypeId(), req.getDayType())
                .orElseGet(() -> TicketPrice.builder()
                        .seatType(seatType)
                        .roomType(roomType)
                        .dayType(req.getDayType())
                        .build());

        price.setPrice(req.getPrice());
        return ticketPriceRepository.save(price);
    }

    @Override
    public List<TicketPriceResponse> getAllPrices() {
        List<TicketPrice> prices = ticketPriceRepository.findAllWithRelations();

        // Gom nhóm theo roomTypeId + seatTypeId → tách normal vs holiday
        Map<String, TicketPriceResponse> map = new HashMap<>();

        for (TicketPrice tp : prices) {
            String key = tp.getRoomType().getId() + "_" + tp.getSeatType().getId();
            TicketPriceResponse dto = map.getOrDefault(key,
                    TicketPriceResponse.builder()
                            .roomTypeId(tp.getRoomType().getId())
                            .seatTypeId(tp.getSeatType().getId())
                            .normalDayPrice(null)
                            .weekendPrice(null)
                            .build());

            if (tp.getDayType() == DayType.NORMAL)
                dto.setNormalDayPrice(tp.getPrice());
            else
                dto.setWeekendPrice(tp.getPrice());

            map.put(key, dto);
        }

        return new ArrayList<>(map.values());
    }


    @Transactional
    @Override
    public Double calculatePrice(Long seatId, Long showTimeId) {
        // 1. Lấy seatType
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND));
        Long seatTypeId = seat.getSeatType().getId();

        // 2. Lấy roomType + showDate
        ShowTime showTime = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new RuntimeException("ShowTime not found"));
        Long roomTypeId = showTime.getRoom().getRoomType().getId();
        LocalDate showDate = LocalDate.from(showTime.getStartTime());

        // 3. Tính dayType
        DayType dayType = getDayType(showDate);

        // 4. Truy vấn giá
        return ticketPriceRepository.findPrice(roomTypeId, seatTypeId, dayType)
                .orElseThrow(() -> new RuntimeException("Ticket price not found"));
    }

    private DayType getDayType(LocalDate date) {
        if (holidayService.isHoliday(date)) return DayType.HOLIDAY;

        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY)
            return DayType.HOLIDAY;

        return DayType.NORMAL;
    }
}

