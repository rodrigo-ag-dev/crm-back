package com.sysluna.api.application;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sysluna.api.domain.dto.DealDTO;
import com.sysluna.api.domain.dto.DealResponse;
import com.sysluna.api.domain.dto.DealStaleDTO;
import com.sysluna.api.domain.exception.NotFoundException;
import com.sysluna.api.domain.exception.UnauthorizedException;
import com.sysluna.api.domain.model.Deal;
import com.sysluna.api.domain.model.DealStageHistory;
import com.sysluna.api.ports.in.DealPortIn;
import com.sysluna.api.ports.out.DealPortOut;
import com.sysluna.api.ports.out.DealStageHistoryPortOut;
import com.sysluna.api.ports.out.UserPortOut;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DealService implements DealPortIn {

  private final DealPortOut dealPortOut;
  private final DealStageHistoryPortOut dealStageHistoryPortOut;
  private final UserPortOut userPortOut;

  @Override
  public BigInteger getTotalDeals() {
    return BigInteger.valueOf(dealPortOut.countByOwnerId(getCurrentUserId()));
  }

  @Override
  public BigDecimal getTotalRevenue() {
    return dealPortOut.getTotalRevenueByOwnerId(getCurrentUserId());
  }

  @Override
  public DealDTO save(DealDTO dealDTO) {
    String currentUserId = getCurrentUserId();
    Deal deal;
    if (dealDTO.getId() != null) {
      deal = dealPortOut.findByIdAndOwnerId(dealDTO.getId(), currentUserId)
          .orElseThrow(() -> new NotFoundException("Deal not found with ID: " + dealDTO.getId()));
      deal.setTitle(dealDTO.getTitle());
      deal.setCompanyId(dealDTO.getCompanyId());
      deal.setContactId(dealDTO.getContactId());
      deal.setOwnerId(currentUserId);
      deal.setStageId(dealDTO.getStageId());
      deal.setDescription(dealDTO.getDescription());
      deal.setProbability(dealDTO.getProbability());
      deal.setAmount(dealDTO.getAmount());
      deal.setCloseDateExpected(dealDTO.getCloseDateExpected());
      deal.validate();
    } else {
      dealDTO.setOwnerId(currentUserId);
      deal = Deal.fromDTO(dealDTO);
    }
    return DealDTO.fromDeal(dealPortOut.save(deal));
  }

  @Override
  public boolean deleteDeal(String id) {
    Deal deal = dealPortOut.findByIdAndOwnerId(id, getCurrentUserId())
        .orElseThrow(() -> new NotFoundException("Deal not found with ID: " + id));
    dealPortOut.delete(deal);
    return true;
  }

  @Override
  public List<DealDTO> getDealsByTitle(String title) {
    List<Deal> deals = dealPortOut.findByTitleAndOwnerId(title, getCurrentUserId());
    return deals != null && !deals.isEmpty()
        ? deals.stream().map(DealDTO::fromDeal).toList()
        : null;
  }

  @Override
  public Page<DealResponse> searchDeals(String title, String companyId, String contactId, Pageable pageable) {
    return dealPortOut.searchDeals(title, companyId, contactId, getCurrentUserId(), pageable);
  }

  @Override
  public DealDTO getDealById(String id) {
    Deal deal = dealPortOut.findByIdAndOwnerId(id, getCurrentUserId())
        .orElseThrow(() -> new NotFoundException("Deal not found with ID: " + id));
    return DealDTO.fromDeal(deal);
  }

  @Override
  public DealDTO lost(String id) {
    Deal deal = dealPortOut.findByIdAndOwnerId(id, getCurrentUserId())
        .orElseThrow(() -> new NotFoundException("Deal not found with ID: " + id));
    deal.markAsLost();
    return DealDTO.fromDeal(dealPortOut.save(deal));
  }

  @Override
  public DealDTO won(String id) {
    Deal deal = dealPortOut.findByIdAndOwnerId(id, getCurrentUserId())
        .orElseThrow(() -> new NotFoundException("Deal not found with ID: " + id));
    deal.markAsWon();
    return DealDTO.fromDeal(dealPortOut.save(deal));
  }

  @Override
  @Transactional
  public DealDTO updateStage(String id, String stageId) {
    String changeById = getCurrentUserId();
    Deal deal = dealPortOut.findByIdAndOwnerId(id, changeById)
        .orElseThrow(() -> new NotFoundException("Deal not found with ID: " + id));

    dealStageHistoryPortOut.save(DealStageHistory.builder()
        .dealId(id)
        .changedById(changeById)
        .fromStageId(deal.getStageId())
        .toStageId(stageId)
        .build());

    deal.changeStage(stageId);
    return DealDTO.fromDeal(dealPortOut.save(deal));
  }

  @Override
  public List<DealStaleDTO> getStaleDeals(int days, int limit) {
    LocalDateTime threshold = LocalDateTime.now().minusDays(days);
    return dealPortOut.findStaleDeals(getCurrentUserId(), threshold, PageRequest.of(0, limit));
  }

  private String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken)) {
      var user = userPortOut.findByEmail(authentication.getName());
      if (user == null) {
        throw new UnauthorizedException("Authenticated user not found.");
      }
      return user.getId();
    }
    throw new UnauthorizedException("Authenticated user not found.");
  }
}
