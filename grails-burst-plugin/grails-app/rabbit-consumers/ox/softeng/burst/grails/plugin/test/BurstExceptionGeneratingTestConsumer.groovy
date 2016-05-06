package ox.softeng.burst.grails.plugin.test

import com.budjb.rabbitmq.consumer.MessageContext
import ox.softeng.burst.grails.plugin.exception.BurstException
import ox.softeng.burst.grails.plugin.rabbitmq.consumer.TypedMessageConsumerBurstCapable

class BurstExceptionGeneratingTestConsumer implements TypedMessageConsumerBurstCapable<String> {

    static rabbitConfig = [
            queue: 'exception'
    ]

    Class<Test> resource = Test

    String processMessage(String body, String messageId,MessageContext messageContext) {
        logger.debug('Seeing message "{}", and now ...', body)
        if (body ==~ /Unhandled Exception fail/)
            throw new IllegalArgumentException('Am uncontrollably failing because that\'s what i do')
        if (body ==~ /Handled Exception fail/)
            throw new BurstException('INT01', 'Am failing because that\'s what i do')
        if (body ==~ /Nested Exception fail/)
            throw new BurstException('INT02', 'Am failing because that\'s what i do',
                                     new IllegalArgumentException('Illegal arg',
                                                                  new IllegalAccessException('Illegal Access')))

        null

    }

    @Override
    List<String> getContextTopics(MessageContext messageContext) {
        ['Test', 'Exceptions']
    }
}