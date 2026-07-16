package com.sysluna.api.infrastructure.config;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.sysluna.api.domain.exception.BusinessException;
import com.sysluna.api.domain.exception.NotFoundException;
import com.sysluna.api.domain.exception.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ProblemDetail> handleBusinessException(
      BusinessException ex,
      HttpServletRequest request) {
    return buildProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ProblemDetail> handleNotFoundException(
      NotFoundException ex,
      HttpServletRequest request) {
    return buildProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ProblemDetail> handleUnauthorizedException(
      UnauthorizedException ex,
      HttpServletRequest request) {
    return buildProblemDetail(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ProblemDetail> handleSpringAuthenticationException(
      AuthenticationException ex,
      HttpServletRequest request) {
    return buildProblemDetail(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> handleAccessDeniedException(
      AccessDeniedException ex,
      HttpServletRequest request) {
    return buildProblemDetail(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidationException(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problemDetail.setTitle("Validation error");
    problemDetail.setDetail("One or more fields are invalid.");
    problemDetail.setProperty("path", request.getRequestURI());
    problemDetail.setProperty("timestamp", OffsetDateTime.now());

    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    problemDetail.setProperty("errors", errors);

    return ResponseEntity.badRequest().body(problemDetail);
  }

  @ExceptionHandler({
      HttpMessageNotReadableException.class,
      MissingServletRequestParameterException.class,
      MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ProblemDetail> handleBadRequestExceptions(
      Exception ex,
      HttpServletRequest request) {
    return buildProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ProblemDetail> handleMethodNotSupportedException(
      HttpRequestMethodNotSupportedException ex,
      HttpServletRequest request) {
    return buildProblemDetail(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGenericException(
      Exception ex,
      HttpServletRequest request) {
    return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error.", request.getRequestURI());
  }

  private ResponseEntity<ProblemDetail> buildProblemDetail(
      HttpStatus status,
      String detail,
      String path) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setTitle(status.getReasonPhrase());
    problemDetail.setDetail(detail);
    problemDetail.setProperty("path", path);
    problemDetail.setProperty("timestamp", OffsetDateTime.now());
    return ResponseEntity.status(status).body(problemDetail);
  }
}
