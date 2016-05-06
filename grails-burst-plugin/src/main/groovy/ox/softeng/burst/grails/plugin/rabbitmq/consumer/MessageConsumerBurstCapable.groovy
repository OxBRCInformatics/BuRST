package ox.softeng.burst.grails.plugin.rabbitmq.consumer

import com.budjb.rabbitmq.consumer.MessageContext
import grails.web.mime.MimeType
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import org.springframework.http.HttpStatus
import org.springframework.validation.Errors
import ox.softeng.burst.grails.plugin.BurstCapable
import ox.softeng.burst.grails.plugin.exception.BurstException

import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * @since 01/03/2016
 */
@CompileStatic
trait MessageConsumerBurstCapable extends BurstCapable {

    List<MimeType> getContentTypes() {
        [MimeType.ALL]
    }

    abstract List<String> getContextTopics(MessageContext messageContext)

    abstract String getMessageId(MessageContext messageContext)

    def respond(HttpStatus status, String messageId, MessageContext messageContext, def object) {
        respond(status, messageId, object, getContextTopics(messageContext), getDefaultMetadata(messageContext))
    }

    def respond(HttpStatus status, String messageId, MessageContext messageContext, def object, Map<String, String> metadataMap) {
        respond(status, messageId, object, getContextTopics(messageContext), getDefaultMetadata(messageContext) + metadataMap)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    def respond(HttpStatus status, String messageId, def object, List<String> topics, Map<String, String> metadataMap) {

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.response "${status.value()} ${status.reasonPhrase}"

        String title
        String message
        if (status.'1xxInformational' || status.'2xxSuccessful') {
            title = "$messageId SUCCESSFULLY processed at ${OffsetDateTime.now(ZoneId.of('UTC'))}"
            message = "$messageId was successfully ${status.reasonPhrase}"
            logger.debug('{} - Response: {}', messageId, "${status.value()} ${status.reasonPhrase}")
        }
        else {
            title = "$messageId FAILED processing at ${OffsetDateTime.now(ZoneId.of('UTC'))}"
            message = "$messageId failed processing due to '${status.reasonPhrase}'. Please see other messages to determine why."
            logger.warn('{} - Response: {}', messageId, "${status.value()} ${status.reasonPhrase}")
        }

        broadcastNoticeMessage message, messageId, title, topics, metadataMap

        String response = writer.toString()
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
                timestamp  : messageContext.properties.timestamp.toString() ?: OffsetDateTime.now(ZoneId.of('UTC')),
                application: messageContext.properties.appId ?: source

        ]
        messageContext.properties.headers.each {k, v ->
            md.put(k, v as String)
        }
        md as Map<String, String>
    }

    void handleException(BurstException ex, String messageId, MessageContext messageContext) {
        handleException(ex, messageId, getContextTopics(messageContext), getDefaultMetadata(messageContext))
    }

    void handleException(BurstException ex, String messageId, MessageContext messageContext, Map<String, String> metadataMap) {
        handleException(ex, messageId, getContextTopics(messageContext), getDefaultMetadata(messageContext) + metadataMap)
    }

    void handleErrors(Errors errors, String errorCode, String messageId, String title, MessageContext messageContext) {
        handleErrors(errors, errorCode, messageId, title, getContextTopics(messageContext), getDefaultMetadata(messageContext))
    }

    void handleErrors(Errors errors, String errorCode, String messageId, MessageContext messageContext) {
        handleErrors(errors, errorCode, messageId, getContextTopics(messageContext), getDefaultMetadata(messageContext))
    }

    void handleErrors(Errors errors, String errorCode, String messageId, String title, MessageContext messageContext,
                      Map<String, String> metadataMap) {
        handleErrors(errors, errorCode, messageId, title, getContextTopics(messageContext), getDefaultMetadata(messageContext) + metadataMap)
    }

    void handleErrors(Errors errors, String errorCode, String messageId, MessageContext messageContext, Map<String, String> metadataMap) {
        handleErrors(errors, errorCode, messageId, getContextTopics(messageContext), getDefaultMetadata(messageContext) + metadataMap)
    }
}
