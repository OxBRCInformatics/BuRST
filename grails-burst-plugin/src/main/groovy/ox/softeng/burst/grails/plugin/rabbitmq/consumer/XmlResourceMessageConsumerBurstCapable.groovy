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
abstract class XmlResourceMessageConsumerBurstCapable<R> extends ResourceMessageConsumerBurstCapable<R> {

    List<MimeType> getContentTypes() {
        [MimeType.XML, MimeType.TEXT_XML]
    }

    GPathResult handleMessage(GPathResult body, MessageContext messageContext) {
        String messageId = messageContext.properties.messageId ?: messageContext.consumerTag
        try {
            if (!acceptedContentType(messageContext)) {
                handleException(new UnacceptableMimeTypeException('BURST11', rabbitConfig.queue, messageContext.properties.contentType),
                                messageId, messageContext)
                return respond(NOT_ACCEPTABLE, messageId, messageContext, body) as GPathResult
            }
            return processMessage(body, messageId, messageContext) as GPathResult
        } catch (BurstException ex) {
            handleException(ex, messageId, messageContext)
        } catch (Exception ex) {
            handleException(new BurstException('BURST12', 'Unhandled Exception', ex), messageId, messageContext)
        }
        respond INTERNAL_SERVER_ERROR, messageId, messageContext, body
    }
}