package hr.algebra.donfundy.support;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    public String getLocalizedMessage(String messageId, Locale locale) {
        return messageSource.getMessage(messageId, null, locale);
    }

    public String getLocalizedMessage(String messageId, Object[] args, Locale locale) {
        return messageSource.getMessage(messageId, args, locale);
    }
}
