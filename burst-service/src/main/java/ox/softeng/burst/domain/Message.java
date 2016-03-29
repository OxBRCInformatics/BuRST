package ox.softeng.burst.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(schema="Report")
@NamedQueries({
                      @NamedQuery(name="Message.MatchedMessages",
                                  //			query="select distinct m from Message m join m.topics as t "
                                  ///					+ "where (t in (:topics) "
                                  //					+ "and m.dateTimeReceived < :dateNow "
                                  //					+ "and m.dateTimeReceived >= :lastSentDate "
                                  //					+ "and m.severityNumber >= :severity ) "
                                  //					+ "group by m.id having count(distinct t) > (:topicsSize) ")
                                  query="select distinct m from Message m "
                                        + "where "
                                          //			+ "m in ( "
                                          //			+ "select n from Message n inner join n.topics nt where nt in (:topics) "
                                          //			+ "group by n having :topicsSize <= count(distinct nt) ) "
                                        + "m.dateTimeReceived < :dateNow "
                                        + "and m.dateTimeReceived >= :lastSentDate "
                                        + "and m.severityNumber >= :severity ")
              })
public class Message implements Serializable{


    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected Long id = null;

    protected OffsetDateTime dateTimeCreated;
    protected OffsetDateTime dateTimeReceived;

    @Enumerated(EnumType.STRING)
    protected Severity severity;

    protected int severityNumber;

    protected String source;

    @Column(length=10485760)
    protected String message;

    @Fetch(FetchMode.JOIN)
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(schema="Report")
    protected List<String> topics;

    @OneToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL, mappedBy="message")
    protected List<Metadata> metadata;

    public Message()
    {
        topics = new ArrayList<>();
        metadata = new ArrayList<>();
    }

    public Message(String source, String message, Severity severity, OffsetDateTime dateTimeCreated)
    {
        dateTimeReceived = OffsetDateTime.now(ZoneId.of("UTC"));
        this.source = source;
        this.message = message;
        this.severity = severity;
        this.dateTimeCreated = dateTimeCreated;
        topics = new ArrayList<>();
        metadata = new ArrayList<>();
    }

    public void addTopic(String topic)
    {
        topics.add(topic);
    }

    public void addMetadata(String key, String value)
    {
        metadata.add(new Metadata(key, value, this));
    }


    public OffsetDateTime getDateTimeCreated() {
        return dateTimeCreated;
    }

    public void setDateTimeCreated(OffsetDateTime dateCreated) {
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

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
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

    @PrePersist
    public void updateSeverityNumber() {
        this.severityNumber = severity.ordinal();
    }
}
