package vn.cineshow.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.cineshow.model.SubTitle;

import java.util.List;

public interface SubTitleRepository  extends JpaRepository<SubTitle, Long> {
    List<SubTitle> findSubTitleBy(Long roomTypeId, Sort sort);

}
