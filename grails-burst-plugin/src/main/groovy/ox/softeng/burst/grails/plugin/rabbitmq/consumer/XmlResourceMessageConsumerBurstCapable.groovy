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
        try {
            if (!acceptedContentType(messageContext)) {
                handleException(new UnacceptableMimeTypeException('BURST11', rabbitConfig.queue, messageContext.properties.contentType),
                                messageContext)
                return respond(NOT_ACCEPTABLE, body)
            }
            return processMessage(body, messageContext) as GPathResult
        } catch (BurstException ex) {
            handleException(ex, messageContext)
        } catch (Exception ex) {
            handleException(new BurstException('BURST12', 'Unhandled Exception', ex), messageContext)
        }
        respond INTERNAL_SERVER_ERROR, body
    }
}