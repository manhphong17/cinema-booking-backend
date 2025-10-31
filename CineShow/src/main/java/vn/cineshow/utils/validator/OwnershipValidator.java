package vn.cineshow.utils.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.repository.OrderRepository;

@Component
@RequiredArgsConstructor
public class OwnershipValidator {
    private final OrderRepository orderRepository;

    public void mustOwnOrder(Long orderId, Long userId) {
        boolean ok = orderRepository.existsByIdAndUser_Id(orderId, userId);
        if (!ok) {
            // AppException chỉ nhận 1 đối số: ErrorCode
            throw new AppException(ErrorCode.NOT_ORDER_OWNER);
        }
    }
}
