package ox.softeng.burst.grails.plugin.rabbitmq.consumer

import com.budjb.rabbitmq.consumer.MessageContext
import grails.web.mime.MimeType
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import org.springframework.http.HttpStatus
import org.springframework.validation.Errors
import ox.softeng.burst.grails.plugin.BurstCapable
import ox.softeng.burst.grails.plugin.exception.BurstException

/**
 * @since 01/03/2016
 */
trait MessageConsumerBurstCapable extends BurstCapable {

    List<MimeType> getContentTypes() {
        [MimeType.ALL]
    }

    def respond(HttpStatus status, def object) {

        if (object instanceof Number) {
            return status.value()
        }

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.response "${status.value()} ${status.reasonPhrase}"

        String response = writer.toString()
        logger.debug('Response: {}', response)

        if (object instanceof GPathResult) return new XmlSlurper().parseText(response)
        try {
            return response.asType(object.class)
        } catch (Exception ignored) {}
        null
    }

    boolean acceptedContentType(MessageContext messageContext) {
        getContentTypes().any {
            it == MimeType.ALL ?: messageContext.properties.contentType == it.name
        }
    }

    Map<String, String> getDefaultMetadata(MessageContext messageContext) {
        [
                queue      : messageContext.envelope.routingKey,
                consumerTag: messageContext.consumerTag,
                messageId  : messageContext.properties.messageId,
                timestamp  : messageContext.properties.timestamp.toString(),
                application: messageContext.properties.appId

        ]
    }

    void handleException(BurstException ex, MessageContext messageContext) {
        super.handleException(ex, getDefaultMetadata(messageContext))
    }

    void handleErrors(Errors errors, String errorCode, MessageContext messageContext) {
        super.handleErrors(errors, errorCode, getDefaultMetadata(messageContext))
    }

    void handleException(BurstException ex, MessageContext messageContext, Map<String, String> metadataMap) {
        super.handleException(ex, getDefaultMetadata(messageContext) + metadataMap)
    }

    void handleErrors(Errors errors, String errorCode, MessageContext messageContext, Map<String, String> metadataMap) {
        super.handleErrors(errors, errorCode, getDefaultMetadata(messageContext) + metadataMap)
    }

    /*
    Use the methods which take a message context
     */

    @Deprecated
    @Override
    void handleErrors(Errors errors, String errorCode, Map<String, String> metadataMap) {
        super.handleErrors(errors, errorCode, metadataMap)
    }

    /*
    Use the methods which take a message context
     */

    @Deprecated
    @Override
    void handleException(BurstException ex, Map<String, String> metadataMap) {
        super.handleException(ex, metadataMap)
    }
}
