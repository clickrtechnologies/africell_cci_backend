package com.africell.cci_backend.Repository;
import com.africell.cci_backend.Entity.TblBillingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface TblBillingRepository extends JpaRepository<TblBillingId, Long> {

    Optional<TblBillingId> findByProductId(String productId);
}
