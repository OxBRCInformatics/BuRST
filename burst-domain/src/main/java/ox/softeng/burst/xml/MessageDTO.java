package ox.softeng.burst.xml;

import ox.softeng.burst.domain.report.Message;
import ox.softeng.burst.domain.subscription.SeverityEnum;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="message")
@XmlAccessorType(XmlAccessType.FIELD)
public class MessageDTO implements Serializable{

    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(OffsetDateTimeAdapter.class)
    private OffsetDateTime dateTimeCreated;
    @XmlElement(required=true)
    private String details;
    @XmlElement(name = "metadata")
    private List<Metadata> metadata;
    @XmlElement(required = true)
    private SeverityEnum severity;
    @XmlElement(required=true)
    private String source;
    @XmlElement
    private String title;
    @XmlElement( name="topic",required=true)
    private List<String> topics;

    public MessageDTO(){
        topics = new ArrayList<>();
        metadata = new ArrayList<>();
    }

    public void addMetadata(Metadata md) {
        metadata.add(md);
    }

    public void addMetadata(String key, String value) {
        metadata.add(new Metadata(key, value));
    }

    public MessageDTO addToMetadata(String key, String value) {
        this.metadata.add(new Metadata(key, value));
        return this;
    }

    public void addTopic(String topic) {
        topics.add(topic);
    }

    public Message generateMessage() {
        Message msg = new Message(this.source, this.details, this.getSeverity(), dateTimeCreated, title);
        topics.forEach(msg::addTopic);
        if (metadata != null) {
            for (Metadata md : metadata) {
                msg.addMetadata(md.key, md.value);
            }
        }
        return msg;
    }

    public OffsetDateTime getDateTimeCreated() {
        return dateTimeCreated;
    }


    public void setDateTimeCreated(OffsetDateTime dateTimeCreated) {
        this.dateTimeCreated = dateTimeCreated;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Metadata> metadata) {
        this.metadata = metadata;
    }

    public SeverityEnum getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityEnum severity) {
        this.severity = severity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public String toString()
    {
        return "Message Object: " + details;
    }

    public static class Metadata
    {
        protected String key;
        protected String value;

        public Metadata() {

        }

        public Metadata(String key, String value) {
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

}
