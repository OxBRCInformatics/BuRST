/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 James Welch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ox.softeng.burst.domain.subscription;

import ox.softeng.burst.domain.util.DomainClass;
import ox.softeng.burst.util.FrequencyEnum;
import ox.softeng.burst.util.SeverityEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Entity
@Table(name = "subscription", schema = "subscription",
       indexes = {
               @Index(columnList = "last_scheduled_run,severity_id", name = "index_lsr_si"),
               @Index(columnList = "next_scheduled_run,last_scheduled_run", name = "index_nsr_lsr"),
               @Index(columnList = "next_scheduled_run,last_scheduled_run,next_scheduled_run", name = "index_nsr_lsr_nsr"),
               @Index(columnList = "next_scheduled_run", name = "index_nsr")
       })
@NamedQueries({
                      @NamedQuery(name = "subscription.all_due",
                                  query = "select s from Subscription s" +
                                          " join fetch s.subscriber u" +
                                          " join fetch s.frequency f" +
                                          " join fetch s.severity sev" +
                                          " where s.nextScheduledRun is not null" +
                                          " and s.lastScheduledRun is not null" +
                                          " and s.nextScheduledRun < :dateNow "),
                      @NamedQuery(name = "subscription.uninitialised",
                                  query = "select s from Subscription s" +
                                          " join fetch s.frequency f" +
                                          " where s.nextScheduledRun is null" +
                                          " or s.lastScheduledRun is null "),
                      @NamedQuery(name = "subscription.find_by_user_and_topics",
                                  query = "select s from Subscription s" +
                                          " join fetch s.subscriber u" +
                                          " where s.subscriber = :subscriber" +
                                          " and s.topicsString = :topics")
              })
@SequenceGenerator(name = "subscriptionIdSeq", sequenceName = "subscription.subscription_id_seq", allocationSize = 1)
public class Subscription extends DomainClass implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(Subscription.class);

    private static final long serialVersionUID = 1L;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frequency_id", foreignKey = @ForeignKey(name = "fk_subscription_frequency"))
    protected Frequency frequency;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subscriptionIdSeq")
    protected Long id = null;
    @Column(name = "last_scheduled_run")
    protected OffsetDateTime lastScheduledRun;
    @Column(name = "next_scheduled_run")
    protected OffsetDateTime nextScheduledRun;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "severity_id", foreignKey = @ForeignKey(name = "fk_subscription_severity"))
    protected Severity severity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_subscription_users"))
    protected User subscriber;


    @Column(name = "topics")
    protected String topicsString;

    public Subscription(User subscriber, Frequency frequency, Severity severity) {
        this(subscriber, frequency, severity, "");
    }

    public Subscription(User subscriber, Frequency frequency, Severity severity, Collection<String> topics) {
        this(subscriber, frequency, severity, topics.stream().map(String::trim).collect(Collectors.joining(",")));
    }

    public Subscription(User subscriber, FrequencyEnum frequency, SeverityEnum severity, String topics) {
        this(subscriber, new Frequency(frequency), new Severity(severity), topics);
    }

    public Subscription(User subscriber, Frequency frequency, Severity severity, String topics) {
        this.subscriber = subscriber;
        this.frequency = frequency;
        this.severity = severity;
        this.topicsString = topics;
    }

    public Subscription() {
    }

    public void addTopic(String topic) {
        addTopics(topic.trim());
    }

    public void addTopics(Collection<String> topicCollection) {
        addTopics(topicCollection.stream().map(String::trim).collect(Collectors.joining(",")));
    }

    public void addTopics(String topics) {
        if (topicsString == null || topicsString.isEmpty()) topicsString = topics;
        else topicsString += "," + topics.trim();
    }

    public void calculateNextScheduledRun(Long immediateFrequency) {
        if (lastScheduledRun == null) {
            lastScheduledRun = OffsetDateTime.now();
        }
        switch (getFrequency().getFrequency()) {
            case IMMEDIATE:
                setNextScheduledRun(lastScheduledRun.plusMinutes(immediateFrequency));
                break;
            case DAILY:
                setNextScheduledRun(lastScheduledRun.plusDays(1));
                break;
            case WEEKLY:
                setNextScheduledRun(lastScheduledRun.plusWeeks(1));
                break;
            case MONTHLY:
                setNextScheduledRun(lastScheduledRun.plusMonths(1));
                break;
            default:
                setNextScheduledRun(null);
        }
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OffsetDateTime getLastScheduledRun() {
        return lastScheduledRun;
    }

    public void setLastScheduledRun(OffsetDateTime lastScheduledRun) {
        this.lastScheduledRun = lastScheduledRun;
    }

    public OffsetDateTime getNextScheduledRun() {
        return nextScheduledRun;
    }

    public void setNextScheduledRun(OffsetDateTime nextScheduledRun) {
        this.nextScheduledRun = nextScheduledRun;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public User getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(User subscriber) {
        this.subscriber = subscriber;
    }

    public Set<String> getTopics() {
        if (topicsString == null || topicsString.isEmpty()) return new HashSet<>();
        return Arrays.stream(topicsString.split(",")).map(String::trim).collect(Collectors.toSet());
    }

    public void setTopics(Set<String> topics) {
        this.topicsString = topics.stream().map(String::trim).collect(Collectors.joining(","));
    }

    public String getTopicsString() {
        return topicsString;
    }

    public void setTopicsString(String topics) {
        this.topicsString = topics;
    }

    public boolean hasTopics() {
        return !getTopics().isEmpty();
    }

    public void setTopics(List<String> topics) {
        setTopics(new HashSet<>(topics));
    }

    public static List<Subscription> findDueSubscriptions(EntityManagerFactory entityManagerFactory, OffsetDateTime now) {
        logger.trace("Searching for due subscriptions");
        // First we find all the subscriptions where the "next time" is less than now
        EntityManager em = entityManagerFactory.createEntityManager();
        TypedQuery<Subscription> subsQuery = em.createNamedQuery("subscription.all_due", Subscription.class);
        subsQuery.setParameter("dateNow", now);
        List<Subscription> dueSubscriptions = subsQuery.getResultList();
        em.close();
        List<Subscription> clean = dueSubscriptions.stream()
                .filter(Subscription::hasTopics)
                .collect(Collectors.toList());
        logger.trace("Found {} due subscriptions for {}", clean.size(), now.toString());
        return clean;
    }

    public static Subscription findOrCreate(EntityManagerFactory entityManagerFactory, Subscription subscription) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TypedQuery<Subscription> query = entityManager.createNamedQuery("subscription.find_by_user_and_topics", Subscription.class);
        query.setParameter("subscriber", subscription.subscriber);
        query.setParameter("topics", subscription.topicsString);
        List<Subscription> subscriptions = query.getResultList();
        entityManager.close();
        if (subscriptions.isEmpty()) return subscription;
        return subscriptions.get(0);
    }

    public static void initialiseSubscriptions(EntityManagerFactory entityManagerFactory, Long immediateFrequency) {
        logger.trace("Checking for subscriptions to initialise");
        EntityManager badSubsEm = entityManagerFactory.createEntityManager();
        TypedQuery<Subscription> badSubsQuery = badSubsEm.createNamedQuery("subscription.uninitialised", Subscription.class);
        List<Subscription> uninitialisedSubscriptions = badSubsQuery.getResultList();
        badSubsEm.close();
        if (uninitialisedSubscriptions.size() > 0) {
            logger.debug("Initialising {} subscriptions", uninitialisedSubscriptions.size());
            for (Subscription s : uninitialisedSubscriptions) {
                logger.trace("Scheduling new run for {} subscription {}", s.getSubscriber().emailAddress, s.getId());
                EntityManager schedEm = entityManagerFactory.createEntityManager();
                schedEm.getTransaction().begin();
                s.calculateNextScheduledRun(immediateFrequency);
                schedEm.merge(s);
                schedEm.getTransaction().commit();
                schedEm.close();
            }
        }
        logger.trace("Subscriptions all initialised");
    }
}
