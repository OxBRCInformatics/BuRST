package ox.softeng.burst.grails.plugin.test

import com.budjb.rabbitmq.consumer.MessageContext
import org.hibernate.HibernateException
import org.hibernate.JDBCException
import ox.softeng.burst.grails.plugin.rabbitmq.consumer.XmlResourceMessageConsumerBurstCapable

import java.sql.SQLException

class BurstValidationConsumer extends XmlResourceMessageConsumerBurstCapable<Test> {

    static rabbitConfig = [
            queue: 'validation'
    ]

    @Override
    List<String> getContextTopics(MessageContext messageContext) {
        ['Test', 'validation']
    }

    /**
     * Saves a resource
     *
     * @param resource The resource to be saved
     * @return The saved resource or null if can't save it
     */
    @Override
    Map<String, String> extractRelevantMetadataFromGeneratedInstance(Test instance) {
        [name: instance.name, type: instance.type, failSave: instance.failSave as String]
    }

    Test saveResource(Test resource) {
        if (resource.failSave) throw new HibernateException('Could not save', new JDBCException('Could not connect', new SQLException('Bad SQL')))
        resource.save flush: true
    }
}