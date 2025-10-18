package vn.cineshow.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import vn.cineshow.enums.ConcessionTypeStatus;
import vn.cineshow.model.ConcessionType;

import java.util.List;
import java.util.Optional;

public interface ConcessionTypeRepository extends JpaRepository<ConcessionType, Long> {
    Optional<ConcessionType> findByNameIgnoreCase(String concessionType);

    ConcessionType findByIdAndStatusNot(Long id, ConcessionTypeStatus status);

    List<ConcessionType> findAllByStatus(ConcessionTypeStatus status);

}