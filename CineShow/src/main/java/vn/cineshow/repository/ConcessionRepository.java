package vn.cineshow.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.cineshow.enums.ConcessionStatus;
import vn.cineshow.enums.StockStatus;
import vn.cineshow.model.Concession;

import java.util.List;

public interface ConcessionRepository extends JpaRepository<Concession, Long> {

    @Query("""
                SELECT c
                FROM Concession c
                WHERE c.concessionStatus <> vn.cineshow.enums.ConcessionStatus.DELETED
                  AND (:stockStatus IS NULL OR c.stockStatus = :stockStatus)
                  AND (:concessionTypeId IS NULL OR c.concessionType.id = :concessionTypeId)
                  AND (:concessionStatus IS NULL OR c.concessionStatus = :concessionStatus)
                  AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Concession> findFilteredConcessions(
            @Param("stockStatus") StockStatus stockStatus,
            @Param("concessionTypeId") Long concessionTypeId,
            @Param("concessionStatus") ConcessionStatus concessionStatus,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByConcessionType_Id(Long concessionTypeId);

    @Query("SELECT c FROM Concession c WHERE c.id IN :ids AND c.concessionStatus = vn.cineshow.enums.ConcessionStatus.ACTIVE")
    List<Concession> findActiveConcessionsByIds(@Param("ids") List<Long> ids);
}


