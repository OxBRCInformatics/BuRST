package ox.softeng.burst.grails.plugin.rabbitmq.consumer

import com.budjb.rabbitmq.consumer.MessageContext
import org.springframework.validation.Errors
import ox.softeng.burst.grails.plugin.exception.BurstException
import ox.softeng.burst.grails.plugin.rabbitmq.databinding.RabbitDataBinder
import ox.softeng.burst.grails.plugin.rabbitmq.exception.UnacceptableMimeTypeException

import java.lang.reflect.ParameterizedType

import static org.springframework.http.HttpStatus.*

/**
 * @since 24/02/2016
 */
trait ResourceMessageConsumerBurstCapable<R> extends MessageConsumerBurstCapable implements RabbitDataBinder {

    abstract Map<String, String> extractRelevantMetadataFromGeneratedInstance(R instance)

    String handleMessage(String body, MessageContext messageContext) {
        try {
            if (!acceptedContentType(messageContext)) {
                handleException(new UnacceptableMimeTypeException('BURST08', rabbitConfig.queue, messageContext.properties.contentType),
                                messageContext)
                return respond(NOT_ACCEPTABLE, body)
            }
            return processMessage(body, messageContext) as String
        } catch (BurstException ex) {
            handleException(ex, messageContext)
        } catch (Exception ex) {
            handleException(new BurstException('BURST09', 'Unhandled Exception', ex), messageContext)
        }
        respond INTERNAL_SERVER_ERROR, body
    }

    /**
     * Default process action is to save the object.
     * Response will be in the correct type as XML with a HTTP status code and reason in the body.
     */
    def processMessage(def body, MessageContext messageContext) {
        logger.debug('{} Processing message body of type "{}"', messageContext.consumerTag, body.getClass())
        save(body, messageContext)
    }

    def save(def object, MessageContext messageContext) {
        logger.debug('{} Saving new object with content-type "{}"', messageContext.consumerTag, messageContext.properties.contentType)
        def instance = createResource object, messageContext

        // Binding failed so we have nothing to extract for metadata
        if (instance instanceof Errors) {
            handleErrors(instance, 'BURSTVAL01', messageContext)
            return respond(UNPROCESSABLE_ENTITY, object)
        }

        logger.debug('{} Instance of type "{}" created', messageContext.consumerTag, instance.class)

        instance.validate()
        // If errors here then object has data but does not pass validation constraints
        if (instance.hasErrors()) {
            handleErrors instance.errors as Errors, 'BURSTVAL02', messageContext, extractRelevantMetadataFromGeneratedInstance(instance as R)
            return respond(UNPROCESSABLE_ENTITY, object)
        }
        logger.debug('{} Instance validated', messageContext.consumerTag)

        try {
            saveResource instance as R
        } catch (RuntimeException exception) {
            BurstException burstException = new BurstException('BURST10', 'Failed to save resource', exception)
            handleException(burstException, messageContext, extractRelevantMetadataFromGeneratedInstance(instance as R))
            return respond(INTERNAL_SERVER_ERROR, object)
        }

        logger.debug('{} Instance saved', messageContext.consumerTag)

        respond(CREATED, object)
    }

    def createResource(def objectToBind, MessageContext context) {
        R instance = getType().newInstance()
        def result = bindData instance, objectToBind, context
        if (result) return result
        instance
    }

    /**
     * Saves a resource
     *
     * @param resource The resource to be saved
     * @return The saved resource or null if can't save it
     */
    R saveResource(R resource) {
        resource.save flush: true
    }

    Class<R> getType() {
        ((ParameterizedType) this.class.genericInterfaces.find {it instanceof ParameterizedType}).actualTypeArguments[0] as Class
    }
}
