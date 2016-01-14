package ox.softeng.burst.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Topic {

	@Id
	protected String topicName; 
	
	public Topic(){}
	
	public Topic(String topicName)
	{
		this.topicName = topicName;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}
	
}
