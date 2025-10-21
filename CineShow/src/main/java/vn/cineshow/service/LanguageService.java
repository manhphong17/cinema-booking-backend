package vn.cineshow.service;


import vn.cineshow.dto.response.movie.LanguageResponse;

import java.util.List;

public interface LanguageService {
    List<LanguageResponse> getAll();
}
