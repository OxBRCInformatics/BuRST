package ox.softeng.burst.grails.plugin

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.validation.Errors
import ox.softeng.burst.grails.plugin.exception.BurstException

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

    void handleException(BurstException ex, Map<String, String> metadataMap) {
        logger.debug("Handling exception inside Burst Capable object {}. {}", getClass(), ex.getMessage())
        burstService.broadcastException ex, source, topics, metadataMap
    }

    void handleErrors(Errors errors, String errorCode, Map<String, String> metadataMap) {
        logger.debug("Handling error inside Burst Capable object {} has {} errors", getClass(), errors.errorCount)
        burstService.broadcastErrors errors, errorCode, source, topics, metadataMap
    }

    abstract List<String> getTopics()

}
