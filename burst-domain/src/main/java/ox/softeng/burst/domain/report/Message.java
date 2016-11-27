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
@Table(name = "message", schema = "report", indexes = {
        @Index(columnList = "datetime_received", name = "index_datetime_received"),
        @Index(columnList = "severity_number", name = "index_severity_number")
})
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
                                               referencedColumnName = "id"
                     ),
                     foreignKey = @ForeignKey(name = "fk_topics_messages"),
                     indexes = @Index(columnList = "topic", name = "index_topic")
    )
    @Column(name = "topic")
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
