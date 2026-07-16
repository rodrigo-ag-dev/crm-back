package com.sysluna.api.ports.in;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sysluna.api.domain.dto.DealDTO;
import com.sysluna.api.domain.dto.DealResponse;
import com.sysluna.api.domain.dto.DealStaleDTO;

public interface DealPortIn {
  DealDTO getDealById(String id);

  BigInteger getTotalDeals();

  BigDecimal getTotalRevenue();

  List<DealDTO> getDealsByTitle(String title);

  Page<DealResponse> searchDeals(String title, String companyId, String contactId, Pageable pageable);

  DealDTO save(DealDTO dealDTO);

  boolean deleteDeal(String id);

  DealDTO lost(String id);

  DealDTO won(String id);

  DealDTO updateStage(String id, String stageId);

  List<DealStaleDTO> getStaleDeals(int days, int limit);
}
