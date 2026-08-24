package com.africell.cci_backend.Service;

import com.africell.cci_backend.Entity.TblToneCatalogue;
import com.africell.cci_backend.Repository.TblToneCatalogueRepository;
import com.africell.cci_backend.dto.response.ToneCatalogueResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToneCatalogueService {

    private final TblToneCatalogueRepository toneCatalogueRepository;

    public ToneCatalogueService(
            TblToneCatalogueRepository toneCatalogueRepository) {

        this.toneCatalogueRepository = toneCatalogueRepository;
    }

    //Get tones with search and sorting
    public List<ToneCatalogueResponse> getTones(
            String search,
            String sortBy,
            String sortDir) {

        Sort sort = createSort(sortBy, sortDir);
        List<TblToneCatalogue> tones;

        if (search == null || search.isBlank()) {

            // Get all tones
            tones = toneCatalogueRepository.findAll(sort);

        } else {
            // Search tones
            String keyword = search.trim();

            tones = toneCatalogueRepository
                    .findByToneCodeContainingIgnoreCaseOrToneNameContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrArtistNameContainingIgnoreCase(
                            keyword,
                            keyword,
                            keyword,
                            keyword,
                            sort
                    );
        }
        return tones.stream()
                .map(this::convertToResponse)
                .toList();
    }
    //Get artist
    public List<String> getArtists(String search) {
        String keyword = search == null ? "" : search.trim();
        return toneCatalogueRepository.searchArtists(keyword);
    }
    // Get categories
    public List<String> getCategories(String search) {
        String keyword = search == null ? "" : search.trim();
        return toneCatalogueRepository.searchCategories(keyword);
    }
    // Create sorting
    private Sort createSort(String sortBy, String sortDir) {
        String field = "toneName";
        if (sortBy != null && !sortBy.isBlank()) {
            switch (sortBy) {
                case "toneCode":
                    field = "toneCode";
                    break;
                case "toneName":
                    field = "toneName";
                    break;
                case "category":
                    field = "category";
                    break;
                case "artistName":
                     field = "artistName";
                     break;
                case "updateTime":
                    field = "updateTime";
                    break;
            }
        }
        if ("desc".equalsIgnoreCase(sortDir)) {
            return Sort.by(Sort.Direction.DESC, field);
        } else {
            return Sort.by(Sort.Direction.ASC, field);
        }
    }
    // convert Entity to Response
    private ToneCatalogueResponse convertToResponse(
            TblToneCatalogue tone) {
        ToneCatalogueResponse response = new ToneCatalogueResponse();

        response.setToneCode(tone.getToneCode());
        response.setToneUrl(tone.getToneUrl());
        response.setToneName(tone.getToneName());
        response.setCategory(tone.getCategory());
        response.setArtistName(tone.getArtistName());
        response.setUpdateTime(tone.getUpdateTime());
        return response;
    }

}