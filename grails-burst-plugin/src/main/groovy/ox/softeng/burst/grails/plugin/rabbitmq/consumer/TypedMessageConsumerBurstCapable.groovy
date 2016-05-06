package ox.softeng.burst.grails.plugin.rabbitmq.consumer

import com.budjb.rabbitmq.consumer.MessageContext
import groovy.transform.CompileStatic
import org.springframework.http.HttpStatus
import ox.softeng.burst.grails.plugin.exception.BurstException

/**
 * @since 18/02/2016
 */
@CompileStatic
trait TypedMessageConsumerBurstCapable<T> extends MessageConsumerBurstCapable {

    T handleMessage(T body, MessageContext messageContext) {
        String messageId = getMessageId(messageContext)
        try {
            return processMessage(body, messageId, messageContext)
        } catch (BurstException ex) {
            handleException(ex, messageId, messageContext)
        } catch (Exception ex) {
            handleException(new BurstException('BURST03', 'Unhandled Exception', ex), messageId, messageContext)
        }
        respond HttpStatus.INTERNAL_SERVER_ERROR, messageId, messageContext, body
    }

    @Override
    String getMessageId(MessageContext messageContext) {
        messageContext.properties.messageId ?: messageContext.consumerTag
    }

    abstract T processMessage(T body, String messageId, MessageContext messageContext)
}