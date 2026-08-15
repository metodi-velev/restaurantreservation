package com.example.restaurantreservation.exception;

import com.example.restaurantreservation.dto.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> map = ex.getBindingResult().getAllErrors().stream()
                .collect(Collectors.toMap(ObjectError::getCode, ObjectError::getDefaultMessage));

        return ResponseEntity.badRequest().body(map);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.error("IllegalArgumentException: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorDto.builder()
                        .code("INVALID_REQUEST")
                        .message(exception.getMessage())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .build());
    }

    @ExceptionHandler(TimeSlotNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTimeSlotNotFoundException(TimeSlotNotFoundException exception) {
        log.warn("TimeSlotNotFoundException: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDto.builder()
                        .code("TIME_SLOT_NOT_FOUND")
                        .message(exception.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .build());
    }

    @ExceptionHandler(TimeSlotAlreadyReservedException.class)
    public ResponseEntity<ErrorDto> handleTimeSlotAlreadyReservedException(TimeSlotAlreadyReservedException exception) {
        log.warn("TimeSlotAlreadyReservedException: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorDto.builder()
                        .code("TIME_SLOT_ALREADY_RESERVED")
                        .message(exception.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .build());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorDto> handleOptimisticLockingFailureException(OptimisticLockingFailureException exception) {
        log.warn("OptimisticLockingFailureException: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorDto.builder()
                        .code("CONCURRENT_MODIFICATION")
                        .message(exception.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .build());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ErrorDto> handleReservationNotFoundException(ReservationNotFoundException exception) {
        log.warn("ReservationNotFoundException: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDto.builder()
                        .code("RESERVATION_NOT_FOUND")
                        .message(exception.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDto> handleRuntimeException(RuntimeException exception) {
        log.error("RuntimeException: {}", exception.getMessage(), exception);

        HttpStatus status;
        String code;

        if (exception.getMessage() != null && exception.getMessage().contains("no table which suits your search criteria")) {
            status = HttpStatus.NOT_FOUND;
            code = "TABLE_NOT_FOUND";
        } else if (exception.getMessage() != null && exception.getMessage().contains("concurrent modification")) {
            status = HttpStatus.CONFLICT;
            code = "CONCURRENT_MODIFICATION";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = "INTERNAL_ERROR";
        }

        return ResponseEntity.status(status)
                .body(ErrorDto.builder()
                        .code(code)
                        .message(exception.getMessage())
                        .status(status.value())
                        .build());
    }
}