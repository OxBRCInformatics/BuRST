package ox.softeng.burst.grails.plugin.rabbitmq.consumer;

import com.budjb.rabbitmq.consumer.MessageContext;
import groovy.util.slurpersupport.GPathResult;

/**
 * @since 19/06/2017
 */
public interface XmlMessageConsumerHandler {
    GPathResult handleMessage(GPathResult body, MessageContext messageContext);

    String handleMessage(String body, MessageContext messageContext);
}
