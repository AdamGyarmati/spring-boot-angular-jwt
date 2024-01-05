package hu.gyarmati.securitydemowithangular.exceptionhandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameAlreadyInDatabaseException.class)
    public ResponseEntity<ValidationError> handleUsernameAlreadyInDatabaseException(UsernameAlreadyInDatabaseException exception) {
        ValidationError validationError = new ValidationError("username", "Username is taken: " + exception.getUsername());
        return new ResponseEntity<>(validationError, HttpStatus.BAD_REQUEST);
    }
}
