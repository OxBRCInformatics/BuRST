package ox.softeng.burst.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

@Entity
public class Message implements Serializable{


	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected Long id = null;

	protected LocalDateTime dateTimeCreated;
	protected LocalDateTime dateTimeSent;
	
	@ManyToOne
	protected Severity severity;
	
	protected String source;
	
	protected String message;
	
	@ManyToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL)
	protected List<Topic> topics;

	public Message(){}
	
	public Message(String source, String message, Severity severity, LocalDateTime dateTimeSent)
	{
		dateTimeCreated = LocalDateTime.now();
		this.source = source;
		this.message = message;
		this.severity = severity;
		this.dateTimeSent = dateTimeSent;
		topics = new ArrayList<Topic>();
	}
	public void addTopic(Topic topic)
	{
		topics.add(topic);
	}

	
	public LocalDateTime getDateTimeCreated() {
		return dateTimeCreated;
	}
	
	public void setDateTimeCreated(LocalDateTime dateCreated) {
		this.dateTimeCreated = dateCreated;
	}
	
	
	public Severity getSeverity() {
		return severity;
	}
	
	public void setSeverity(Severity severity) {
		this.severity = severity;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public List<Topic> getTopics() {
		return topics;
	}
	
	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}
	
	public Long getId() {
		return id;
	}
}
