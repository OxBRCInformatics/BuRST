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
import ox.softeng.burst.grails.plugin.exception.BurstException
import ox.softeng.burst.util.SeverityEnum
import ox.softeng.burst.xml.MessageDTO

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
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

    Marshaller getMarshaller() {
        Marshaller marshaller = JAXBContext.newInstance(MessageDTO.class).createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller
    }

    void broadcastMessage(MessageDTO message, boolean retry = true) {

        logger.debug("Broadcasting BuRST message: {}", message.title)
        logger.trace("{}", message)
        StringWriter writer
        try {
            writer = new StringWriter()
            getMarshaller().marshal(message, writer)

            rabbitMessagePublisher.send {
                routingKey = 'burst'
                body = writer.toString()
            }
        } catch (JAXBException jaxbEX) {
            if (retry && jaxbEX.cause.message.contains('com.rabbitmq.client.impl.LongStringHelper$ByteArrayLongString')) {
                logger.warn("Failed to send original message, stripping and sending clean version")
                MessageDTO bare = new MessageDTO()
                bare.dateTimeCreated = message.dateTimeCreated
                bare.details = message.details.toString()
                bare.severity = message.severity
                bare.source = message.source.toString()
                bare.title = message.title.toString()
                bare.metadata = message.metadata
                bare.topics.addAll(message.topics.collect {it.toString()})
                broadcastMessage(bare, false)
                return
            }
            logger.error("Failed to broadcast message '" + message.title, jaxbEX.cause)

        } catch (Exception ex) {
            logger.error("Failed to broadcast message '" + message.title, ex)
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

        logger.debug("Broadcasting errors:\n{}", details.toString())
        broadcastErrorMessage(details.toString(), source, title, topics, metadata)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    void broadcastSeverityMessage(SeverityEnum mSeverity, String message, String mSource, String mTitle, List<String> mTopics,
                                  Map<String, String> metadataMap = [:]) {
        broadcastMessage {
            dateTimeCreated = OffsetDateTime.now(ZoneId.of('UTC'))
            severity = mSeverity
            details = message.toString()
            source = mSource.toString()
            topics = mTopics.collect {it.toString()}
            metadataMap.each {k, v ->
                addToMetadata(k.toString(), v.toString() ?: 'unknown')
            }
            title = mTitle.toString()
            it
        }
    }
}
