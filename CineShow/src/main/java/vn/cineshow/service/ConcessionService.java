package vn.cineshow.service;


import org.springframework.data.domain.Page;
import vn.cineshow.dto.request.concession.ConcessionAddRequest;
import vn.cineshow.dto.request.concession.ConcessionUpdateRequest;
import vn.cineshow.dto.response.concession.ConcessionResponse;
import vn.cineshow.enums.ConcessionStatus;

public interface ConcessionService {

    Long addConcession(ConcessionAddRequest concessionAddRequest);


    Page<ConcessionResponse> getFilteredConcessions(
            String stockStatus,
            Long concessionTypeId,
            String concessionStatus,
            String keyword,
            int page,
            int size
    );

    ConcessionResponse updateConcession(Long id, ConcessionUpdateRequest request);

    ConcessionResponse addStock(Long id, int quantityToAdd);

    ConcessionResponse updateConcessionStatus(Long id, ConcessionStatus status);

    void deleteConcession(Long id);
}
