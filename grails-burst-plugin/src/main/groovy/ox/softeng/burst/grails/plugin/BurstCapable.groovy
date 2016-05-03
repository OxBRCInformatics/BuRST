package ox.softeng.burst.grails.plugin

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.validation.Errors
import ox.softeng.burst.domain.SeverityEnum
import ox.softeng.burst.grails.plugin.exception.BurstException

import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * @since 22/02/2016
 */
trait BurstCapable {

    Logger getLogger() {
        LoggerFactory.getLogger(getClass())
    }

    @Autowired
    BurstService burstService

    @Value('${info.app.name}')
    String source

    void handleException(BurstException ex, String messageId, Map<String, String> metadataMap) {
        String logMsg = "$messageId - Handling exception inside Burst capable object ${getClass().getCanonicalName()}: ${ex.getMessage()}"
        if(ex.message.contains('Unhandled Exception'))
            logger.error(logMsg, ex.cause)
        else logger.error(logMsg)
        burstService.broadcastException ex, source, topics, metadataMap
    }

    void handleErrors(Errors errors, String errorCode, String messageId, Map<String, String> metadataMap) {
        logger.warn("{} - Handling error inside Burst capable object {} has {} errors", messageId, getClass().getCanonicalName(), errors.errorCount)
        burstService.broadcastErrors errors, errorCode, source, topics, metadataMap
    }

    void handleException(BurstException ex, Map<String, String> metadataMap) {
        String logMsg = "Handling exception inside Burst capable object ${getClass().getCanonicalName()}: ${ex.getMessage()}"
        if(ex.message.contains('Unhandled Exception'))
            logger.error(logMsg, ex.cause)
        else logger.error(logMsg)
        burstService.broadcastException ex, source, topics, metadataMap
    }

    void handleErrors(Errors errors, String errorCode, Map<String, String> metadataMap) {
        logger.warn("Handling error inside Burst capable object {} has {} errors", getClass().getCanonicalName(), errors.errorCount)
        burstService.broadcastErrors errors, errorCode, source, topics, metadataMap
    }

    void broadcastInformationMessage(String message, String messageId, Map<String, String> metadataMap){
        logger.trace('{} - Sending information message to Burst: {}', messageId, message)
        burstService.broadcastInformationMessage(message, source, getTopics(), metadataMap)
    }

    abstract List<String> getTopics()

}
