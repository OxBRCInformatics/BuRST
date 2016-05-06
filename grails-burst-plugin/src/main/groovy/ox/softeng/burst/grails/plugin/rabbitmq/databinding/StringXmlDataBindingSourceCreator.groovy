package ox.softeng.burst.grails.plugin.rabbitmq.databinding

import grails.databinding.DataBindingSource
import grails.web.mime.MimeType
import groovy.transform.CompileStatic
import groovy.util.slurpersupport.GPathResult
import org.grails.web.databinding.bindingsource.XmlDataBindingSourceCreator
import org.xml.sax.SAXException

/**
 * @since 22/02/2016
 */
@CompileStatic
class StringXmlDataBindingSourceCreator extends XmlDataBindingSourceCreator {

    @Override
    MimeType[] getMimeTypes() {
        [MimeType.XML, MimeType.TEXT_XML] as MimeType[]
    }

    @Override
    DataBindingSource createDataBindingSource(MimeType mimeType, Class bindingTargetType, Object bindingSource) {
        if (bindingSource instanceof String) {
            try {
                GPathResult result = new XmlSlurper().parseText(new String(bindingSource))
                return super.createDataBindingSource(mimeType, bindingTargetType, result)
            } catch (SAXException saxException) {
                throw createBindingSourceCreationException(saxException)
            }
        }
        return super.createDataBindingSource(mimeType, bindingTargetType, bindingSource)
    }
}
