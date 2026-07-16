package com.sysluna.api.infrastructure.controller;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sysluna.api.domain.dto.DealDTO;
import com.sysluna.api.domain.dto.DealIdDTO;
import com.sysluna.api.domain.dto.DealResponse;
import com.sysluna.api.domain.dto.DealStageDTO;
import com.sysluna.api.domain.dto.DealStaleDTO;
import com.sysluna.api.ports.in.DealPortIn;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/deals")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Deals", description = "Deal management API")
public class DealController {
  private final DealPortIn dealPortIn;

  @GetMapping("title")
  public List<DealDTO> getDealByTitle(@RequestParam String title) {
    return dealPortIn.getDealsByTitle(title);
  }

  @GetMapping("/{id}")
  public DealDTO getDealById(@PathVariable String id) {
    return dealPortIn.getDealById(id);
  }

  @GetMapping("/total")
  public BigInteger getTotalDeals() {
    return dealPortIn.getTotalDeals();
  }

  @GetMapping("/revenue")
  public BigDecimal getTotalRevenue() {
    return dealPortIn.getTotalRevenue();
  }

  @GetMapping("/stale")
  public List<DealStaleDTO> getStaleDeals(
      @RequestParam(defaultValue = "7") int days,
      @RequestParam(defaultValue = "5") int limit) {
    return dealPortIn.getStaleDeals(days, limit);
  }

  @GetMapping("/search")
  public Page<DealResponse> searchDeals(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String companyId,
      @RequestParam(required = false) String contactId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return dealPortIn.searchDeals(title, companyId, contactId, PageRequest.of(page, size, Sort.by("title").ascending()));
  }

  @PostMapping
  public DealDTO saveDeal(@Valid @RequestBody DealDTO dealDTO) {
    return dealPortIn.save(dealDTO);
  }

  @PostMapping("/lost")
  public DealDTO lost(@Valid @RequestBody DealIdDTO dealIdDTO) {
    return dealPortIn.lost(dealIdDTO.getId());
  }

  @PostMapping("/won")
  public DealDTO won(@Valid @RequestBody DealIdDTO dealIdDTO) {
    return dealPortIn.won(dealIdDTO.getId());
  }

  @PostMapping("/stage")
  public DealDTO updateStage(@Valid @RequestBody DealStageDTO dealStageDTO) {
    return dealPortIn.updateStage(dealStageDTO.getId(), dealStageDTO.getStageId());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDeal(@PathVariable String id) {
    boolean deleted = dealPortIn.deleteDeal(id);
    if (!deleted) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
  }
}
