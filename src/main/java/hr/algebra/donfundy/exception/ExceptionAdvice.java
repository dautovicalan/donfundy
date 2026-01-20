package hr.algebra.donfundy.exception;

import hr.algebra.donfundy.support.ErrorResponse;
import hr.algebra.donfundy.support.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ExceptionAdvice {

    private final MessageService messageService;

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        log.error("Bad credentials: {}", ex.getMessage());
        Locale locale = request.getLocale();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(messageService.getLocalizedMessage("error.authentication.failed", locale));
        return errorResponse;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessageCode());
        return createErrorResponse(ex, request.getLocale());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBusinessException(BusinessException ex, WebRequest request) {
        log.error("Business error: {}", ex.getMessageCode());
        return createErrorResponse(ex, request.getLocale());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(ValidationException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessageCode());
        return createErrorResponse(ex, request.getLocale());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        log.error("Authentication failed: {}", ex.getMessage());
        Locale locale = request.getLocale();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(messageService.getLocalizedMessage("error.authentication.failed", locale));
        return errorResponse;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAuthorizationDeniedException(AuthorizationDeniedException ex, WebRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        Locale locale = request.getLocale();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(messageService.getLocalizedMessage("error.access.denied", locale));
        return errorResponse;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        Locale locale = request.getLocale();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(messageService.getLocalizedMessage("error.access.denied", locale));
        return errorResponse;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument: {}", ex.getMessage());
        Locale locale = request.getLocale();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(messageService.getLocalizedMessage("error.illegal.argument", locale));
        return errorResponse;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        Locale locale = request.getLocale();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(messageService.getLocalizedMessage("error.internal.server", locale));
        return errorResponse;
    }

    private ErrorResponse createErrorResponse(LocalizedException ex, Locale locale) {
        ErrorResponse errorResponse = new ErrorResponse();
        String message = messageService.getLocalizedMessage(
                ex.getMessageCode(),
                ex.getMessageArgs(),
                locale
        );
        errorResponse.setMessage(message);
        return errorResponse;
    }
}
