package ox.softeng.burst.grails.plugin.rabbitmq.consumer

import com.budjb.rabbitmq.consumer.MessageContext
import ox.softeng.burst.grails.plugin.BurstCapable
import ox.softeng.burst.grails.plugin.exception.BurstException

/**
 * @since 18/02/2016
 */
trait MessageConsumerBurstCapable<T> implements BurstCapable {

    T handleMessage(T body, MessageContext messageContext) {
        try {
            return processMessage(body, messageContext)
        } catch (BurstException ex) {
            handleException(ex, [:])
        } catch (Exception ex) {
            handleException(new BurstException('BURST01', 'Unhandled Exception', ex), [:])
        }
        null
    }

    abstract T processMessage(T body, MessageContext messageContext)
}