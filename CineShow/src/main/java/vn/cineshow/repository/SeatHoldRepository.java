package vn.cineshow.repository;

import org.springframework.data.repository.CrudRepository;
import vn.cineshow.dto.response.booking.SeatHold;

public interface SeatHoldRepository extends CrudRepository<SeatHold, String> {
}
