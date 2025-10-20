package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.response.movie.LanguageResponse;
import vn.cineshow.repository.LanguageRepository;
import vn.cineshow.service.LanguageService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageServiceImpl implements LanguageService {
    private final LanguageRepository languageRepository;

    @Override
    public List<LanguageResponse> getAll() {
        return languageRepository.findAll()
                .stream().map(language -> LanguageResponse.builder()
                        .id(language.getId())
                        .name(language.getName())
                        .build())
                .toList();
    }
}
