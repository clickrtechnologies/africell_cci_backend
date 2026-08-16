package com.africell.cci_backend.Repository;

import com.africell.cci_backend.Entity.TblSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface TblSubscriptionRepository extends JpaRepository<TblSubscription, Long> {
}
