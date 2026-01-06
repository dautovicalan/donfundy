package hr.algebra.donfundy.exception;

public class ValidationException extends LocalizedException {

    public ValidationException(String messageCode) {
        super(messageCode);
    }

    public ValidationException(String messageCode, Object[] messageArgs) {
        super(messageCode, messageArgs);
    }
}
