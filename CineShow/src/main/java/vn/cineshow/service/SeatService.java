package vn.cineshow.service;

import vn.cineshow.dto.request.seat.BulkBlockRequest;
import vn.cineshow.dto.request.seat.BulkTypeRequest;
import vn.cineshow.dto.request.seat.SeatInitRequest;
import vn.cineshow.dto.request.seat.SeatMatrixRequest;
import vn.cineshow.dto.response.seat.SeatMatrixResponse;

import java.util.Map;

public interface SeatService {

    int initSeats(Long roomId, SeatInitRequest request);

    SeatMatrixResponse getSeatMatrix(Long roomId);

    Map<String, Integer> saveSeatMatrix(Long roomId, SeatMatrixRequest request);

    int bulkUpdateSeatType(Long roomId, BulkTypeRequest request);

    int bulkBlockSeats(Long roomId, BulkBlockRequest request);
}
