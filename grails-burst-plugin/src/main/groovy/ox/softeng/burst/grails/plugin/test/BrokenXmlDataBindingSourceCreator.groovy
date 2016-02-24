package ox.softeng.burst.grails.plugin.test

import grails.databinding.DataBindingSource
import grails.databinding.SimpleMapDataBindingSource
import grails.web.mime.MimeType
import groovy.util.slurpersupport.GPathResult
import org.grails.databinding.xml.GPathResultMap
import org.xml.sax.SAXException
import ox.softeng.burst.grails.plugin.rabbitmq.databinding.StringXmlDataBindingSourceCreator

/**
 * @since 17/02/2016
 */
class BrokenXmlDataBindingSourceCreator extends StringXmlDataBindingSourceCreator {

    @Override
    DataBindingSource createDataBindingSource(MimeType mimeType, Class bindingTargetType, Object bindingSource) {

        if (bindingSource instanceof String) {
            try {
                GPathResult result = new XmlSlurper().parseText(new String(bindingSource))
                def gpathMap = new GPathResultMap(result)
                if (gpathMap.break != null) throw createBindingSourceCreationException(new IllegalStateException('Its broken'))
                return super.createDataBindingSource(mimeType, bindingTargetType, result)
            } catch (SAXException saxException) {
                throw createBindingSourceCreationException(saxException)
            }
        }

        if (bindingSource instanceof GPathResult) {
            def gpathMap = new GPathResultMap((GPathResult) bindingSource)
            if (gpathMap.break != null) throw createBindingSourceCreationException(new IllegalStateException('Its broken'))

            return new SimpleMapDataBindingSource(gpathMap)
        }
        return super.createDataBindingSource(mimeType, bindingTargetType, bindingSource)
    }

}
