package $

{packageName}

import com.budjb.rabbitmq.consumer.MessageContext
import grails.web.mime.MimeType
import ox.softeng.burst.grails.plugin.rabbitmq.consumer.MessageConsumerBurstCapable

class $ {
    className
}

Consumer implements MessageConsumerBurstCapable {

    static rabbitConfig = [
            : // TODO: Setup config.
    ]

    @Override
    List<MimeType> getContentTypes() {
        [MimeType.ALL]
    }

    /**
     * Handle an incoming RabbitMQ message.
     *
     * @param body The converted body of the incoming message.
     * @param context Properties of the incoming message.
     * @return
     */
    def handleMessage(def body, MessageContext messageContext) {
        // TODO: Handle messages
        body
    }

    @Override
    List<String> getTopics() {
        [] //TODO add topics
    }
}