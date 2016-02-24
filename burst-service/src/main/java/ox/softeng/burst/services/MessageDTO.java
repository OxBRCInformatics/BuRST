package ox.softeng.burst.services;

import ox.softeng.burst.domain.Message;
import ox.softeng.burst.domain.Severity;
import ox.softeng.burst.domain.Topic;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "message")
public class MessageDTO implements Serializable {


    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(OffsetDateTimeAdapter.class)
    public OffsetDateTime dateTimeCreated;
    @XmlElement(required = true)
    public String details;
    @XmlElement(name = "metadata")
    //@XmlElementWrapper(name="metadata")
    public List<Metadata> metadata;
    @XmlElement(required = true)
    public Severity severity;
    @XmlElement(required = true)
    public String source;
    @XmlElement(name = "topic", required = true)
    //@XmlElementWrapper(name="topics")
    public List<String> topics;

    public MessageDTO() {
        topics = new ArrayList<>();
        metadata = new ArrayList<>();
    }

    public MessageDTO addToMetadata(String key, String value) {
        this.metadata.add(new Metadata(key, value));
        return this;
    }

    public Message generateMessage() {
        Message msg = new Message(this.source, this.details, this.severity, dateTimeCreated);
        for (String topic : topics) {
            msg.addTopic(new Topic(topic));
        }
        if (metadata != null) {
            for (Metadata md : metadata) {
                msg.addMetadata(md.key, md.value);
            }
        }
        return msg;
    }

    public List<Metadata> getMetadata() {
        return metadata;
    }

    public String toString() {
        return "Message Object: " + details;
    }

    public static class Metadata {
        private String key;
        private String value;

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
