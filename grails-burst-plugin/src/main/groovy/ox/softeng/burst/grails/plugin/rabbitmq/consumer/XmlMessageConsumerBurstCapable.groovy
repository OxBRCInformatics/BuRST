package ox.softeng.burst.grails.plugin.rabbitmq.consumer

import com.budjb.rabbitmq.consumer.MessageContext
import grails.web.mime.MimeType
import groovy.util.slurpersupport.GPathResult
import ox.softeng.burst.grails.plugin.exception.BurstException
import ox.softeng.burst.grails.plugin.rabbitmq.exception.UnacceptableMimeTypeException

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE

/**
 * @since 18/02/2016
 */
trait XmlMessageConsumerBurstCapable extends MessageConsumerBurstCapable {

    List<MimeType> getContentTypes() {
        [MimeType.XML, MimeType.TEXT_XML]
    }

    GPathResult handleMessage(GPathResult body, MessageContext messageContext) {
        try {
            if (!acceptedContentType(messageContext)) {
                handleException(new UnacceptableMimeTypeException('BURST04', rabbitConfig.queue, messageContext.properties.contentType),
                                messageContext)
                return respond(NOT_ACCEPTABLE, body)
            }
            return processMessage(body, messageContext) as GPathResult
        } catch (BurstException ex) {
            handleException(ex, messageContext)
        } catch (Exception ex) {
            ex.printStackTrace()
            handleException(new BurstException('BURST05', 'Unhandled Exception', ex), messageContext)
        }
        respond INTERNAL_SERVER_ERROR, body
    }

    String handleMessage(String body, MessageContext messageContext) {
        try {
            if (!acceptedContentType(messageContext)) {
                handleException(new UnacceptableMimeTypeException('BURST06', rabbitConfig.queue, messageContext.properties.contentType),
                                messageContext)
                return respond(NOT_ACCEPTABLE, body)
            }
            return processMessage(body, messageContext) as String
        } catch (BurstException ex) {
            handleException(ex, messageContext)
        } catch (Exception ex) {
            ex.printStackTrace()
            handleException(new BurstException('BURST07', 'Unhandled Exception', ex), messageContext)
        }
        respond INTERNAL_SERVER_ERROR, body
    }

    abstract Object processMessage(Object body, MessageContext messageContext)
}