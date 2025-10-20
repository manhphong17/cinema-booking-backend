package vn.cineshow.dto.response;


import lombok.Builder;

@Builder
public record ConcessionTypeResponse(
        Long id,
        String name,
        String status) {
}