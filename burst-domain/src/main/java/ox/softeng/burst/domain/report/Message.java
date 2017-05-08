/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 James Welch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ox.softeng.burst.domain.report;

import ox.softeng.burst.domain.subscription.Severity;
import ox.softeng.burst.domain.util.DomainClass;
import ox.softeng.burst.util.SeverityEnum;
import ox.softeng.burst.xml.MessageDTO;
import ox.softeng.burst.xml.MetadataDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "message", schema = "report", indexes = {
        @Index(columnList = "datetime_received", name = "index_datetime_received"),
        @Index(columnList = "severity_number", name = "index_severity_number"),
        @Index(columnList = "datetime_received,severity_number", name = "index_dr_s")
})
@NamedQueries({
                      @NamedQuery(name = "message.with_severity_between_time",
                                  query = "select distinct m from Message m" +
                                          //                                          " join fetch m.metadata metadata " +
                                          " join fetch m.topics topics " +
                                          " where m.dateTimeReceived < :endTime" +
                                          " and m.dateTimeReceived >= :startTime" +
                                          " and m.severityNumber >= :severity")
              })
@SequenceGenerator(name = "messagesIdSeq", sequenceName = "report.messages_id_seq", allocationSize = 1)
public class Message extends DomainClass implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(Message.class);

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
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "message")
    protected Set<Metadata> metadata;
    @Enumerated(EnumType.STRING)
    protected SeverityEnum severity;
    @Column(name = "severity_number")
    protected int severityNumber;
    protected String source;
    protected String title;
    @ElementCollection
    @CollectionTable(name = "message_topics",
                     schema = "report",
                     joinColumns = @JoinColumn(name = "message_id",
                                               referencedColumnName = "id"
                     ),
                     foreignKey = @ForeignKey(name = "fk_topics_messages"),
                     indexes = @Index(columnList = "topic", name = "index_topic")
    )
    @Column(name = "topic")
    protected Set<String> topics;

    public Message() {
        topics = new HashSet<>();
        metadata = new HashSet<>();
    }

    public Message(String source, String message, SeverityEnum severity, OffsetDateTime dateTimeCreated, String title) {
        this();
        dateTimeReceived = OffsetDateTime.now(ZoneId.of("UTC"));
        this.source = source;
        this.message = message;
        this.severity = severity;
        this.dateTimeCreated = dateTimeCreated;
        this.title = title;
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

    public Set<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(Set<Metadata> metadata) {
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

    public Set<String> getTopics() {
        return topics;
    }

    public void setTopics(Set<String> topics) {
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

    public static List<Message> findAllMessagesBySeverityBetweenTime(EntityManagerFactory entityManagerFactory, Severity severity,
                                                                     OffsetDateTime startTimestamp, OffsetDateTime endTimestamp) {
        logger.trace("Searching for all messages by severity between time");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TypedQuery<Message> msgQuery = entityManager.createNamedQuery("message.with_severity_between_time", Message.class);
        msgQuery.setParameter("endTime", endTimestamp);
        msgQuery.setParameter("startTime", startTimestamp);
        msgQuery.setParameter("severity", severity.getSeverity().ordinal());
        List<Message> matchedMessages = msgQuery.getResultList();
        entityManager.close();
        logger.trace("Found {} messages with severity {} between {} and {} ", matchedMessages.size(), severity, startTimestamp.toString(),
                     endTimestamp.toString());
        return matchedMessages;
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
