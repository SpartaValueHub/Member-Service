package com.sparta.member_service.adaptor.in.web;

import com.sparta.member_service.adaptor.in.web.vo.ErrorResponseVo;
import com.sparta.member_service.application.exception.DuplicateResourceException;
import com.sparta.member_service.application.exception.ForbiddenException;
import com.sparta.member_service.application.exception.InvalidTermConsentException;
import com.sparta.member_service.application.exception.MediaConfigurationException;
import com.sparta.member_service.application.exception.MediaInvalidRequestException;
import com.sparta.member_service.application.exception.MediaStorageException;
import com.sparta.member_service.application.exception.MemberNotFoundException;
import com.sparta.member_service.application.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseVo> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(InvalidTermConsentException.class)
    public ResponseEntity<ErrorResponseVo> handleInvalidTermConsent(
            InvalidTermConsentException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MediaInvalidRequestException.class)
    public ResponseEntity<ErrorResponseVo> handleMediaInvalidRequest(
            MediaInvalidRequestException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MediaConfigurationException.class)
    public ResponseEntity<ErrorResponseVo> handleMediaConfiguration(
            MediaConfigurationException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseVo> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseVo> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(error(HttpStatus.UNAUTHORIZED, ex.getCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponseVo> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(error(HttpStatus.FORBIDDEN, ex.getCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MediaStorageException.class)
    public ResponseEntity<ErrorResponseVo> handleMediaStorage(
            MediaStorageException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponseVo> handleMemberNotFound(
            MemberNotFoundException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), request.getRequestURI()));
    }

    private ErrorResponseVo error(HttpStatus status, String code, String message, String path) {
        return ErrorResponseVo.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .code(code)
                .message(message)
                .path(path)
                .build();
    }
}
