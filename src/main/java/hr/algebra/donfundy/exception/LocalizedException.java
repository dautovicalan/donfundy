package hr.algebra.donfundy.exception;

import lombok.Getter;

@Getter
public abstract class LocalizedException extends RuntimeException {

    private final String messageCode;
    private final Object[] messageArgs;

    protected LocalizedException(String messageCode) {
        this(messageCode, null);
    }

    protected LocalizedException(String messageCode, Object[] messageArgs) {
        super(messageCode);
        this.messageCode = messageCode;
        this.messageArgs = messageArgs;
    }

}
