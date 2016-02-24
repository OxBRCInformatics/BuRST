package ox.softeng.burst.services;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ox.softeng.burst.domain.Message;
import ox.softeng.burst.domain.Severity;
import ox.softeng.burst.domain.Topic;

@XmlRootElement(name="message")
@XmlAccessorType(XmlAccessType.FIELD)
public class MessageDTO implements Serializable{


	private static final long serialVersionUID = 1L;

	@XmlElement(required=true)
	@XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
	private LocalDateTime dateTimeCreated;
	
	@XmlElement(required=true)
	private Severity severity;
	
	@XmlElement(required=true)
	private String source;
	
	@XmlElement(required=true)
	private String details;
	
	@XmlElement( name="topic",required=true)
	//@XmlElementWrapper(name="topics")
	private List<String> topics;

	@XmlElement( name="metadata",required=false)
	//@XmlElementWrapper(name="metadata")
	private List<Metadata> metadata;

	public MessageDTO(){
		topics = new ArrayList<String>();
		metadata = new ArrayList<Metadata>();
	}
	
	
	public Message generateMessage()
	{
		Message msg = new Message(this.source, this.details, this.severity, dateTimeCreated);
		for(String topic : topics)
		{
			msg.addTopic(new Topic(topic));
		}
		if(metadata != null)
		{
			for(Metadata md : metadata)
			{
				msg.addMetadata(md.key, md.value);
			}
		}
		return msg;
	}
	
	public String toString()
	{
		return "Message Object: "+ details;
	}

	
	public static class Metadata
	{
		protected String key;
		protected String value;
		public Metadata()
		{
			
		}
		public Metadata(String key, String value)
		{
			this.key = key;
			this.value = value;
		}
		
		
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}

	public List<Metadata> getMetadata() {
		return metadata;
	}


	public LocalDateTime getDateTimeCreated() {
		return dateTimeCreated;
	}


	public void setDateTimeCreated(LocalDateTime dateTimeCreated) {
		this.dateTimeCreated = dateTimeCreated;
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


	public String getDetails() {
		return details;
	}


	public void setDetails(String details) {
		this.details = details;
	}


	public List<String> getTopics() {
		return topics;
	}


	public void setTopics(List<String> topics) {
		this.topics = topics;
	}


	public void setMetadata(List<Metadata> metadata) {
		this.metadata = metadata;
	}
	
	public void addTopic(String topic)
	{
		topics.add(topic);
	}
	
	public void addMetadata(Metadata md)
	{
		metadata.add(md);
	}
	public void addMetadata(String key, String value)
	{
		metadata.add(new Metadata(key, value));
	}
	
}
