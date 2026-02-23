package com.wulghash.gamereleasetracker.infrastructure.web;

import com.wulghash.gamereleasetracker.domain.model.BacklogEntryNotFoundException;
import com.wulghash.gamereleasetracker.domain.model.GameAlreadyInBacklogException;
import com.wulghash.gamereleasetracker.domain.model.GameAlreadySubscribedException;
import com.wulghash.gamereleasetracker.domain.model.GameNotFoundException;
import com.wulghash.gamereleasetracker.domain.model.InvalidStatusTransitionException;
import com.wulghash.gamereleasetracker.domain.model.SubscriptionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler({GameNotFoundException.class, SubscriptionNotFoundException.class, BacklogEntryNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> handleNotFound(RuntimeException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler({GameAlreadySubscribedException.class, GameAlreadyInBacklogException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    Map<String, String> handleConflict(RuntimeException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    Map<String, String> handleInvalidTransition(InvalidStatusTransitionException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return Map.of("errors", errors);
    }
}
