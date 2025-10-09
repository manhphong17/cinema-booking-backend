package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
}
