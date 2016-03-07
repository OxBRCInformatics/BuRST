package ox.softeng.burst.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(schema="Report")
public class Metadata {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	protected Long id = null;

	@ManyToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="message")
	protected Message message;

	protected String key;
	protected String value;


	public Metadata(String key, String value, Message message)
	{
		this.key = key;
		this.value = value;
		this.message = message;
	}
	public Metadata()
	{

	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Message getMessage() {
		return message;
	}
	public void setMessage(Message message) {
		this.message = message;
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


