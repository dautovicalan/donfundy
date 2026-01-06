package hr.algebra.donfundy.exception;

import lombok.Getter;

@Getter
public abstract class LocalizedException extends RuntimeException {

    private final String messageCode;
    private final Object[] messageArgs;

    protected LocalizedException(String messageCode) {
        this(messageCode, (Object[]) null);
    }

    protected LocalizedException(String messageCode, Object[] messageArgs) {
        super(messageCode);
        this.messageCode = messageCode;
        this.messageArgs = messageArgs;
    }

    protected LocalizedException(String messageCode, Throwable cause) {
        this(messageCode, null, cause);
    }

    protected LocalizedException(String messageCode, Object[] messageArgs, Throwable cause) {
        super(messageCode, cause);
        this.messageCode = messageCode;
        this.messageArgs = messageArgs;
    }
}
