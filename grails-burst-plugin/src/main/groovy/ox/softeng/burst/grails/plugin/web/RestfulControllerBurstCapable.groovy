package ox.softeng.burst.grails.plugin.web

import grails.rest.RestfulController
import grails.transaction.Transactional
import grails.web.http.HttpHeaders
import org.springframework.validation.Errors
import ox.softeng.burst.grails.plugin.BurstCapable
import ox.softeng.burst.grails.plugin.exception.BurstException

import static org.springframework.http.HttpStatus.*

/**
 * @since 24/02/2016
 */
abstract class RestfulControllerBurstCapable<T> extends RestfulController<T> implements BurstCapable {

    RestfulControllerBurstCapable(Class<T> resource) {
        super(resource)
    }

    RestfulControllerBurstCapable(Class<T> resource, boolean readOnly) {
        super(resource, readOnly)
    }

    abstract Map<String, String> extractRelevantMetadataFromGeneratedInstance(T instance)

    /**
     * Saves a resource
     */
    @Transactional
    def save() {
        if (handleReadOnly()) {
            return
        }
        def instance = createResource()

        // Binding failed so we have nothing to extract for metadata
        if (instance instanceof Errors) {
            handleErrors(instance, 'VAL03', [:])
            return respond(UNPROCESSABLE_ENTITY, object)
        }

        instance.validate()

        if (instance.hasErrors()) {
            handleErrors instance.errors as Errors, 'VAL04', extractRelevantMetadataFromGeneratedInstance(instance)
            transactionStatus.setRollbackOnly()
            respond instance.errors, view: 'create' // STATUS CODE 422
            return
        }

        try {
            saveResource instance
        } catch (RuntimeException exception) {
            BurstException burstException = new BurstException('BURST03', 'Failed to save resource', exception)
            ((Errors) instance.errors).reject('failed.save', [burstException.message] as Object[], 'Failed to save resource because {0}')
            handleException(burstException, extractRelevantMetadataFromGeneratedInstance(instance as T))
            respond instance.errors, view: 'edit', status: 500
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message',
                                        args: [message(code: "${resourceName}.label".toString(), default: resourceClassName), instance.id])
                redirect instance
            }
            '*' {
                response.addHeader(HttpHeaders.LOCATION,
                                   grailsLinkGenerator.link(resource: this.controllerName, action: 'show', id: instance.id, absolute: true,
                                                            namespace: hasProperty('namespace') ? this.namespace : null))
                respond instance, [status: CREATED]
            }
        }
    }

    /**
     * Updates a resource for the given id
     * @param id
     */
    @Transactional
    def update() {
        if (handleReadOnly()) {
            return
        }

        T instance = queryForResource(params.id)
        if (instance == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        instance.properties = getObjectToBind()

        if (instance.hasErrors()) {
            handleErrors instance.errors as Errors, 'VAL04', extractRelevantMetadataFromGeneratedInstance(instance)
            transactionStatus.setRollbackOnly()
            respond instance.errors, view: 'edit' // STATUS CODE 422
            return
        }

        try {
            updateResource instance
        } catch (RuntimeException exception) {
            BurstException burstException = new BurstException('BURST04', 'Failed to update resource', exception)
            ((Errors) instance.errors).reject('failed.save', [burstException.message] as Object[], 'Failed to save resource because {0}')
            handleException(burstException, extractRelevantMetadataFromGeneratedInstance(instance as T))
            respond instance.errors, view: 'edit', status: 500
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message',
                                        args: [message(code: "${resourceClassName}.label".toString(), default: resourceClassName), instance.id])
                redirect instance
            }
            '*' {
                response.addHeader(HttpHeaders.LOCATION,
                                   grailsLinkGenerator.link(resource: this.controllerName, action: 'show', id: instance.id, absolute: true,
                                                            namespace: hasProperty('namespace') ? this.namespace : null))
                respond instance, [status: OK]
            }
        }
    }

    /**
     * Creates a new instance of the resource.  If the request
     * contains a body the body will be parsed and used to
     * initialize the new instance, otherwise request parameters
     * will be used to initialized the new instance.
     *
     * @return The resource instance
     */
    protected def createResource() {
        T instance = resource.newInstance()
        def result = bindData instance, getObjectToBind()
        if (result) return result
        instance
    }

}
