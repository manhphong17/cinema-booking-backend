package vn.cineshow.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.OrderConcession;

@Repository
public interface OrderConcessionRepository extends JpaRepository<OrderConcession, Long> {

}
