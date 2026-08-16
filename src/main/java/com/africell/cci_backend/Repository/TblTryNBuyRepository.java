package com.africell.cci_backend.Repository;

import com.africell.cci_backend.Entity.TblTryNBuy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TblTryNBuyRepository extends JpaRepository<TblTryNBuy, Long> {
}
