package ox.softeng.burst.services;

import java.io.Serializable;
import java.time.LocalDateTime;

import java.util.List;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ox.softeng.burst.domain.Message;
import ox.softeng.burst.domain.Severity;
import ox.softeng.burst.domain.Topic;

@XmlRootElement(name="message")
public class MessageMsg implements Serializable{


	private static final long serialVersionUID = 1L;

	@XmlElement(required=true)
	@XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
	public LocalDateTime dateTimeSent;
	
	@XmlElement(required=true)
	public String severity;
	
	@XmlElement(required=true)
	public String source;
	
	@XmlElement(required=true)
	public String details;
	
	@XmlElement( name="topic",required=true)
	@XmlElementWrapper(name="topics")
	public List<String> topics;

	public MessageMsg(){
		
	}
	
	
	public Message generateMessage()
	{
		Message msg = new Message(this.source, this.details, new Severity(this.severity), dateTimeSent);
		for(String topic : topics)
		{
			msg.addTopic(new Topic(topic));
		}
		return msg;
	}
	
	public String toString()
	{
		return "Message Object: "+ details;
	}

	
}
