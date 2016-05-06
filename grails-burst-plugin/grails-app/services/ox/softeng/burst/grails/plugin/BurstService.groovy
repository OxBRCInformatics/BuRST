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

        logger.info("Broadcasting BuRST message")
        logger.trace("{}", message)
        rabbitMessagePublisher.send {
            routingKey = 'burst'
            body = writer.toString()
        }
    }

    void broadcastMessage(@DelegatesTo(MessageDTO) Closure<MessageDTO> closure) {
        broadcastMessage(new MessageDTO().with(closure))
    }

    void broadcastNoticeMessage(String message, String mSource, String mTitle, List<String> mTopics, Map<String, String> metadataMap = [:]) {
        broadcastSeverityMessage(SeverityEnum.NOTICE, message, mSource, mTitle, mTopics, metadataMap)
    }

    void broadcastInformationMessage(String message, String mSource, String mTitle, List<String> mTopics, Map<String, String> metadataMap = [:]) {
        broadcastSeverityMessage(SeverityEnum.INFORMATIONAL, message, mSource, mTitle, mTopics, metadataMap)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    void broadcastSeverityMessage(SeverityEnum mSeverity, String message, String mSource, String mTitle, List<String> mTopics,
                                  Map<String, String> metadataMap = [:]) {
        broadcastMessage {
            dateTimeCreated = OffsetDateTime.now(ZoneId.of('UTC'))
            severity = mSeverity
            details = message
            source = mSource
            topics = mTopics
            metadataMap.each {k, v ->
                addToMetadata(k, v ?: 'unknown')
            }
            title = mTitle
            it
        }
    }

    void broadcastException(BurstException ex, String title, List<String> topics, Map<String, String> metadataMap = [:]) {
        broadcastException(ex, appName, title, '????', topics, metadataMap)
    }

    void broadcastException(BurstException ex, String title, String id, List<String> topics, Map<String, String> metadataMap = [:]) {
        broadcastException(ex, appName, title, id, topics, metadataMap)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    void broadcastException(BurstException ex, String exSource, String exTitle, String id, List<String> exTopics,
                            Map<String, String> metadataMap = [:]) {
        OffsetDateTime odt = OffsetDateTime.now(ZoneId.of('UTC'))
        broadcastMessage {
            dateTimeCreated = odt
            severity = SeverityEnum.CRITICAL
            details = "Exception occurred inside $exSource while processing $id::\n\n" +
                      "${ex.getMessage()}\n\n" +
                      "Please contact ${organisation} and inform of the errorcode ${ex.errorCode} at time ${odt}"
            source = exSource
            topics = exTopics
            metadataMap.each {k, v ->
                addToMetadata(k, v ?: 'unknown')
            }
            title = exTitle
            it
        }
    }

    void broadcastErrors(Errors errors, String errorCode, String title, String id, List<String> topics, Map<String, String> metadataMap = [:]) {
        broadcastErrors(errors, errorCode, appName, title, id, topics, metadataMap)
    }

    String getResourceName(String fullResourceName) {
        stripResourceNames ? fullResourceName.replaceAll(/\w+\./, '') : fullResourceName
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    void broadcastErrors(Errors errors, String errorCode, String exSource, String exTitle, String id, List<String> exTopics,
                         Map<String, String> metadataMap = [:]) {

        StringBuilder exDetails = new StringBuilder("$errorCode - ")
        if (errors instanceof ValidationErrors) {
            exDetails.append 'Validation '
        }

        exDetails.append "Errors from $exSource while trying to process resource ${getResourceName(errors.objectName)} for $id::\n\n"

        errors.allErrors.each {error ->
            exDetails.append "  - ${messageSource.getMessage(error, Locale.default)}\n"
        }

        broadcastMessage {
            dateTimeCreated = OffsetDateTime.now(ZoneId.of('UTC'))
            severity = SeverityEnum.ERROR
            details = exDetails.toString()
            source = exSource
            topics = exTopics
            metadataMap.each {k, v ->
                addToMetadata(k, v ?: 'unknown')
            }
            title = exTitle
            it
        }
    }
}
