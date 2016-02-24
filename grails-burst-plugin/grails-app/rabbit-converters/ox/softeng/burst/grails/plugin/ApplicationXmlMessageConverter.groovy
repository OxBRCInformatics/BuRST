package ox.softeng.burst.grails.plugin

import com.budjb.rabbitmq.converter.MessageConverter
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.xml.sax.SAXException


class ApplicationXmlMessageConverter extends MessageConverter<GPathResult> {

    BurstService burstService

    @Override
    String getContentType() {
        "application/xml"
    }

    @Override
    boolean canConvertFrom() {
        true
    }

    @Override
    boolean canConvertTo() {
        true
    }

    @Override
    GPathResult convertTo(byte[] input) {
        try {
            return new XmlSlurper().parseText(new String(input))
        } catch (SAXException ignored) {
            null
        }
    }

    @Override
    byte[] convertFrom(GPathResult input) {
        XmlUtil.serialize(input).bytes
    }
}