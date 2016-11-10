package ox.softeng.burst.domain.report;

import ox.softeng.burst.util.SeverityEnum;
import ox.softeng.burst.xml.MessageDTO;
import ox.softeng.burst.xml.MetadataDTO;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "message", schema = "report")
@NamedQueries({
                      @NamedQuery(name = "message.MatchedMessages",
                                  query = "select distinct m from Message m "
                                          + "where "
                                          + "m.dateTimeReceived < :dateNow "
                                          + "and m.dateTimeReceived >= :lastSentDate "
                                          + "and m.severityNumber >= :severity ")
              })
@SequenceGenerator(name = "messagesIdSeq", sequenceName = "report.messages_id_seq", allocationSize = 1)
public class Message implements Serializable {


    private static final long serialVersionUID = 1L;
    @Column(name = "datetime_created")
    protected OffsetDateTime dateTimeCreated;
    @Column(name = "datetime_received")
    protected OffsetDateTime dateTimeReceived;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "messagesIdSeq")
    protected Long id = null;
    @Column(name = "message", columnDefinition = "TEXT")
    protected String message;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "message")
    protected List<Metadata> metadata;
    @Enumerated(EnumType.STRING)
    protected SeverityEnum severity;
    @Column(name = "severity_number")
    protected int severityNumber;
    protected String source;
    protected String title;
    @Fetch(FetchMode.JOIN)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "message_topics",
                     schema = "report",
                     joinColumns = @JoinColumn(name = "message_id",
                                               referencedColumnName = "id"))
    protected List<String> topics;

    public Message() {
        topics = new ArrayList<>();
        metadata = new ArrayList<>();
    }

    public Message(String source, String message, SeverityEnum severity, OffsetDateTime dateTimeCreated, String title) {
        dateTimeReceived = OffsetDateTime.now(ZoneId.of("UTC"));
        this.source = source;
        this.message = message;
        this.severity = severity;
        this.dateTimeCreated = dateTimeCreated;
        this.title = title;
        topics = new ArrayList<>();
        metadata = new ArrayList<>();
    }

    public void addMetadata(String key, String value) {
        metadata.add(new Metadata(key, value, this));
    }

    public void addTopic(String topic) {
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

    public boolean hasTitle() {
        return title != null;
    }

    @PrePersist
    public void updateSeverityNumber() {
        if (severity != null) {
            this.severityNumber = severity.ordinal();
        }
    }

    public static Message generateMessage(MessageDTO messageDTO) {
        Message msg = new Message(messageDTO.getSource(), messageDTO.getDetails(), messageDTO.getSeverity(),
                                  messageDTO.getDateTimeCreated(), messageDTO.getTitle());
        messageDTO.getTopics().forEach(msg::addTopic);
        if (messageDTO.getMetadata() != null) {
            for (MetadataDTO md : messageDTO.getMetadata()) {
                msg.addMetadata(md.getKey(), md.getValue());
            }
        }
        return msg;
    }
}
