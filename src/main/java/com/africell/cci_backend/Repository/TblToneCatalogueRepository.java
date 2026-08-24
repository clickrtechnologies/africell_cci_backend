package com.africell.cci_backend.Repository;

import com.africell.cci_backend.Entity.TblToneCatalogue;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TblToneCatalogueRepository
        extends JpaRepository<TblToneCatalogue, Long> {

    // Existing lookup
    Optional<TblToneCatalogue> findByToneCode(String toneCode);


    // Search tones
    List<TblToneCatalogue>
    findByToneCodeContainingIgnoreCaseOrToneNameContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrArtistNameContainingIgnoreCase(
            String toneCode,
            String toneName,
            String category,
            String artistName,
            Sort sort
    );


    // Search artists
    @Query("""
            SELECT DISTINCT t.artistName
            FROM TblToneCatalogue t
            WHERE t.artistName IS NOT NULL
            AND LOWER(t.artistName)
                LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY t.artistName
            """)
    List<String> searchArtists(
            @Param("search") String search
    );


    // Search categories
    @Query("""
            SELECT DISTINCT t.category
            FROM TblToneCatalogue t
            WHERE t.category IS NOT NULL
            AND LOWER(t.category)
                LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY t.category
            """)
    List<String> searchCategories(
            @Param("search") String search
    );
}