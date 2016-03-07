import grails.util.Environment
import ox.softeng.burst.grails.plugin.test.BrokenXmlDataBindingSourceCreator

beans = {
    if (Environment.current == Environment.TEST) {
        xmlDataBindingSourceCreator(BrokenXmlDataBindingSourceCreator)
    }
}