package com.abhijeet.HookCatcher.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
   @ExceptionHandler(RuntimeException.class)
   public ResponseEntity<String> handleRuntimeException(RuntimeException ex)
   {
       return ResponseEntity
               .status(HttpStatus.INTERNAL_SERVER_ERROR)
               .body("An unexpected error occured");
   }

   @ExceptionHandler(UsernameAlreadyExistsException.class)
   public ResponseEntity<String> UserAlready(UsernameAlreadyExistsException ex)
   {
       return ResponseEntity
               .status(HttpStatus.CONFLICT)
               .body("Username Already Present");
   }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(EndpointNotFoundException.class)
    public ResponseEntity<String> handleEndpointNotFound(EndpointNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidTargetException.class)
    public ResponseEntity<String> handleInvalidTarget(InvalidTargetException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(org.springframework.web.client.ResourceAccessException.class)
    public ResponseEntity<String> handleUnreachable(org.springframework.web.client.ResourceAccessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Could not reach the target URL");
    }
}
