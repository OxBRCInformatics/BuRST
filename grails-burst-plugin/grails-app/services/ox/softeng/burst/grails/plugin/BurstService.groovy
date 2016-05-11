package ox.softeng.burst.grails.plugin

import com.budjb.rabbitmq.publisher.RabbitMessagePublisher
import grails.compiler.GrailsCompileStatic
import grails.validation.ValidationErrors
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.validation.Errors
import ox.softeng.burst.domain.SeverityEnum
import ox.softeng.burst.grails.plugin.exception.BurstException
import ox.softeng.burst.xml.MessageDTO

import javax.xml.bind.JAXB
import java.time.OffsetDateTime
import java.time.ZoneId

@GrailsCompileStatic
class BurstService {

    static transactional = false

    Logger logger = LoggerFactory.getLogger(BurstService)

    @Autowired
    RabbitMessagePublisher rabbitMessagePublisher

    @Value('${info.app.name}')
    String appName

    @Value('${burst.strip.resource.name:false}')
    Boolean stripResourceNames

    @Value('${burst.source.application.organisation:Oxford BRC Informatics}')
    String organisation

    MessageSource messageSource

    void broadcastMessage(MessageDTO message) {

        StringWriter writer = new StringWriter()
        JAXB.marshal(message, writer)

        logger.debug("Broadcasting BuRST message")
        logger.trace("{}", message)
        rabbitMessagePublisher.send {
            routingKey = 'burst'
            body = writer.toString()
        }
    }

    void broadcastMessage(@DelegatesTo(MessageDTO) Closure<MessageDTO> closure) {
        broadcastMessage(new MessageDTO().with(closure))
    }

    void broadcastCriticalMessage(String message, String source, String title, List<String> topics, Map<String, String> metadata = [:]) {
        broadcastSeverityMessage(SeverityEnum.CRITICAL, message, source, title, topics, metadata)
    }

    void broadcastWarningMessage(String message, String source, String title, List<String> topics, Map<String, String> metadata = [:]) {
        broadcastSeverityMessage(SeverityEnum.WARNING, message, source, title, topics, metadata)
    }

    void broadcastErrorMessage(String message, String source, String title, List<String> topics, Map<String, String> metadata = [:]) {
        broadcastSeverityMessage(SeverityEnum.ERROR, message, source, title, topics, metadata)
    }

    void broadcastNoticeMessage(String message, String source, String title, List<String> topics, Map<String, String> metadata = [:]) {
        broadcastSeverityMessage(SeverityEnum.NOTICE, message, source, title, topics, metadata)
    }

    void broadcastInformationMessage(String message, String source, String title, List<String> topics, Map<String, String> metadata = [:]) {
        broadcastSeverityMessage(SeverityEnum.INFORMATIONAL, message, source, title, topics, metadata)
    }

    void broadcastException(BurstException ex, String title, List<String> topics, Map<String, String> metadata = [:]) {
        broadcastException(ex, appName, title, '????', topics, metadata)
    }

    void broadcastException(BurstException ex, String title, String id, List<String> topics, Map<String, String> metadata = [:]) {
        broadcastException(ex, appName, title, id, topics, metadata)
    }

    void broadcastException(BurstException ex, String source, String title, String id, List<String> exTopics,
                            Map<String, String> metadata = [:]) {
        broadcastCriticalMessage("Exception occurred inside $source while processing $id::\n\n" +
                                 "${ex.getMessage()}\n\n" +
                                 "Please contact ${organisation} and inform of the errorcode ${ex.errorCode} at time " +
                                 "${OffsetDateTime.now(ZoneId.of('UTC'))}",
                                 source, title, exTopics, metadata)
    }

    void broadcastErrors(Errors errors, String errorCode, String title, String id, List<String> topics, Map<String, String> metadata = [:]) {
        broadcastErrors(errors, errorCode, appName, title, id, topics, metadata)
    }

    String getResourceName(String fullResourceName) {
        stripResourceNames ? fullResourceName.replaceAll(/\w+\./, '') : fullResourceName
    }

    void broadcastErrors(Errors errors, String errorCode, String source, String title, String id, List<String> topics,
                         Map<String, String> metadata = [:]) {

        StringBuilder details = new StringBuilder("$errorCode - ")
        if (errors instanceof ValidationErrors) {
            details.append 'Validation '
        }

        details.append "Errors from $source while trying to process resource ${getResourceName(errors.objectName)} for $id::\n\n"

        errors.allErrors.each {error ->
            details.append "  - ${messageSource.getMessage(error, Locale.default)}\n"
        }

        broadcastErrorMessage(details.toString(), source, title, topics, metadata)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    void broadcastSeverityMessage(SeverityEnum mSeverity, String message, String mSource, String mTitle, List<String> mTopics,
                                  Map<String, String> metadata = [:]) {
        broadcastMessage {
            dateTimeCreated = OffsetDateTime.now(ZoneId.of('UTC'))
            severity = mSeverity
            details = message.toString()
            source = mSource.toString()
            topics = mTopics
            metadata.each {k, v ->
                addToMetadata(k.toString(), v.toString() ?: 'unknown')
            }
            title = mTitle.toString()
            it
        }
    }
}
