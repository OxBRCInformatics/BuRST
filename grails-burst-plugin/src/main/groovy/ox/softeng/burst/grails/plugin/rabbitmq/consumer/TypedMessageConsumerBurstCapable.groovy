package ox.softeng.burst.grails.plugin.rabbitmq.consumer

import com.budjb.rabbitmq.consumer.MessageContext
import org.springframework.http.HttpStatus
import ox.softeng.burst.grails.plugin.exception.BurstException

/**
 * @since 18/02/2016
 */
trait TypedMessageConsumerBurstCapable<T> extends MessageConsumerBurstCapable {

    T handleMessage(T body, MessageContext messageContext) {
        try {
            return processMessage(body, messageContext)
        } catch (BurstException ex) {
            handleException(ex, messageContext)
        } catch (Exception ex) {
            handleException(new BurstException('BURST03', 'Unhandled Exception', ex), messageContext)
        }
        respond HttpStatus.INTERNAL_SERVER_ERROR, body
    }

    abstract T processMessage(T body, MessageContext messageContext)
}