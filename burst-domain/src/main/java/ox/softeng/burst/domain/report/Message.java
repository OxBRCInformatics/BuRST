/**
 * Academic Use Licence
 *
 * These licence terms apply to all licences granted by
 * OXFORD UNIVERSITY INNOVATION LIMITED whose administrative offices are at
 * University Offices, Wellington Square, Oxford OX1 2JD, United Kingdom ("OUI")
 * for use of BuRST, a generic tool for collating error and debug information from
 * a number of distributed tools, and provides a subscription service so that
 * end-users can be informed of messages ("the Software") through this website
 * https://github.com/OxBRCInformatics/BuRST (the "Website").
 *
 * PLEASE READ THESE LICENCE TERMS CAREFULLY BEFORE DOWNLOADING THE SOFTWARE
 * THROUGH THIS WEBSITE. IF YOU DO NOT AGREE TO THESE LICENCE TERMS YOU SHOULD NOT
 * [REQUEST A USER NAME AND PASSWORD OR] DOWNLOAD THE SOFTWARE.
 *
 * THE SOFTWARE IS INTENDED FOR USE BY ACADEMICS CARRYING OUT RESEARCH AND NOT FOR
 * USE BY CONSUMERS OR COMMERCIAL BUSINESSES.
 *
 * 1. Academic Use Licence
 *
 *   1.1 The Licensee is granted a limited non-exclusive and non-transferable
 *       royalty free licence to download and use the Software provided that the
 *       Licensee will:
 *
 *       (a) limit their use of the Software to their own internal academic
 *           non-commercial research which is undertaken for the purposes of
 *           education or other scholarly use;
 *
 *       (b) not use the Software for or on behalf of any third party or to
 *           provide a service or integrate all or part of the Software into a
 *           product for sale or license to third parties;
 *
 *       (c) use the Software in accordance with the prevailing instructions and
 *           guidance for use given on the Website and comply with procedures on
 *           the Website for user identification, authentication and access;
 *
 *       (d) comply with all applicable laws and regulations with respect to their
 *           use of the Software; and
 *
 *       (e) ensure that the Copyright Notice (c) 2016, Oxford University
 *           Innovation Ltd." appears prominently wherever the Software is
 *           reproduced and is referenced or cited with the Copyright Notice when
 *           the Software is described in any research publication or on any
 *           documents or other material created using the Software.
 *
 *   1.2 The Licensee may only reproduce, modify, transmit or transfer the
 *       Software where:
 *
 *       (a) such reproduction, modification, transmission or transfer is for
 *           academic, research or other scholarly use;
 *
 *       (b) the conditions of this Licence are imposed upon the receiver of the
 *           Software or any modified Software;
 *
 *       (c) all original and modified Source Code is included in any transmitted
 *           software program; and
 *
 *       (d) the Licensee grants OUI an irrevocable, indefinite, royalty free,
 *           non-exclusive unlimited licence to use and sub-licence any modified
 *           Source Code as part of the Software.
 *
 *     1.3 OUI reserves the right at any time and without liability or prior
 *         notice to the Licensee to revise, modify and replace the functionality
 *         and performance of the access to and operation of the Software.
 *
 *     1.4 The Licensee acknowledges and agrees that OUI owns all intellectual
 *         property rights in the Software. The Licensee shall not have any right,
 *         title or interest in the Software.
 *
 *     1.5 This Licence will terminate immediately and the Licensee will no longer
 *         have any right to use the Software or exercise any of the rights
 *         granted to the Licensee upon any breach of the conditions in Section 1
 *         of this Licence.
 *
 * 2. Indemnity and Liability
 *
 *   2.1 The Licensee shall defend, indemnify and hold harmless OUI against any
 *       claims, actions, proceedings, losses, damages, expenses and costs
 *       (including without limitation court costs and reasonable legal fees)
 *       arising out of or in connection with the Licensee's possession or use of
 *       the Software, or any breach of these terms by the Licensee.
 *
 *   2.2 The Software is provided on an "as is" basis and the Licensee uses the
 *       Software at their own risk. No representations, conditions, warranties or
 *       other terms of any kind are given in respect of the the Software and all
 *       statutory warranties and conditions are excluded to the fullest extent
 *       permitted by law. Without affecting the generality of the previous
 *       sentences, OUI gives no implied or express warranty and makes no
 *       representation that the Software or any part of the Software:
 *
 *       (a) will enable specific results to be obtained; or
 *
 *       (b) meets a particular specification or is comprehensive within its field
 *           or that it is error free or will operate without interruption; or
 *
 *       (c) is suitable for any particular, or the Licensee's specific purposes.
 *
 *   2.3 Except in relation to fraud, death or personal injury, OUI"s liability to
 *       the Licensee for any use of the Software, in negligence or arising in any
 *       other way out of the subject matter of these licence terms, will not
 *       extend to any incidental or consequential damages or losses, or any loss
 *       of profits, loss of revenue, loss of data, loss of contracts or
 *       opportunity, whether direct or indirect.
 *
 *   2.4 The Licensee hereby irrevocably undertakes to OUI not to make any claim
 *       against any employee, student, researcher or other individual engaged by
 *       OUI, being a claim which seeks to enforce against any of them any
 *       liability whatsoever in connection with these licence terms or their
 *       subject-matter.
 *
 * 3. General
 *
 *   3.1 Severability - If any provision (or part of a provision) of these licence
 *       terms is found by any court or administrative body of competent
 *       jurisdiction to be invalid, unenforceable or illegal, the other
 *       provisions shall remain in force.
 *
 *   3.2 Entire Agreement - These licence terms constitute the whole agreement
 *       between the parties and supersede any previous arrangement, understanding
 *       or agreement between them relating to the Software.
 *
 *   3.3 Law and Jurisdiction - These licence terms and any disputes or claims
 *       arising out of or in connection with them shall be governed by, and
 *       construed in accordance with, the law of England. The Licensee
 *       irrevocably submits to the exclusive jurisdiction of the English courts
 *       for any dispute or claim that arises out of or in connection with these
 *       licence terms.
 *
 * If you are interested in using the Software commercially, please contact
 * Oxford University Innovation Limited to negotiate a licence.
 * Contact details are enquiries@innovation.ox.ac.uk quoting reference 14422.
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
