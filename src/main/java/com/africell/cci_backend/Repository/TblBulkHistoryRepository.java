package com.africell.cci_backend.Repository;

import com.africell.cci_backend.Entity.TblBulkHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TblBulkHistoryRepository
        extends JpaRepository<TblBulkHistory, Long> {

    Optional<TblBulkHistory> findByPreviewId(String previewId);
}