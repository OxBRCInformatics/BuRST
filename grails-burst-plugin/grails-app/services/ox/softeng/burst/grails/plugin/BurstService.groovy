package ox.softeng.burst.grails.plugin

import com.budjb.rabbitmq.publisher.RabbitMessagePublisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.validation.Errors
import ox.softeng.burst.domain.subscription.SeverityEnum
import ox.softeng.burst.grails.plugin.exception.BurstException
import ox.softeng.burst.xml.MessageDTO

import javax.xml.bind.JAXB
import java.time.OffsetDateTime
import java.time.ZoneId

class BurstService {

    static transactional = false

    Logger logger = LoggerFactory.getLogger(BurstService)

    @Autowired
    RabbitMessagePublisher rabbitMessagePublisher

    @Value('${info.app.name}')
    String appName

    MessageSource messageSource

    void broadcastMessage(MessageDTO message) {

        StringWriter writer = new StringWriter()
        JAXB.marshal(message, writer)

        logger.info("Broadcasting BuRST message")
        logger.debug("{}", message)
        rabbitMessagePublisher.send {
            routingKey = 'burst'
            body = writer.toString()
        }
    }

    void broadcastMessage(@DelegatesTo(MessageDTO) Closure<MessageDTO> closure) {
        broadcastMessage(new MessageDTO().with(closure))
    }

    void broadcastInformationMessage(String message, String mSource,List<String> mTopics, Map<String, String> metadataMap = [:]){
        broadcastMessage {
            dateTimeCreated = OffsetDateTime.now(ZoneId.of('UTC'))
            severity = SeverityEnum.INFORMATIONAL
            details = message
            source = mSource
            topics = mTopics
            metadataMap.each {k, v ->
                addToMetadata(k, v ?: 'unknown')
            }
            it
        }
    }

    void broadcastException(BurstException ex, List<String> topics, Map<String, String> metadataMap = [:]) {
        broadcastException(ex, appName, topics, metadataMap)
    }

    void broadcastException(BurstException ex, String exSource, List<String> exTopics, Map<String, String> metadataMap = [:]) {
        broadcastMessage {
            dateTimeCreated = OffsetDateTime.now(ZoneId.of('UTC'))
            severity = SeverityEnum.CRITICAL
            details = ex.getMessage()
            source = exSource
            topics = exTopics
            metadataMap.each {k, v ->
                addToMetadata(k, v ?: 'unknown')
            }
            it
        }
    }

    void broadcastErrors(Errors errors, String errorCode, List<String> topics, Map<String, String> metadataMap = [:]) {
        broadcastErrors(errors, errorCode, appName, topics, metadataMap)
    }

    void broadcastErrors(Errors errors, String errorCode, String exSource, List<String> exTopics, Map<String, String> metadataMap = [:]) {
        String exDetails = "$errorCode - Errors while trying to process '${errors.objectName}' resource::\n"

        errors.allErrors.each {error ->
            exDetails += "  ${messageSource.getMessage(error, Locale.default)}\n"
        }

        broadcastMessage {
            dateTimeCreated = OffsetDateTime.now(ZoneId.of('UTC'))
            severity = SeverityEnum.ERROR
            details = exDetails
            source = exSource
            topics = exTopics
            metadataMap.each {k, v ->
                addToMetadata(k, v ?: 'unknown')
            }
            it
        }
    }
}
