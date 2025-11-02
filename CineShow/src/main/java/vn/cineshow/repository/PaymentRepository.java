package vn.cineshow.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import vn.cineshow.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
