package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.ticketPrice.TicketPriceRequest;
import vn.cineshow.dto.response.ticketPrice.TicketPriceResponse;
import vn.cineshow.enums.DayType;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.RoomType;
import vn.cineshow.model.SeatType;
import vn.cineshow.model.TicketPrice;
import vn.cineshow.repository.RoomTypeRepository;
import vn.cineshow.repository.SeatTypeRepository;
import vn.cineshow.repository.TicketPriceRepository;
import vn.cineshow.service.TicketPriceService;

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
}
