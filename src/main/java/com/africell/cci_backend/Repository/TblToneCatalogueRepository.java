package com.africell.cci_backend.Repository;

import com.africell.cci_backend.Entity.TblToneCatalogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface TblToneCatalogueRepository extends JpaRepository<TblToneCatalogue, Long> {
    Optional<TblToneCatalogue> findByToneCode(String toneCode);
}
