package ox.softeng.burst.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


@Entity
@Table(schema="Subscription")
@NamedQueries({
	@NamedQuery(name="Subscription.allDue", query="select s from Subscription s where s.nextScheduledRun is not null and s.lastScheduledRun is not null and s.nextScheduledRun < :dateNow "),
	@NamedQuery(name="Subscription.unInitialised", query="select s from Subscription s where s.nextScheduledRun is null or s.lastScheduledRun is null "),
})
public class Subscription implements Serializable{
	
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected Long id = null;
	
	@ManyToOne
	protected User subscriber;

	@Enumerated(EnumType.STRING)
	protected Frequency frequency;
	
	
	@Fetch(FetchMode.JOIN)
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(schema="Subscription")
	protected List<String> topics;
	
	@Enumerated(EnumType.STRING)
	protected Severity severity;

	protected LocalDateTime nextScheduledRun;
	protected LocalDateTime lastScheduledRun;
	
	public Subscription(User subscriber, Frequency frequency, Severity severity)
	{
		this.subscriber = subscriber;
		this.frequency = frequency;
		this.severity = severity;
		
		topics = new ArrayList<String>();
		
		nextScheduledRun = LocalDateTime.now();
		switch(frequency)
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
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(User subscriber) {
		this.subscriber = subscriber;
	}

	public Frequency getFrequency() {
		return frequency;
	}

	public void setFrequency(Frequency frequency) {
		this.frequency = frequency;
	}

	public List<String> getTopics() {
		return topics;
	}

	public void setTopics(List<String> topics) {
		this.topics = topics;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public LocalDateTime getNextScheduledRun() {
		return nextScheduledRun;
	}

	public void setNextScheduledRun(LocalDateTime nextScheduledRun) {
		this.nextScheduledRun = nextScheduledRun;
	}

	public LocalDateTime getLastScheduledRun() {
		return lastScheduledRun;
	}

	public void setLastScheduledRun(LocalDateTime lastScheduledRun) {
		this.lastScheduledRun = lastScheduledRun;
	}

	public void calculateNextScheduledRun() {
		if(lastScheduledRun == null)
		{
			lastScheduledRun = LocalDateTime.now();
		}
		switch(getFrequency())
		{
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
	
}
