package vn.cineshow.dto.response;

public record IdNameDTO(Long id, String name) {
    public static IdNameDTO of(Long id, String name) { return new IdNameDTO(id, name); }
}
