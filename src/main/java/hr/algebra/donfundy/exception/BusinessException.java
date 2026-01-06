package hr.algebra.donfundy.exception;

public class BusinessException extends LocalizedException {

    public BusinessException(String messageCode) {
        super(messageCode);
    }

    public BusinessException(String messageCode, Object[] messageArgs) {
        super(messageCode, messageArgs);
    }

    public BusinessException(String messageCode, Throwable cause) {
        super(messageCode, cause);
    }

    public BusinessException(String messageCode, Object[] messageArgs, Throwable cause) {
        super(messageCode, messageArgs, cause);
    }
}
