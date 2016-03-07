package ${packageName}

import ${packageName}.${className}
import ox.softeng.burst.grails.plugin.rabbitmq.consumer.XmlResourceMessageConsumerBurstCapable

class ${className}Consumer extends XmlResourceMessageConsumerBurstCapable<${className}> {

    static rabbitConfig = [
            : // TODO: Setup config.
    ]

    @Override
    Map<String, String> extractRelevantMetadataFromGeneratedInstance(${className} instance ) {
        [:] //TODO extract metadata from instance
    }

    @Override
    List<String> getTopics() {
        [] //TODO add topics
    }
}