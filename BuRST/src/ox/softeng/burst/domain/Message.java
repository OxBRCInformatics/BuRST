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
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(schema="Report")
public class Message implements Serializable{


	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected Long id = null;

	protected LocalDateTime dateTimeCreated;
	protected LocalDateTime dateTimeReceived;
	
	protected Severity severity;
	
	protected String source;
	
	protected String message;
	
	@ManyToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL)
	protected List<Topic> topics;
	
	@OneToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL, mappedBy="message")
	protected List<Metadata> metadata;

	public Message(){}
	
	public Message(String source, String message, Severity severity, LocalDateTime dateTimeCreated)
	{
		dateTimeReceived = LocalDateTime.now();
		this.source = source;
		this.message = message;
		this.severity = severity;
		this.dateTimeCreated = dateTimeCreated;
		topics = new ArrayList<Topic>();
		metadata = new ArrayList<Metadata>();
	}

	public void addTopic(Topic topic)
	{
		topics.add(topic);
	}
	
	public void addMetadata(String key, String value)
	{
		metadata.add(new Metadata(key, value, this));
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
	

	public List<Metadata> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<Metadata> metadata) {
		this.metadata = metadata;
	}
}
