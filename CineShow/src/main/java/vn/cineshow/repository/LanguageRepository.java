package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.Language;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
}
