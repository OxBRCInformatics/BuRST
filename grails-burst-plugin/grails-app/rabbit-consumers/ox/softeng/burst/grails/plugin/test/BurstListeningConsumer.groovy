package ox.softeng.burst.grails.plugin.test

import com.budjb.rabbitmq.consumer.MessageContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BurstListeningConsumer {

    static rabbitConfig = [
            queue: 'burst'
    ]

    Logger logger = LoggerFactory.getLogger(BurstListeningConsumer)

    /**
     * Handle an incoming RabbitMQ message.
     *
     * @param body The converted body of the incoming message.
     * @param context Properties of the incoming message.
     * @return
     */
    String handleMessage(String body, MessageContext messageContext) {
        logger.error 'Burst seeing message {}', body
        body
    }
}