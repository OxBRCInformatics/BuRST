package ${packageName}


import ${packageName}.${className}
import grails.web.mime.MimeType
import ox.softeng.burst.grails.plugin.rabbitmq.consumer.ResourceMessageConsumerBurstCapable

class ${className}Consumer implements ResourceMessageConsumerBurstCapable <${className}> {

    static rabbitConfig = [
            : // TODO: Setup config.
    ]

    @Override
    List<MimeType> getContentTypes() {
        [MimeType.ALL]
    }

    @Override
    Map<String, String> extractRelevantMetadataFromGeneratedInstance(${className} instance ) {
        [:] //TODO extract metadata from instance
    }

    @Override
    List<String> getTopics() {
        [] //TODO add topics
    }
}