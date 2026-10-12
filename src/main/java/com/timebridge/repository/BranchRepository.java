package com.timebridge.repository;

import com.timebridge.entity.Branch;
import com.timebridge.entity.Province;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByProvinceOrderByNameAsc(Province province);

    long countByProvince(Province province);

    Optional<Branch> findByNameIgnoreCase(String name);

    // Locks the branch row so two bookings for the same branch are processed one at a time.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Branch b where b.id = :id")
    Optional<Branch> findByIdForUpdate(@Param("id") Long id);
}
