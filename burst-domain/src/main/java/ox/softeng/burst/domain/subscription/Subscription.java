package ox.softeng.burst.domain.subscription;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Entity
@Table(name = "subscription", schema = "subscription")
@NamedQueries({
                      @NamedQuery(name = "subscription.allDue",
                                  query = "select s from Subscription s where s.nextScheduledRun is not null and s.lastScheduledRun is not " +
                                          "null and s.nextScheduledRun < :dateNow "),
                      @NamedQuery(name = "subscription.unInitialised",
                                  query = "select s from Subscription s where s.nextScheduledRun is null or s.lastScheduledRun is " +
                                          "null "),
              })
@SequenceGenerator(name = "subscriptionIdSeq", sequenceName = "subscription.subscription_id_seq", allocationSize = 1)
public class Subscription implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(Subscription.class);

    private static final long serialVersionUID = 1L;
    @ManyToOne
    @JoinColumn(name = "frequency")
    protected Frequency frequency;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subscriptionIdSeq")
    protected Long id = null;
    @Column(name = "last_scheduled_run")
    protected OffsetDateTime lastScheduledRun;
    @Column(name = "next_scheduled_run")
    protected OffsetDateTime nextScheduledRun;
    @ManyToOne
    @JoinColumn(name = "severity")
    protected Severity severity;
    @ManyToOne
    protected User subscriber;
    @Fetch(FetchMode.JOIN)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "subscription_topics",
                     schema = "subscription",
                     joinColumns = @JoinColumn(name = "subscription_id",
                                               referencedColumnName = "id"))
    protected Set<String> topics;

    @Column(name = "topics")
    protected String topicsString;

    public Subscription(User subscriber, Frequency frequency, Severity severity) {
        this(subscriber, frequency, severity, "");
    }

    public Subscription(User subscriber, Frequency frequency, Severity severity, Collection<String> topics) {
        this(subscriber, frequency, severity, topics.stream().map(String::trim).collect(Collectors.joining(",")));
    }

    public Subscription(User subscriber, Frequency frequency, Severity severity, String topics) {
        this.subscriber = subscriber;
        this.frequency = frequency;
        this.severity = severity;
        this.topics = new HashSet<>();
        addTopics(topics);

        nextScheduledRun = OffsetDateTime.now();
        switch (frequency.getFrequency()) {
            case IMMEDIATE:
                nextScheduledRun.plusMinutes(1);
                break;
            case DAILY:
                nextScheduledRun.plusDays(1);
                break;
            case WEEKLY:
                nextScheduledRun.plusWeeks(1);
                break;
            case MONTHLY:
                nextScheduledRun.plusMonths(1);
                break;

        }
    }

    public Subscription() {
    }

    public void addTopic(String topic) {
        addTopics(topic);
    }

    public void addTopics(Collection<String> topicCollection) {
        this.topics.addAll(topicCollection);
        topicsString = topicCollection.stream().collect(Collectors.joining(","));
    }

    public void addTopics(String topics) {
        addTopics(Arrays.asList(topics.split(",")));
    }

    public void calculateNextScheduledRun() {
        if (lastScheduledRun == null) {
            lastScheduledRun = OffsetDateTime.now();
        }
        switch (getFrequency().getFrequency()) {
            case IMMEDIATE:
                setNextScheduledRun(lastScheduledRun.plusMinutes(1));
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
        return topics;
    }

    public void setTopics(Set<String> topics) {
        this.topics = new HashSet<>(topics);
        this.topicsString = topics.stream().collect(Collectors.joining(","));
    }

    public String getTopicsString() {
        return topics.stream().collect(Collectors.joining(","));
    }

    public void setTopicsString(String topics) {
        setTopics(new HashSet<>(Arrays.asList(topics.split(","))));
    }

    public void tidyUpTopics(EntityManagerFactory entityManagerFactory) {
        Set<String> tidied = new HashSet<>();
        if (topicsString != null) tidied.addAll(Arrays.asList(topicsString.split(",")));
        topics.forEach(topic -> tidied.addAll(Arrays.asList(topic.split(","))));
        if (tidied.size() != topics.size()) {
            setTopics(tidied);
            EntityManager em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            em.merge(this);
            em.getTransaction().commit();
            em.close();
        }
    }

    public static void initialiseSubscriptions(EntityManagerFactory entityManagerFactory) {
        EntityManager badSubsEm = entityManagerFactory.createEntityManager();
        TypedQuery<Subscription> badSubsQuery = badSubsEm.createNamedQuery("subscription.unInitialised", Subscription.class);
        List<Subscription> uninitialisedSubscriptions = badSubsQuery.getResultList();
        badSubsEm.close();
        if (uninitialisedSubscriptions.size() > 0) {
            logger.debug("Initialising {} subscriptions", uninitialisedSubscriptions.size());
            for (Subscription s : uninitialisedSubscriptions) {
                logger.debug("Scheduling new run for {} subscription {}", s.getSubscriber().emailAddress, s.getId());
                EntityManager schedEm = entityManagerFactory.createEntityManager();
                schedEm.getTransaction().begin();
                s.calculateNextScheduledRun();
                schedEm.merge(s);
                schedEm.getTransaction().commit();
                schedEm.close();
            }
        }
    }
}