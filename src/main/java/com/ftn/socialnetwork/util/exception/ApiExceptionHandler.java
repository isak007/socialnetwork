package com.ftn.socialnetwork.util.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {EntityNotFoundException.class})
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException e){

        HttpStatus notFound = HttpStatus.NOT_FOUND;

        ExceptionMessage exceptionMessage = new ExceptionMessage(
                e.getMessage(),
                notFound,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(exceptionMessage, notFound);
    }

    @ExceptionHandler(value = {UsernameExistsException.class})
    public ResponseEntity<Object> handleUsernameExistsException(UsernameExistsException e){

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        ExceptionMessage exceptionMessage = new ExceptionMessage(
                e.getMessage(),
                badRequest,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(exceptionMessage, badRequest);
    }

    @ExceptionHandler(value = {EmailExistsException.class})
    public ResponseEntity<Object> handleEmailExistsException(EmailExistsException e){

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        ExceptionMessage exceptionMessage = new ExceptionMessage(
                e.getMessage(),
                badRequest,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(exceptionMessage, badRequest);
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException e){

        HttpStatus badRequest = HttpStatus.UNAUTHORIZED;

        ExceptionMessage exceptionMessage = new ExceptionMessage(
                e.getMessage(),
                badRequest,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(exceptionMessage, badRequest);
    }

    @ExceptionHandler(value = {EntityExistsException.class})
    public ResponseEntity<Object> entityExistsException(EntityExistsException e){

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        ExceptionMessage exceptionMessage = new ExceptionMessage(
                e.getMessage(),
                badRequest,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(exceptionMessage, badRequest);
    }

    @ExceptionHandler(value = {InvalidEmailException.class})
    public ResponseEntity<Object> invalidEmailException(InvalidEmailException e){

        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        ExceptionMessage exceptionMessage = new ExceptionMessage(
                e.getMessage(),
                badRequest,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(exceptionMessage, badRequest);
    }
}
