package ox.softeng.burst.grails.plugin.rabbitmq.consumer

import com.budjb.rabbitmq.consumer.MessageContext
import grails.web.mime.MimeType
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import org.springframework.http.HttpStatus
import org.springframework.validation.Errors
import ox.softeng.burst.domain.SeverityEnum
import ox.softeng.burst.grails.plugin.BurstCapable
import ox.softeng.burst.grails.plugin.exception.BurstException

import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * @since 01/03/2016
 */
trait MessageConsumerBurstCapable extends BurstCapable {

    List<MimeType> getContentTypes() {
        [MimeType.ALL]
    }

    def respond(HttpStatus status, String messageId, MessageContext messageContext, def object) {
        respond(status, messageId, object, getDefaultMetadata(messageContext))
    }

    def respond(HttpStatus status, String messageId, MessageContext messageContext, def object, Map<String, String> metadataMap) {
        respond(status, messageId, object, getDefaultMetadata(messageContext) + metadataMap)
    }

    def respond(HttpStatus status, String messageId, def object, Map<String, String> metadataMap) {

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.response "${status.value()} ${status.reasonPhrase}"

        String response = writer.toString()
        logger.debug('{} - Response: {}', messageId, response)
        broadcastInformationMessage(response, messageId,metadataMap)

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
        Map md = [
                queue      : messageContext.envelope.routingKey,
                consumerTag: messageContext.consumerTag,
                messageId  : messageContext.properties.messageId,
                timestamp  : messageContext.properties.timestamp.toString(),
                application: messageContext.properties.appId

        ]
        messageContext.properties.headers.each {k, v ->
            md.put(k, v as String)
        }
        md
    }

    void handleException(BurstException ex, String messageId, MessageContext messageContext) {
        handleException(ex, messageId, getDefaultMetadata(messageContext))
    }

    void handleErrors(Errors errors, String errorCode, String messageId, MessageContext messageContext) {
        handleErrors(errors, errorCode, messageId, getDefaultMetadata(messageContext))
    }

    void handleException(BurstException ex, String messageId, MessageContext messageContext, Map<String, String> metadataMap) {
        handleException(ex, messageId, getDefaultMetadata(messageContext) + metadataMap)
    }

    void handleErrors(Errors errors, String errorCode, String messageId, MessageContext messageContext, Map<String, String> metadataMap) {
        handleErrors(errors, errorCode, messageId, getDefaultMetadata(messageContext) + metadataMap)
    }
}
