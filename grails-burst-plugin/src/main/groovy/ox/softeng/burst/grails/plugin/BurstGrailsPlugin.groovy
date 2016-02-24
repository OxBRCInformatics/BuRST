package ox.softeng.burst.grails.plugin

import grails.plugins.Plugin
import grails.util.Environment
import ox.softeng.burst.grails.plugin.rabbitmq.databinding.StringXmlDataBindingSourceCreator
import ox.softeng.burst.grails.plugin.test.BrokenXmlDataBindingSourceCreator

class BurstGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.1.1 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            '**/grails/plugin/test**'
    ]

    // TODO Fill in these fields
    def title = "Grails Burst Plugin"
    // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Ties the BuRST system in with Grails.

Provides the following classes which can be extended, each will produce BuRST messages in the event of any errors or exceptions:

* Grails Controller
* Grails Restful Controller
* RabbitMQ Consumer
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/grails-burst-plugin"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [name: "Oxford University BRC Informatics", url: "http://oxfordbrc.nihr.ac.uk/working_groups/clinical-informatics/"]

    // Any additional developers beyond the author specified above.
    def developers = [[name: "James Welch", email: "james.welch@cs.ox.ac.uk"]]

    // Location of the plugin's issue tracker.
    //    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
    def scm = [url: "https://github.com/olliefreeman/BuRST"]

    def dependsOn = ['rabbitmqNative': "3.1.3 > *"]
    def loadAfter = ['mimetypes', 'domainClass']
    def influences = ['rabbitmqNative']

    Closure doWithSpring() {
        {->

            xmlDataBindingSourceCreator(StringXmlDataBindingSourceCreator)

            if (Environment.current == Environment.DEVELOPMENT || Environment.current == Environment.TEST) {
                xmlDataBindingSourceCreator(BrokenXmlDataBindingSourceCreator)
            }
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
