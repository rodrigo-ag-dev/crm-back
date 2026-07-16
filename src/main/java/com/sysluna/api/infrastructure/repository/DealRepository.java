package com.sysluna.api.infrastructure.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sysluna.api.domain.dto.DealResponse;
import com.sysluna.api.domain.dto.DealStaleDTO;
import com.sysluna.api.domain.model.Deal;

public interface DealRepository extends JpaRepository<Deal, String> {
  List<Deal> findByTitleAndOwnerId(String title, String ownerId);

  Optional<Deal> findByIdAndOwnerId(String id, String ownerId);

  long countByOwnerId(String ownerId);

  @Query("""
          SELECT new com.sysluna.api.domain.dto.DealResponse(
              d.id, d.companyId, d.contactId, d.ownerId, d.stageId, d.title,
              d.description, d.lost, d.won, d.probability, d.amount, d.closeDateExpected, d.createdAt,
              s.name
          )
          FROM Deal d
          LEFT JOIN Stage s ON d.stageId = s.id
          WHERE d.ownerId = :ownerId
            AND (:title IS NULL
                 OR LOWER(d.title) LIKE CONCAT('%', LOWER(CAST(:title AS string)), '%'))
            AND (:companyId IS NULL OR d.companyId = :companyId)
            AND (:contactId IS NULL OR d.contactId = :contactId)
      """)
  Page<DealResponse> searchDeals(
      @Param("title") String title,
      @Param("companyId") String companyId,
      @Param("contactId") String contactId,
      @Param("ownerId") String ownerId,
      Pageable pageable);

  @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Deal d WHERE d.ownerId = :ownerId")
  BigDecimal getTotalRevenueByOwnerId(@Param("ownerId") String ownerId);

  @Query("""
      SELECT new com.sysluna.api.domain.dto.DealStaleDTO(
          d.id, d.title, c.name, d.updatedAt
      )
      FROM Deal d
      LEFT JOIN Company c ON d.companyId = c.id
      WHERE d.ownerId = :ownerId
        AND d.won = false
        AND d.lost = false
        AND d.updatedAt < :threshold
      ORDER BY d.updatedAt ASC
      """)
  List<DealStaleDTO> findStaleDeals(
      @Param("ownerId") String ownerId,
      @Param("threshold") LocalDateTime threshold,
      Pageable pageable);
}
