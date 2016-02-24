package ox.softeng.burst.grails.plugin.rabbitmq.consumer

import com.budjb.rabbitmq.consumer.MessageContext
import groovy.util.slurpersupport.GPathResult
import ox.softeng.burst.grails.plugin.exception.BurstException

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

/**
 * @since 18/02/2016
 */
trait XmlResourceMessageConsumerBurstCapable<R> extends ResourceMessageConsumerBurstCapable<R> {

    GPathResult handleMessage(GPathResult body, MessageContext messageContext) {
        try {
            return processMessage(body, messageContext) as GPathResult
        } catch (BurstException ex) {
            handleException(ex, [:])
        } catch (Exception ex) {
            handleException(new BurstException('BURST01', 'Unhandled Exception', ex), [:])
        }
        respond INTERNAL_SERVER_ERROR, body
    }
}