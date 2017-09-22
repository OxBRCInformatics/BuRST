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
package ox.softeng.burst.domain.subscription;

import ox.softeng.burst.domain.util.DomainClass;
import ox.softeng.burst.util.FrequencyEnum;
import ox.softeng.burst.util.SeverityEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Entity
@Table(name = "subscription", schema = "subscription",
       indexes = {
               @Index(columnList = "last_scheduled_run,severity_id", name = "index_lsr_si"),
               @Index(columnList = "next_scheduled_run,last_scheduled_run", name = "index_nsr_lsr"),
               @Index(columnList = "next_scheduled_run,last_scheduled_run,next_scheduled_run", name = "index_nsr_lsr_nsr"),
               @Index(columnList = "next_scheduled_run", name = "index_nsr")
       })
@NamedQueries({
                      @NamedQuery(name = "subscription.all_due",
                                  query = "select s from Subscription s" +
                                          " join fetch s.subscriber u" +
                                          " join fetch s.frequency f" +
                                          " join fetch s.severity sev" +
                                          " where s.nextScheduledRun is not null" +
                                          " and s.lastScheduledRun is not null" +
                                          " and s.nextScheduledRun < :dateNow "),
                      @NamedQuery(name = "subscription.uninitialised",
                                  query = "select s from Subscription s" +
                                          " join fetch s.frequency f" +
                                          " where s.nextScheduledRun is null" +
                                          " or s.lastScheduledRun is null "),
                      @NamedQuery(name = "subscription.find_by_user_and_topics",
                                  query = "select s from Subscription s" +
                                          " join fetch s.subscriber u" +
                                          " where s.subscriber = :subscriber" +
                                          " and s.topicsString = :topics")
              })
@SequenceGenerator(name = "subscriptionIdSeq", sequenceName = "subscription.subscription_id_seq", allocationSize = 1)
public class Subscription extends DomainClass implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(Subscription.class);

    private static final long serialVersionUID = 1L;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frequency_id", foreignKey = @ForeignKey(name = "fk_subscription_frequency"))
    protected Frequency frequency;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subscriptionIdSeq")
    protected Long id = null;
    @Column(name = "last_scheduled_run")
    protected OffsetDateTime lastScheduledRun;
    @Column(name = "next_scheduled_run")
    protected OffsetDateTime nextScheduledRun;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "severity_id", foreignKey = @ForeignKey(name = "fk_subscription_severity"))
    protected Severity severity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_subscription_users"))
    protected User subscriber;


    @Column(name = "topics")
    protected String topicsString;

    public Subscription(User subscriber, Frequency frequency, Severity severity) {
        this(subscriber, frequency, severity, "");
    }

    public Subscription(User subscriber, Frequency frequency, Severity severity, Collection<String> topics) {
        this(subscriber, frequency, severity, topics.stream().map(String::trim).collect(Collectors.joining(",")));
    }

    public Subscription(User subscriber, FrequencyEnum frequency, SeverityEnum severity, String topics) {
        this(subscriber, new Frequency(frequency), new Severity(severity), topics);
    }

    public Subscription(User subscriber, Frequency frequency, Severity severity, String topics) {
        this.subscriber = subscriber;
        this.frequency = frequency;
        this.severity = severity;
        this.topicsString = topics;
    }

    public Subscription() {
    }

    public void addTopic(String topic) {
        addTopics(topic.trim());
    }

    public void addTopics(Collection<String> topicCollection) {
        addTopics(topicCollection.stream().map(String::trim).collect(Collectors.joining(",")));
    }

    public void addTopics(String topics) {
        if (topicsString == null || topicsString.isEmpty()) topicsString = topics;
        else topicsString += "," + topics.trim();
    }

    public void calculateNextScheduledRun(Long immediateFrequency) {
        if (lastScheduledRun == null) {
            lastScheduledRun = OffsetDateTime.now();
        }
        switch (getFrequency().getFrequency()) {
            case IMMEDIATE:
                setNextScheduledRun(lastScheduledRun.plusMinutes(immediateFrequency));
                break;
            case DAILY:
                setNextScheduledRun(lastScheduledRun.plusDays(1));
                break;
            case WEEKLY:
                setNextScheduledRun(lastScheduledRun.plusWeeks(1));
                break;
            case MONTHLY:
                setNextScheduledRun(lastScheduledRun.plusMonths(1));
                break;
            default:
                setNextScheduledRun(null);
        }
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OffsetDateTime getLastScheduledRun() {
        return lastScheduledRun;
    }

    public void setLastScheduledRun(OffsetDateTime lastScheduledRun) {
        this.lastScheduledRun = lastScheduledRun;
    }

    public OffsetDateTime getNextScheduledRun() {
        return nextScheduledRun;
    }

    public void setNextScheduledRun(OffsetDateTime nextScheduledRun) {
        this.nextScheduledRun = nextScheduledRun;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public User getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(User subscriber) {
        this.subscriber = subscriber;
    }

    public Set<String> getTopics() {
        if (topicsString == null || topicsString.isEmpty()) return new HashSet<>();
        return Arrays.stream(topicsString.split(",")).map(String::trim).collect(Collectors.toSet());
    }

    public void setTopics(Set<String> topics) {
        this.topicsString = topics.stream().map(String::trim).collect(Collectors.joining(","));
    }

    public String getTopicsString() {
        return topicsString;
    }

    public void setTopicsString(String topics) {
        this.topicsString = topics;
    }

    public boolean hasTopics() {
        return !getTopics().isEmpty();
    }

    public void setTopics(List<String> topics) {
        setTopics(new HashSet<>(topics));
    }

    public static List<Subscription> findDueSubscriptions(EntityManagerFactory entityManagerFactory, OffsetDateTime now) {
        logger.trace("Searching for due subscriptions");
        // First we find all the subscriptions where the "next time" is less than now
        EntityManager em = entityManagerFactory.createEntityManager();
        TypedQuery<Subscription> subsQuery = em.createNamedQuery("subscription.all_due", Subscription.class);
        subsQuery.setParameter("dateNow", now);
        List<Subscription> dueSubscriptions = subsQuery.getResultList();
        em.close();
        List<Subscription> clean = dueSubscriptions.stream()
                .filter(Subscription::hasTopics)
                .collect(Collectors.toList());
        logger.trace("Found {} due subscriptions for {}", clean.size(), now.toString());
        return clean;
    }

    public static Subscription findOrCreate(EntityManagerFactory entityManagerFactory, Subscription subscription) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TypedQuery<Subscription> query = entityManager.createNamedQuery("subscription.find_by_user_and_topics", Subscription.class);
        query.setParameter("subscriber", subscription.subscriber);
        query.setParameter("topics", subscription.topicsString);
        List<Subscription> subscriptions = query.getResultList();
        entityManager.close();
        if (subscriptions.isEmpty()) return subscription;
        return subscriptions.get(0);
    }

    /**
     * Determines subscriptions that have not yet been initialised (scheduled) and initialises
     * them.
     * 
     * @param entityManagerFactory {@link EntityManagerFactory}
     * @param immediateFrequency used to calculate the next scheduled run of a subscription that
     * 		  has not been initialised (scheduled).
     */
    public static void initialiseSubscriptions(EntityManagerFactory entityManagerFactory, Long immediateFrequency) {
        logger.debug("Checking for subscriptions to initialise...");
        
        EntityManager entityManager = null;

        try
        {
          entityManager = entityManagerFactory.createEntityManager();
          
          TypedQuery<Subscription> subsToInitialiseQuery = entityManager.createNamedQuery("subscription.uninitialised", Subscription.class);
          List<Subscription> uninitialisedSubscriptions = subsToInitialiseQuery.getResultList();
          

          if (uninitialisedSubscriptions.size() > 0) 
          {
              logger.debug("Initialising {} subscriptions", uninitialisedSubscriptions.size());
              
              for (Subscription s : uninitialisedSubscriptions) 
              {
                  logger.debug("Scheduling new run for {} subscription {}", s.getSubscriber().emailAddress, s.getId());

                  entityManager.getTransaction().begin();
                  s.calculateNextScheduledRun(immediateFrequency);
                  entityManager.merge(s);
                  entityManager.getTransaction().commit(); 
              }
          }
          
          logger.trace("Subscriptions all initialised");
        }
        catch (Exception ex)
        {
          logger.info("Failed to process unintialised subscriptions: " + ex.getStackTrace());
        }
        finally
        {
          entityManager.close();
        }

    }
}
