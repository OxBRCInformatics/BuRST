package ox.softeng.burst.domain;

import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(schema="Subscription")
@NamedQueries({
	@NamedQuery(name="Subscription.allDue", query="select s from Subscription s where s.nextScheduledRun is not null and s.lastScheduledRun is not null and s.nextScheduledRun < :dateNow "),
	@NamedQuery(name="Subscription.unInitialised", query="select s from Subscription s where s.nextScheduledRun is null or s.lastScheduledRun is null "),
})
public class Subscription implements Serializable{
	
	private static final long serialVersionUID = 1L;
	@ManyToOne
	@JoinColumn(name = "frequency")
	protected Frequency frequency;
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id = null;
	@Column(name = "LastScheduledRun")
	protected OffsetDateTime lastScheduledRun;
	@Column(name = "NextScheduledRun")
	protected OffsetDateTime nextScheduledRun;
	@ManyToOne
	@JoinColumn(name = "severity")
	protected Severity severity;
	@ManyToOne
	protected User subscriber;
	@Fetch(FetchMode.JOIN)
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(schema="Subscription")
    @CollectionId(
			columns = @Column(name = "topics_id"),
			type = @Type(type = "long"),
			generator = "identity"
        )
	protected List<String> topics;
	
	public Subscription(User subscriber, Frequency frequency, Severity severity)
	{
		this.subscriber = subscriber;
		this.frequency = frequency;
		this.severity = severity;
		
		topics = new ArrayList<String>();
		
		nextScheduledRun = OffsetDateTime.now();
		switch(frequency.getFrequency())
		{
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
	
	public Subscription()
	{
		
	}

	public void addTopic(String tpc)
	{
		topics.add(tpc);
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

	public List<String> getTopics() {
		return topics;
	}

	public void setTopics(List<String> topics) {
		this.topics = topics;
	}
	
}
