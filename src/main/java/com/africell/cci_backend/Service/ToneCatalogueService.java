package com.africell.cci_backend.Service;

import com.africell.cci_backend.Entity.TblToneCatalogue;
import com.africell.cci_backend.Repository.TblToneCatalogueRepository;
import com.africell.cci_backend.dto.response.ToneCatalogueResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToneCatalogueService {

    private final TblToneCatalogueRepository toneCatalogueRepository;

    public ToneCatalogueService(
            TblToneCatalogueRepository toneCatalogueRepository) {

        this.toneCatalogueRepository = toneCatalogueRepository;
    }

    public List<TblToneCatalogue> getTones(String search) {

        if (search == null || search.isBlank()) {

            return toneCatalogueRepository.findAll();

        } else {

            String keyword = search.trim();

            return toneCatalogueRepository
                    .findByToneCodeContainingIgnoreCaseOrToneNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                            keyword,
                            keyword,
                            keyword
                    );
        }
    }
}