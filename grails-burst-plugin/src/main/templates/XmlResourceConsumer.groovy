package $

import $
import ox.softeng.burst.grails.plugin.rabbitmq.consumer.XmlResourceMessageConsumerBurstCapable

{packageName}
{packageName}.$ {className}

class $ {
    className
}

Consumer implements XmlResourceMessageConsumerBurstCapable < $ {className} > {

    static rabbitConfig = [
            : // TODO: Setup config.
    ]

    @Override
    Map<String, String> extractRelevantMetadataFromGeneratedInstance($ {className} instance ) {
        [:] //TODO extract metadata from instance
    }

    @Override
    List<String> getTopics() {
        [] //TODO add topics
    }
}