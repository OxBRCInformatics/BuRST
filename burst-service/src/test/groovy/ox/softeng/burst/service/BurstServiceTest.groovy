package ox.softeng.burst.service

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ox.softeng.burst.domain.subscription.Subscription
import ox.softeng.burst.domain.subscription.User
import ox.softeng.burst.util.FrequencyEnum
import ox.softeng.burst.util.SeverityEnum
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import javax.persistence.EntityManagerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * @since 04/05/2017
 */
class BurstServiceTest extends Specification {

    public static final Logger logger = LoggerFactory.getLogger(BurstServiceTest)

    BurstService service
    EntityManagerFactory emf

    @Shared
    Properties properties

    def setupSpec() {
        properties = new Properties()
        Path propFile = Paths.get('burst-service/src/test/resources/config.properties')
        assertTrue "Properties file must exist: ${propFile.toAbsolutePath()}", Files.exists(propFile)
        properties.load(new FileInputStream(propFile.toFile()))
    }

    def setup() {
        service = new BurstService(properties)
        emf = service.entityManagerFactory

        List<User> users = []
        (1..200).each {
            users += User.findOrCreate(emf, new User('Test', "User $it", "test.user.${it}@burst.test.com", 'TEST'))
        }
        User.saveAll(emf, users)

        List<Subscription> subscriptions = []
        users.each {
            Subscription s = new Subscription(it, FrequencyEnum.IMMEDIATE, SeverityEnum.INFORMATIONAL, 'topic 1')
            subscriptions += Subscription.findOrCreate(emf, s)
            Subscription s2 = new Subscription(it, FrequencyEnum.IMMEDIATE, SeverityEnum.DEBUG, 'topic 2')
            subscriptions += Subscription.findOrCreate(emf, s2)
        }
        Subscription.saveAll(emf, subscriptions)
    }

    void 'simple test to check everything setup'() {
        expect: 'users in place'
        assertEquals 'Users loaded', 200, User.count(emf, User)

        and: 'subscriptions to be in place'
        assertEquals 'Subscriptions loaded', 400, Subscription.count(emf, Subscription)
    }

    @Ignore('This is only used to manually flood the system, it does not have an outcome')
    void 'flood system with messages'() {
        given: 'service runs'
        service.startService()

        when:
        while (true) {
            sendMessages(10)
            sleep 10000
        }

        then:
        true
    }

    def cleanup() {
        service?.stopService()
    }

    void sendMessages(long amount) {

        Connection connection = service.rabbitMessageService.connection
        Channel channel = connection.createChannel()

        String exchange = service.rabbitMessageService.exchange
        String queue = service.rabbitMessageService.queue

        channel.exchangeDeclare(exchange, "topic", true)


        for (int i = 0; i < amount; i++) {
            Path p = Paths.get("burst-service/src/test/resources/message/instance${(i % 10) + 1}.xml")
            assert Files.exists(p)
            String message = p.text
            channel.basicPublish(exchange, queue, null, message.getBytes("UTF-8"))
            logger.debug("Sent ${p}")
        }
        logger.info('All messages sent')
        channel.close()
        connection.close()
    }
}
