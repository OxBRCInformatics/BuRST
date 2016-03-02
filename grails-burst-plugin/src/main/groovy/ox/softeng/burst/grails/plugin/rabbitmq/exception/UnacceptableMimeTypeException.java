package ox.softeng.burst.grails.plugin.rabbitmq.exception;

import ox.softeng.burst.grails.plugin.exception.BurstException;

/**
 * @since 01/03/2016
 */
public class UnacceptableMimeTypeException extends BurstException {

    public UnacceptableMimeTypeException(String errorCode, String queue, String mimetype) {
        super(errorCode, mimetype + " is not handled by the queue '" + queue + "'");
    }
}
