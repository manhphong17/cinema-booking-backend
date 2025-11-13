package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.PaymentMethod;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {
    Optional<PaymentMethod> findByPaymentCodeIgnoreCase(String paymentCode);

    @Query("SELECT DISTINCT pm.methodName FROM PaymentMethod pm WHERE pm.isActive = true AND pm.methodName <> 'Tiền mặt'")
    List<String> findDistinctMethodNames();

    @Query("SELECT DISTINCT pm.methodName FROM PaymentMethod pm WHERE pm.isActive = true ")
    List<String> findDistinctAllMethodNames();


    //  Lấy danh sách ngân hàng thuộc methodName cụ thể
    List<PaymentMethod> findByMethodName(String methodName);

}
