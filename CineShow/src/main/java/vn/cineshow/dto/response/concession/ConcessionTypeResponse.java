package vn.cineshow.dto.response.concession;


import lombok.Builder;

@Builder
public record ConcessionTypeResponse(
        Long id,
        String name,
        String status) {
}