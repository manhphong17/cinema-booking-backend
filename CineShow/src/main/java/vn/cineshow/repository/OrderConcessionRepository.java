package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.OrderConcession;

import java.util.List;

@Repository
public interface OrderConcessionRepository extends JpaRepository<OrderConcession, Long> {

    // Lấy danh sách OrderConcession theo order_id với thông tin concession
    @Query("SELECT oc FROM OrderConcession oc " +
           "JOIN FETCH oc.concession c " +
           "WHERE oc.order.id = :orderId")
    List<OrderConcession> findByOrderIdWithConcession(@Param("orderId") Long orderId);

    // Lấy thông tin concession với số lượng theo order_id
    @Query("SELECT c.name, oc.quantity, oc.unitPrice, c.urlImage " +
           "FROM OrderConcession oc " +
           "JOIN oc.concession c " +
           "WHERE oc.order.id = :orderId")
    List<Object[]> findConcessionDetailsByOrderId(@Param("orderId") Long orderId);
}
