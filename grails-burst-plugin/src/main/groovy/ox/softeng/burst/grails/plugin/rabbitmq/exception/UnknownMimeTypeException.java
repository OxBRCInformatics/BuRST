package ox.softeng.burst.grails.plugin.rabbitmq.exception;

import ox.softeng.burst.grails.plugin.exception.BurstException;

/**
 * @since 19/02/2016
 */
public class UnknownMimeTypeException extends BurstException {

    public UnknownMimeTypeException(String errorCode, String contentType) {
        super(errorCode, "'" + contentType + "' is not a recognised MimeType");
    }

    public UnknownMimeTypeException(String errorCode, String contentType, Throwable cause) {
        super(errorCode, "'" + contentType + "' is not a recognised MimeType", cause);
    }
}
