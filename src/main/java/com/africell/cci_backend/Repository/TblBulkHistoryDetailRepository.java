package com.africell.cci_backend.Repository;

import com.africell.cci_backend.Entity.TblBulkHistoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TblBulkHistoryDetailRepository
        extends JpaRepository<TblBulkHistoryDetail, Long> {

    List<TblBulkHistoryDetail> findByPreviewId(String previewId);
}