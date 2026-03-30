package nic.ua.poker_estimation.common.api;

import java.time.Instant;
import nic.ua.poker_estimation.common.service.ConflictException;
import nic.ua.poker_estimation.common.service.NotFoundException;
import nic.ua.poker_estimation.common.service.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public final class RestExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({ConflictException.class, ValidationException.class, MethodArgumentNotValidException.class})
    ResponseEntity<ApiErrorResponse> handleConflict(Exception exception) {
        String message = exception instanceof MethodArgumentNotValidException methodArgumentNotValidException
            ? methodArgumentNotValidException.getBindingResult().getAllErrors().getFirst().getDefaultMessage()
            : exception.getMessage();
        return buildResponse(HttpStatus.CONFLICT, message);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
            .body(new ApiErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message));
    }
}
