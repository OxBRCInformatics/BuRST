package ox.softeng.burst.service.report

import ox.softeng.burst.domain.report.Message
import ox.softeng.burst.domain.subscription.User
import ox.softeng.burst.util.SeverityEnum
import spock.lang.Shared
import spock.lang.Specification

import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.OffsetDateTime
import java.util.concurrent.Executors

import static org.junit.Assert.assertTrue

/**
 * @since 08/05/2016
 */
class ReportServiceTest extends Specification {

    ReportService service
    Message message, message2, message3
    User user

    @Shared
    Properties properties

    def setupSpec() {
        properties = new Properties()
        Path propFile = Paths.get('burst-service/src/test/resources/config.properties')
        assertTrue "Properties file must exist: ${propFile.toAbsolutePath()}", Files.exists(propFile)
        properties.load(new FileInputStream(propFile.toFile()))
    }

    def setup() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("ox.softeng.burst", properties);
        service = new ReportService(entityManagerFactory, Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), properties, null, OffsetDateTime.now(), 1)
        message = new Message('test', 'test message', SeverityEnum.ALERT,
                              OffsetDateTime.now(), null)
        message2 = new Message('test', 'test message2', SeverityEnum.ALERT,
                               OffsetDateTime.now(), 'Title 2')
        message3 = new Message('test', 'test message3', SeverityEnum.NOTICE,
                               OffsetDateTime.now(), 'Title 3')
        user = new User('test', 'user', 'test.user@test.com', 'an org', "http://localhost:8080/endpoint")
    }

    def "generate email contents correctly"() {
        when: '1 email'
        Map<SeverityEnum, List<Message>> msgs = new HashMap<>()
        msgs.put(SeverityEnum.ALERT, [message])
        String result = service.generateEmailContents(msgs, user)

        then: '1 message content'
        result == """To test user

The following message has been logged in BuRST matching your subscription:

${message.message}

Kind Regards

The BuRST Service""".toString()

        when: '2 emails in same sev'
        msgs.put(SeverityEnum.ALERT, [message, message2])
        result = service.generateEmailContents(msgs, user)

        then: '2 messages in alert'
        result == """To test user

The following messages have been logged in BuRST matching your subscription:

2 Alert messages:

${message.message}

----

${message2.message}

------ End of Alert messages ------

Kind Regards

The BuRST Service""".toString()

        when: '3 emails and in different sev'
        message.title = "This is a title"
        msgs.put(SeverityEnum.ALERT, [message, message2])
        msgs.put(SeverityEnum.NOTICE, [message3])
        result = service.generateEmailContents(msgs, user)

        then: '2 messages in alert and 1 in notice'
        result == """To test user

The following messages have been logged in BuRST matching your subscription:

2 Alert messages:

${message.message}

----

${message2.message}

------ End of Alert messages ------

1 Notice message:

${message3.message}

------ End of Notice messages ------

Kind Regards

The BuRST Service""".toString()
    }

    def "generate http contents correctly"() {
        when: '1 email'
        Map<SeverityEnum, List<Message>> msgs = new HashMap<>()
        msgs.put(SeverityEnum.ALERT, [message])
        String result = service.generateMessageContents(msgs, user)

        then: '1 message content'
        result == """To test user

The following message has been logged in BuRST matching your subscription:

${message.message}

Kind Regards

The BuRST Service""".toString()

        when: '2 emails in same sev'
        msgs.put(SeverityEnum.ALERT, [message, message2])
        result = service.generateMessageContents(msgs, user)

        then: '2 messages in alert'
        result == """To test user

The following messages have been logged in BuRST matching your subscription:

2 Alert messages:

${message.message}

----

${message2.message}

------ End of Alert messages ------

Kind Regards

The BuRST Service""".toString()

        when: '3 emails and in different sev'
        message.title = "This is a title"
        msgs.put(SeverityEnum.ALERT, [message, message2])
        msgs.put(SeverityEnum.NOTICE, [message3])
        result = service.generateMessageContents(msgs, user)

        then: '2 messages in alert and 1 in notice'
        result == """To test user

The following messages have been logged in BuRST matching your subscription:

2 Alert messages:

${message.message}

----

${message2.message}

------ End of Alert messages ------

1 Notice message:

${message3.message}

------ End of Notice messages ------

Kind Regards

The BuRST Service""".toString()
    }

    def "generate email subjects correctly"() {

        when: '1 email no title'
        Map<SeverityEnum, List<Message>> msgs = new HashMap<>()
        msgs.put(SeverityEnum.ALERT, [message])
        String result = service.generateEmailSubject(msgs)

        then: 'default message with 1 alert'
        result == 'BuRST Reporting Message: 1 Alert'

        when: '1 email with title'
        message.title = "This is a title"
        msgs.put(SeverityEnum.ALERT, [message])
        result = service.generateEmailSubject(msgs)

        then: 'default message with title'
        result == 'BuRST Reporting Message: This is a title'

        when: '2 emails with title and in same sev'
        message.title = "This is a title"
        msgs.put(SeverityEnum.ALERT, [message, message2])
        result = service.generateEmailSubject(msgs)

        then: 'default message with 2 Alert'
        result == 'BuRST Reporting Message: 2 Alerts'

        when: '3 emails with title and in different sev'
        message.title = "This is a title"
        msgs.put(SeverityEnum.ALERT, [message, message2])
        msgs.put(SeverityEnum.NOTICE, [message3])
        result = service.generateEmailSubject(msgs)

        then: 'default message with 2 Alerts and 1 Notice'
        result == 'BuRST Reporting Message: 2 Alerts, 1 Notice'

    }
}
