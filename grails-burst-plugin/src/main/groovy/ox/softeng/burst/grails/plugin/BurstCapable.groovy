package ox.softeng.burst.grails.plugin

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.validation.Errors
import ox.softeng.burst.grails.plugin.exception.BurstException

/**
 * @since 22/02/2016
 */
@CompileStatic
trait BurstCapable {

    Logger getLogger() {
        LoggerFactory.getLogger(getClass())
    }

    @Autowired
    BurstService burstService

    @Value('${info.app.name}')
    String source

    @Value('${burst.default.exception.title:BuRST Exception Message}')
    String defaultExceptionTitle

    @Value('${burst.default.error.title:BuRST Error Message}')
    String defaultErrorTitle

    @Value('${burst.default.information.title:BuRST Information Message}')
    String defaultInformationTitle

    void handleException(BurstException ex, String messageId, List<String> topics, Map<String, String> metadataMap) {
        String logMsg = "$messageId - Handling exception inside Burst capable object ${getClass().getSimpleName()}: ${ex.getMessage()}"
        if (ex.message.contains('Unhandled Exception'))
            logger.error(logMsg, ex.cause)
        else logger.error(logMsg)
        burstService.broadcastException ex, source, ex.getMessageTitle() ?: "$defaultExceptionTitle: Processing $messageId".toString(),
                                        messageId, topics, metadataMap
    }

    void handleErrors(Errors errors, String errorCode, String messageId, List<String> topics, Map<String, String> metadataMap) {
        handleErrors(errors, errorCode, messageId, "$defaultErrorTitle: Processing $messageId", topics, metadataMap)
    }

    void handleErrors(Errors errors, String errorCode, String messageId, String title, List<String> topics, Map<String, String> metadataMap) {
        logger.warn("{} - Handling error inside Burst capable object {} has {} errors", messageId, getClass().getSimpleName(), errors.errorCount)
        burstService.broadcastErrors errors, errorCode, source, title, messageId, topics, metadataMap
    }

    void handleNoIdException(BurstException ex, List<String> topics, Map<String, String> metadataMap) {
        String logMsg = "Handling exception inside Burst capable object ${getClass().getSimpleName()}: ${ex.getMessage()}"
        if (ex.message.contains('Unhandled Exception'))
            logger.error(logMsg, ex.cause)
        else logger.error(logMsg)
        burstService.broadcastException ex, source, topics, metadataMap
    }

    void handleNoIdErrors(Errors errors, String errorCode, List<String> topics, Map<String, String> metadataMap) {
        handleNoIdErrors(errors, errorCode, defaultErrorTitle, topics, metadataMap)
    }

    void handleNoIdErrors(Errors errors, String errorCode, String title, List<String> topics, Map<String, String> metadataMap) {
        logger.warn("Handling error inside Burst capable object {} has {} errors", getClass().getSimpleName(), errors.errorCount)
        burstService.broadcastErrors errors, errorCode, source, title, topics, metadataMap
    }

    void broadcastInformationMessage(String message, String messageId, List<String> topics, Map<String, String> metadataMap) {
        broadcastInformationMessage(message, messageId, "$defaultInformationTitle: Processing $messageId", topics, metadataMap)
    }

    void broadcastInformationMessage(String message, String messageId, String title, List<String> topics, Map<String, String> metadataMap) {
        logger.trace('{} - Sending information message to Burst: {}', messageId, message)
        burstService.broadcastInformationMessage(message, source, title, topics, metadataMap)
    }

    void broadcastNoticeMessage(String message, String messageId, List<String> topics, Map<String, String> metadataMap) {
        broadcastInformationMessage(message, messageId, "$defaultInformationTitle: Processing $messageId", topics, metadataMap)
    }

    void broadcastNoticeMessage(String message, String messageId, String title, List<String> topics, Map<String, String> metadataMap) {
        logger.trace('{} - Sending notice message to Burst: {}', messageId, message)
        burstService.broadcastNoticeMessage(message, source, title, topics, metadataMap)
    }
}
