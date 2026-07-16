package com.sysluna.api.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.sysluna.api.domain.dto.DealResponse;
import com.sysluna.api.domain.dto.DealStaleDTO;
import com.sysluna.api.domain.model.Deal;
import com.sysluna.api.infrastructure.repository.DealRepository;
import com.sysluna.api.ports.out.DealPortOut;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class DealPersistenceAdapter implements DealPortOut {

  private final DealRepository dealRepository;

  @Override
  public Deal save(Deal deal) {
    return dealRepository.save(deal);
  }

  @Override
  public void delete(Deal deal) {
    dealRepository.delete(deal);
  }

  @Override
  public Optional<Deal> findByIdAndOwnerId(String id, String ownerId) {
    return dealRepository.findByIdAndOwnerId(id, ownerId);
  }

  @Override
  public List<Deal> findByTitleAndOwnerId(String title, String ownerId) {
    return dealRepository.findByTitleAndOwnerId(title, ownerId);
  }

  @Override
  public long countByOwnerId(String ownerId) {
    return dealRepository.countByOwnerId(ownerId);
  }

  @Override
  public Page<DealResponse> searchDeals(String title, String companyId, String contactId, String ownerId, Pageable pageable) {
    return dealRepository.searchDeals(title, companyId, contactId, ownerId, pageable);
  }

  @Override
  public BigDecimal getTotalRevenueByOwnerId(String ownerId) {
    return dealRepository.getTotalRevenueByOwnerId(ownerId);
  }

  @Override
  public List<DealStaleDTO> findStaleDeals(String ownerId, LocalDateTime threshold, Pageable pageable) {
    return dealRepository.findStaleDeals(ownerId, threshold, pageable);
  }
}
