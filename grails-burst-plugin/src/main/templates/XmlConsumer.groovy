package $

{packageName}

import com.budjb.rabbitmq.consumer.MessageContext
import ox.softeng.burst.grails.plugin.rabbitmq.consumer.XmlMessageConsumerBurstCapable

class $ {
    className
}

Consumer implements XmlMessageConsumerBurstCapable {

    static rabbitConfig = [
            : // TODO: Setup config.
    ]

    @Override
    Object processMessage(Object body, MessageContext messageContext) {
        body //TODO process message. Will be either String or GPathResult
    }

    @Override
    List<String> getTopics() {
        [] //TODO add topics
    }
}