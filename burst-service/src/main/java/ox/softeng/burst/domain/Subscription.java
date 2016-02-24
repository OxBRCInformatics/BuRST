package ox.softeng.burst.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(schema="Subscription")
public class Subscription implements Serializable{
	
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected Long id = null;
	
	@ManyToOne
	protected User subscriber;

	@Enumerated(EnumType.STRING)
	protected Frequency frequency;
	
	@ManyToMany
	protected List<Topic> topics;
	
	@Enumerated(EnumType.STRING)
	protected Severity severity;

	protected LocalDateTime nextScheduledRun;
	
	public Subscription(User subscriber, Frequency frequency, Severity severity, Topic topic)
	{
		this.subscriber = subscriber;
		this.frequency = frequency;
		this.severity = severity;
		
		topics = new ArrayList<Topic>();
		topics.add(topic);
		
		nextScheduledRun = LocalDateTime.now();
		switch(frequency)
		{
			case IMMEDIATE:
				nextScheduledRun.plusMinutes(5);
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
	
}
