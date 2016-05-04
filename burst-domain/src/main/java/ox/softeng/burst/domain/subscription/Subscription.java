package ox.softeng.burst.domain.subscription;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
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
@SequenceGenerator(name = "subscriptionIdSeq", sequenceName = "subscription.subscription_id_seq")
public class Subscription implements Serializable {

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
    protected String topics;

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
        this.topics = topics;

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

    public void addTopic(String tpc) {
        addTopics(Collections.singletonList(tpc));
    }

    public void addTopics(Collection<String> topicCollection) {
        StringBuilder sb = new StringBuilder(topics);
        topicCollection.forEach(topic -> sb.append(",").append(topic));
        topics = sb.toString();
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

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }
}
