package ${packageName}

import ${packageName}.${className}
import com.budjb.rabbitmq.consumer.MessageContext
import grails.web.mime.MimeType
import ox.softeng.burst.grails.plugin.rabbitmq.consumer.TypedMessageConsumerBurstCapable

class ${className}Consumer implements TypedMessageConsumerBurstCapable<${className}> {

    static rabbitConfig = [
            : // TODO: Setup config.
    ]

    @Override
    List<MimeType> getContentTypes() {
        [MimeType.ALL]
    }

    @Override
    List<String> getTopics() {
        [] //TODO add topics
    }

    @Override
    ${className} processMessage(${className} body, MessageContext messageContext) {
        return body //TODO process message body
    }
}