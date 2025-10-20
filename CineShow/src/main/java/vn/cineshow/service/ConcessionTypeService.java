package vn.cineshow.service;

import vn.cineshow.dto.response.ConcessionTypeResponse;

import java.util.List;

public interface ConcessionTypeService {
    List<ConcessionTypeResponse> getAll();

    /**
     * Cập nhật trạng thái loại sản phẩm.
     * - Cho phép ACTIVE ↔ DELETED
     * - Cấm ACTIVE → DELETED nếu còn concessions đang FK đến
     */
    void updateStatus(Long id);

    void addConcessionType(String name);

}
