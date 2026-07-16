package com.sysluna.api.ports.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sysluna.api.domain.dto.DealResponse;
import com.sysluna.api.domain.dto.DealStaleDTO;
import com.sysluna.api.domain.model.Deal;

public interface DealPortOut {
  Deal save(Deal deal);
  void delete(Deal deal);
  Optional<Deal> findByIdAndOwnerId(String id, String ownerId);
  List<Deal> findByTitleAndOwnerId(String title, String ownerId);
  long countByOwnerId(String ownerId);
  Page<DealResponse> searchDeals(String title, String companyId, String contactId, String ownerId, Pageable pageable);
  BigDecimal getTotalRevenueByOwnerId(String ownerId);
  List<DealStaleDTO> findStaleDeals(String ownerId, LocalDateTime threshold, Pageable pageable);
}
