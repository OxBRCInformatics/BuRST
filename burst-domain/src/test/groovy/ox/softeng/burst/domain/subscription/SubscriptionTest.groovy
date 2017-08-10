package ox.softeng.burst.domain.subscription

import spock.lang.Specification

/**
 * @since 14/06/2016
 */
class SubscriptionTest extends Specification {

    Subscription subscription
    User user

    def setup() {
        user = new User('test', 'user', 'tu@test.com', 'no-org', "http://localhost:8080/endpoint")
    }

    void 'test topics'() {

        given:
        subscription = new Subscription(user, Frequency.from('IMMEDIATE'), Severity.from('ERROR'))

        expect:
        !subscription.hasTopics()

        when:
        subscription.addTopic('test')

        then:
        subscription.hasTopics()
        subscription.topics.size() == 1

        when:
        subscription.addTopic('another')

        then:
        subscription.hasTopics()
        subscription.topics.size() == 2
        subscription.topicsString == 'test,another'

        when:
        subscription.setTopics(['me'])

        then:
        subscription.hasTopics()
        subscription.topics.size() == 1

        when:
        subscription.addTopic(' another')

        then:
        subscription.hasTopics()
        subscription.topics.size() == 2
        subscription.topicsString == 'me,another'

        when:
        subscription.topicsString = 'test'

        then:
        subscription.hasTopics()
        subscription.topics.size() == 1

        when:
        subscription.topicsString = 'test,another'

        then:
        subscription.hasTopics()
        subscription.topics.size() == 2

        when:
        subscription.topicsString = 'test,another,and'

        then:
        subscription.hasTopics()
        subscription.topics.size() == 3

        when:
        subscription.topicsString = '  test   '

        then:
        subscription.hasTopics()
        subscription.topics.size() == 1
        subscription.topics[0] == 'test'

        when:
        subscription.topicsString = '  test,another'

        then:
        subscription.hasTopics()
        subscription.topics.size() == 2
        subscription.topics[0] == 'test'
        subscription.topics[1] == 'another'
    }
}
