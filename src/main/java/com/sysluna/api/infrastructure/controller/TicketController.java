package com.sysluna.api.infrastructure.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sysluna.api.domain.dto.TicketCommentDTO;
import com.sysluna.api.domain.dto.TicketDTO;
import com.sysluna.api.domain.dto.TicketStageChangeDTO;
import com.sysluna.api.domain.dto.TicketStageDTO;
import com.sysluna.api.domain.dto.TicketStageHistoryDTO;
import com.sysluna.api.ports.in.TicketPortIn;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/tickets")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tickets", description = "Ticket management API")
public class TicketController {
  private final TicketPortIn ticketPortIn;

  @GetMapping("/{id}")
  public TicketDTO getTicketById(@PathVariable String id) {
    return ticketPortIn.getTicketById(id);
  }

  @GetMapping("/stages")
  public List<TicketStageDTO> listTicketStages() {
    return ticketPortIn.listTicketStages();
  }

  @GetMapping("/pending")
  public List<TicketDTO> getPendingTickets(@RequestParam(defaultValue = "5") int limit) {
    return ticketPortIn.getPendingTickets(limit);
  }

  @GetMapping("/search")
  public Page<TicketDTO> searchTickets(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String companyId,
      @RequestParam(required = false) String contactId,
      @RequestParam(required = false) String ticketStageId,
      @RequestParam(required = false) Boolean canceled,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ticketPortIn.searchTickets(
        title,
        companyId,
        contactId,
        ticketStageId,
        canceled,
        PageRequest.of(page, size, Sort.by("createdAt").descending()));
  }

  @PostMapping
  public TicketDTO saveTicket(@Valid @RequestBody TicketDTO ticketDTO) {
    return ticketPortIn.save(ticketDTO);
  }

  @PutMapping("/{id}")
  public TicketDTO updateTicket(@PathVariable String id, @Valid @RequestBody TicketDTO ticketDTO) {
    return ticketPortIn.update(id, ticketDTO);
  }

  @PatchMapping("/{id}/stage")
  public TicketDTO updateTicketStage(@PathVariable String id, @Valid @RequestBody TicketStageChangeDTO ticketStageChangeDTO) {
    return ticketPortIn.updateStage(id, ticketStageChangeDTO.getTicketStageId());
  }

  @PostMapping("/{id}/cancel")
  public ResponseEntity<TicketDTO> cancelTicket(@PathVariable String id) {
    return ResponseEntity.ok(ticketPortIn.cancel(id));
  }

  @GetMapping("/{id}/history")
  public List<TicketStageHistoryDTO> getTicketHistory(@PathVariable String id) {
    return ticketPortIn.getTicketHistory(id);
  }

  @GetMapping("/{id}/comments")
  public List<TicketCommentDTO> getTicketComments(@PathVariable String id) {
    return ticketPortIn.getTicketComments(id);
  }

  @PostMapping("/{id}/comments")
  public TicketCommentDTO addTicketComment(@PathVariable String id, @Valid @RequestBody TicketCommentDTO commentDTO) {
    return ticketPortIn.addTicketComment(id, commentDTO);
  }
}
