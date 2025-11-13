package vn.cineshow.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AccountListResponseDTO {
    private List<AccountItemDTO> accounts;
    private long totalAccounts;
    
    // Pagination info
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
