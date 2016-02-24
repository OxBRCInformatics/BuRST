package ox.softeng.burst.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "Report")
public class Message implements Serializable {


    private static final long serialVersionUID = 1L;
    protected OffsetDateTime dateTimeCreated;
    protected OffsetDateTime dateTimeReceived;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id = null;
    protected String message;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "message")
    protected List<Metadata> metadata;
    protected Severity severity;
    protected String source;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected List<Topic> topics;

    public Message() {
    }

    public Message(String source, String message, Severity severity, OffsetDateTime dateTimeCreated) {
        dateTimeReceived = OffsetDateTime.now(ZoneId.of("UTC"));
        this.source = source;
        this.message = message;
        this.severity = severity;
        this.dateTimeCreated = dateTimeCreated;
        topics = new ArrayList<>();
        metadata = new ArrayList<>();
    }

    public void addMetadata(String key, String value) {
        metadata.add(new Metadata(key, value, this));
    }

    public void addTopic(Topic topic) {
        topics.add(topic);
    }

    public OffsetDateTime getDateTimeCreated() {
        return dateTimeCreated;
    }

    public void setDateTimeCreated(OffsetDateTime dateCreated) {
        this.dateTimeCreated = dateCreated;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Metadata> metadata) {
        this.metadata = metadata;
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

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
}
