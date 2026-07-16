package com.sysluna.api.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sysluna.api.domain.dto.TicketCommentDTO;
import com.sysluna.api.domain.dto.TicketDTO;
import com.sysluna.api.domain.dto.TicketStageDTO;
import com.sysluna.api.domain.dto.TicketStageHistoryDTO;
import com.sysluna.api.domain.exception.BusinessException;
import com.sysluna.api.domain.exception.NotFoundException;
import com.sysluna.api.domain.exception.UnauthorizedException;
import com.sysluna.api.domain.model.Ticket;
import com.sysluna.api.domain.model.TicketComment;
import com.sysluna.api.domain.model.TicketStage;
import com.sysluna.api.domain.model.TicketStageHistory;
import com.sysluna.api.ports.in.TicketPortIn;
import com.sysluna.api.ports.out.TicketCommentPortOut;
import com.sysluna.api.ports.out.TicketPortOut;
import com.sysluna.api.ports.out.TicketStageHistoryPortOut;
import com.sysluna.api.ports.out.TicketStagePortOut;
import com.sysluna.api.ports.out.UserPortOut;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TicketService implements TicketPortIn {

  private static final String OPEN_STAGE_NAME = "Open";
  private static final String CLOSED_STAGE_NAME = "Closed";

  private final TicketPortOut ticketPortOut;
  private final TicketStagePortOut ticketStagePortOut;
  private final TicketStageHistoryPortOut ticketStageHistoryPortOut;
  private final TicketCommentPortOut ticketCommentPortOut;
  private final UserPortOut userPortOut;

  @Override
  public TicketDTO getTicketById(String id) {
    Ticket ticket = ticketPortOut.findByIdAndOwnerId(id, getCurrentUserId())
        .orElseThrow(() -> new NotFoundException("Ticket not found with ID: " + id));
    return TicketDTO.fromTicket(ticket);
  }

  @Override
  public List<TicketStageDTO> listTicketStages() {
    return ticketStagePortOut.findAll(Sort.by("order").ascending())
        .stream()
        .map(TicketStageDTO::fromTicketStage)
        .toList();
  }

  @Override
  public Page<TicketDTO> searchTickets(
      String title,
      String companyId,
      String contactId,
      String ticketStageId,
      Boolean canceled,
      Pageable pageable) {
    String currentUserId = getCurrentUserId();
    String titleLike = normalizeFilter(title);
    if (titleLike != null) {
      titleLike = "%" + titleLike + "%";
    }
    return ticketPortOut.searchTickets(
        titleLike,
        normalizeFilter(companyId),
        normalizeFilter(contactId),
        normalizeFilter(ticketStageId),
        canceled,
        currentUserId,
        pageable).map(TicketDTO::fromTicket);
  }

  @Override
  public TicketDTO save(TicketDTO ticketDTO) {
    String currentUserId = getCurrentUserId();
    TicketStage openStage = getStageByNameOrThrow(OPEN_STAGE_NAME);

    Ticket ticket;
    if (ticketDTO.getId() != null) {
      ticket = ticketPortOut.findByIdAndOwnerId(ticketDTO.getId(), currentUserId)
          .orElseThrow(() -> new NotFoundException("Ticket not found with ID: " + ticketDTO.getId()));
      String oldStageId = ticket.getTicketStageId();
      applyEditableFields(ticket, ticketDTO, currentUserId);
      recordStageHistoryIfNeeded(ticket.getId(), oldStageId, ticket.getTicketStageId(), currentUserId);
    } else {
      ticketDTO.setOwnerId(currentUserId);
      if (ticketDTO.getTicketStageId() == null) {
        ticketDTO.setTicketStageId(openStage.getId());
      }
      ticket = Ticket.fromDTO(ticketDTO);
      ticket.setOwnerId(currentUserId);
      ensureStageExists(ticket.getTicketStageId());
    }

    normalizeLifecycleFields(ticket);
    return TicketDTO.fromTicket(ticketPortOut.save(ticket));
  }

  @Override
  public TicketDTO update(String id, TicketDTO ticketDTO) {
    String currentUserId = getCurrentUserId();
    Ticket ticket = ticketPortOut.findByIdAndOwnerId(id, currentUserId)
        .orElseThrow(() -> new NotFoundException("Ticket not found with ID: " + id));
    String oldStageId = ticket.getTicketStageId();
    applyEditableFields(ticket, ticketDTO, currentUserId);
    recordStageHistoryIfNeeded(ticket.getId(), oldStageId, ticket.getTicketStageId(), currentUserId);
    normalizeLifecycleFields(ticket);
    return TicketDTO.fromTicket(ticketPortOut.save(ticket));
  }

  @Override
  @Transactional
  public TicketDTO updateStage(String id, String ticketStageId) {
    String currentUserId = getCurrentUserId();
    Ticket ticket = ticketPortOut.findByIdAndOwnerId(id, currentUserId)
        .orElseThrow(() -> new NotFoundException("Ticket not found with ID: " + id));

    if (Boolean.TRUE.equals(ticket.getCanceled())) {
      throw new BusinessException("Canceled tickets cannot change stage.");
    }
    ensureStageExists(ticketStageId);

    String oldStageId = ticket.getTicketStageId();
    if (oldStageId != null && oldStageId.equals(ticketStageId)) {
      throw new BusinessException("The ticket is already in this stage.");
    }

    ticketStageHistoryPortOut.save(TicketStageHistory.builder()
        .ticketId(id)
        .changedById(currentUserId)
        .fromStageId(oldStageId)
        .toStageId(ticketStageId)
        .build());

    ticket.changeStage(ticketStageId);
    return TicketDTO.fromTicket(ticketPortOut.save(ticket));
  }

  @Override
  @Transactional
  public TicketDTO cancel(String id) {
    String currentUserId = getCurrentUserId();
    Ticket ticket = ticketPortOut.findByIdAndOwnerId(id, currentUserId)
        .orElseThrow(() -> new NotFoundException("Ticket not found with ID: " + id));
    if (Boolean.TRUE.equals(ticket.getCanceled())) {
      return TicketDTO.fromTicket(ticket);
    }
    ticket.markAsCanceled(ticket.getTicketStageId(), LocalDateTime.now());
    return TicketDTO.fromTicket(ticketPortOut.save(ticket));
  }

  @Override
  public List<TicketDTO> getPendingTickets(int limit) {
    return ticketPortOut.findPendingTickets(getCurrentUserId(), PageRequest.of(0, limit))
        .stream()
        .map(TicketDTO::fromTicket)
        .toList();
  }

  @Override
  public List<TicketStageHistoryDTO> getTicketHistory(String ticketId) {
    ensureTicketOwnedByCurrentUser(ticketId);
    return ticketStageHistoryPortOut.findByTicketId(ticketId);
  }

  @Override
  public List<TicketCommentDTO> getTicketComments(String ticketId) {
    ensureTicketOwnedByCurrentUser(ticketId);
    return ticketCommentPortOut.findByTicketId(ticketId);
  }

  @Override
  public TicketCommentDTO addTicketComment(String ticketId, TicketCommentDTO commentDTO) {
    ensureTicketOwnedByCurrentUser(ticketId);
    TicketComment comment = ticketCommentPortOut.save(TicketComment.builder()
        .ticketId(ticketId)
        .authorId(getCurrentUserId())
        .type(commentDTO.getType())
        .body(commentDTO.getBody())
        .build());
    return ticketCommentPortOut.findDTOById(comment.getId())
        .orElseThrow(() -> new NotFoundException("Ticket comment not found with ID: " + comment.getId()));
  }

  private void ensureTicketOwnedByCurrentUser(String ticketId) {
    ticketPortOut.findByIdAndOwnerId(ticketId, getCurrentUserId())
        .orElseThrow(() -> new NotFoundException("Ticket not found with ID: " + ticketId));
  }

  private void applyEditableFields(Ticket ticket, TicketDTO ticketDTO, String currentUserId) {
    ticket.setCompanyId(ticketDTO.getCompanyId());
    ticket.setContactId(ticketDTO.getContactId());
    ticket.setOwnerId(currentUserId);
    ticket.setTicketStageId(
        ticketDTO.getTicketStageId() != null ? ticketDTO.getTicketStageId() : ticket.getTicketStageId());
    ticket.setCanceledStageId(ticketDTO.getCanceledStageId());
    ticket.setTitle(ticketDTO.getTitle());
    ticket.setDescription(ticketDTO.getDescription());
    ticket.setDueDate(ticketDTO.getDueDate());
    ticket.setClosedAt(ticketDTO.getClosedAt());
    ticket.setCanceledAt(ticketDTO.getCanceledAt());
    if (ticketDTO.getCanceled() != null) {
      ticket.setCanceled(ticketDTO.getCanceled());
    }
    ticket.validate();
    ensureStageExists(ticket.getTicketStageId());
  }

  private void normalizeLifecycleFields(Ticket ticket) {
    if (Boolean.TRUE.equals(ticket.getCanceled()) && ticket.getCanceledAt() == null) {
      ticket.setCanceledAt(LocalDateTime.now());
    }
    if (Boolean.TRUE.equals(ticket.getCanceled()) && ticket.getCanceledStageId() == null) {
      ticket.setCanceledStageId(ticket.getTicketStageId());
    }
    updateClosedFlag(ticket, ticket.getTicketStageId());
  }

  private void updateClosedFlag(Ticket ticket, String stageId) {
    TicketStage closedStage = ticketStagePortOut.findByNameIgnoreCase(CLOSED_STAGE_NAME)
        .orElseThrow(() -> new NotFoundException("Closed ticket stage not found."));
    if (closedStage.getId().equals(stageId)) {
      if (ticket.getClosedAt() == null) {
        ticket.setClosedAt(LocalDateTime.now());
      }
    } else if (ticket.getClosedAt() != null && !closedStage.getId().equals(stageId)) {
      ticket.reopen();
    }
  }

  private void recordStageHistoryIfNeeded(String ticketId, String oldStageId, String newStageId, String changedById) {
    if (oldStageId == null || newStageId == null || oldStageId.equals(newStageId)) {
      return;
    }
    ticketStageHistoryPortOut.save(TicketStageHistory.builder()
        .ticketId(ticketId)
        .changedById(changedById)
        .fromStageId(oldStageId)
        .toStageId(newStageId)
        .build());
  }

  private TicketStage getStageByNameOrThrow(String name) {
    return ticketStagePortOut.findByNameIgnoreCase(name)
        .orElseThrow(() -> new NotFoundException("Ticket stage not found with name: " + name));
  }

  private void ensureStageExists(String stageId) {
    if (stageId == null || stageId.trim().isEmpty()) {
      throw new BusinessException("Ticket stage ID is required.");
    }
    ticketStagePortOut.findById(stageId)
        .orElseThrow(() -> new NotFoundException("Ticket stage not found with ID: " + stageId));
  }

  private String normalizeFilter(String value) {
    return value == null || value.trim().isEmpty() ? null : value.trim();
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
