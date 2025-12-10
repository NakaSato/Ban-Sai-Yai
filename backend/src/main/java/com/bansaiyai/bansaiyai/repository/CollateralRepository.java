package com.bansaiyai.bansaiyai.repository;

import com.bansaiyai.bansaiyai.entity.Collateral;
import com.bansaiyai.bansaiyai.entity.enums.CollateralType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Collateral entity
 */
@Repository
public interface CollateralRepository extends JpaRepository<Collateral, Long> {

    /**
     * Find all collateral for a specific loan
     */
    List<Collateral> findByLoanIdOrderByCreatedAtDesc(Long loanId);

    /**
     * Find collateral by collateral number
     */
    Optional<Collateral> findByCollateralNumber(String collateralNumber);

    /**
     * Find all verified collateral for a loan
     */
    @Query("SELECT c FROM Collateral c WHERE c.loan.id = :loanId AND c.isVerified = true")
    List<Collateral> findVerifiedByLoanId(@Param("loanId") Long loanId);

    /**
     * Find all available (not released) collateral for a loan
     */
    @Query("SELECT c FROM Collateral c WHERE c.loan.id = :loanId AND c.isReleased = false")
    List<Collateral> findAvailableByLoanId(@Param("loanId") Long loanId);

    /**
     * Find collateral by type
     */
    List<Collateral> findByCollateralType(CollateralType collateralType);

    /**
     * Find collateral with documents (has documentPath)
     */
    @Query("SELECT c FROM Collateral c WHERE c.loan.id = :loanId AND c.documentPath IS NOT NULL")
    List<Collateral> findWithDocumentsByLoanId(@Param("loanId") Long loanId);

    /**
     * Count collateral for a loan
     */
    long countByLoanId(Long loanId);

    /**
     * Check if collateral exists by document number
     */
    boolean existsByDocumentNumber(String documentNumber);

    /**
     * Find collateral by UUID (for API endpoints)
     */
    Optional<Collateral> findByUuid(java.util.UUID uuid);

    /**
     * Delete collateral by UUID
     */
    void deleteByUuid(java.util.UUID uuid);

    /**
     * Check if collateral exists by UUID
     */
    boolean existsByUuid(java.util.UUID uuid);
}
