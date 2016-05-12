package ox.softeng.burst.grails.plugin.rabbitmq.databinding

import com.budjb.rabbitmq.consumer.MessageContext
import grails.web.mime.MimeType
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.springframework.validation.BindingResult
import ox.softeng.burst.grails.plugin.rabbitmq.exception.UnknownMimeTypeException

/**
 * @since 19/02/2016
 */
@CompileStatic
trait RabbitDataBinder {

    abstract Logger getLogger()

    abstract String getMessageId(MessageContext messageContext)

    BindingResult bindData(target, bindingSource, MessageContext messageContext, Map includeExclude) {
        bindData target, bindingSource, messageContext, includeExclude, null
    }

    BindingResult bindData(target, bindingSource, MessageContext messageContext) {
        bindData target, bindingSource, messageContext, Collections.EMPTY_MAP, null
    }

    BindingResult bindData(target, bindingSource, MessageContext messageContext, String filter) {
        bindData target, bindingSource, messageContext, Collections.EMPTY_MAP, filter
    }

    BindingResult bindData(target, bindingSource, MessageContext messageContext, List excludes) {
        bindData target, bindingSource, messageContext, [exclude: excludes], null
    }

    BindingResult bindData(target, bindingSource, MessageContext messageContext, List excludes, String filter) {
        bindData target, bindingSource, messageContext, [exclude: excludes], filter
    }

    BindingResult bindData(target, bindingSource, MessageContext messageContext, Map includeExclude, String filter) {
        List includeList = convertToListIfCharSequence(includeExclude?.include)
        List excludeList = convertToListIfCharSequence(includeExclude?.exclude)

        String contentType = messageContext.properties.contentType
        MimeType mimeType = MimeType.configuredMimeTypes.find {it.name == contentType}

        if (mimeType == null) throw new UnknownMimeTypeException('ER01', contentType)
        logger.debug('{} Mime type of data to bind: {}', getMessageId(messageContext), mimeType)

        RabbitDataBindingUtils.bindObjectToInstance target, bindingSource, mimeType, includeList, excludeList, filter
    }

    private static List convertToListIfCharSequence(value) {
        List result = []
        if (value instanceof CharSequence) {
            result << (value instanceof String ? value : value.toString())
        }
        else if (value instanceof List) {
            result = (List) value
        }
        result
    }

}