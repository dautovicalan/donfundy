package hr.algebra.donfundy.exception;

public class ResourceNotFoundException extends LocalizedException {

    public ResourceNotFoundException(String messageCode) {
        super(messageCode);
    }

    public ResourceNotFoundException(String messageCode, Object[] messageArgs) {
        super(messageCode, messageArgs);
    }
}
