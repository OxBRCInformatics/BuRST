package ox.softeng.burst.services;

import ox.softeng.burst.domain.subscription.Severity;
import ox.softeng.burst.domain.subscription.Subscription;
import ox.softeng.burst.domain.subscription.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Properties;

public class ReportScheduler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReportScheduler.class);
    private final EntityManagerFactory entityManagerFactory;
    private String defaultEmailSubject;
    private String emailsFrom;
    private String smtpHost;
    private String smtpPassword;
    private String smtpUsername;

    public ReportScheduler(EntityManagerFactory emf, Properties props) {
        entityManagerFactory = emf;
        emailsFrom = props.getProperty("smtp-from");
        smtpHost = props.getProperty("smtp-host");
        smtpUsername = props.getProperty("smtp-username");
        smtpPassword = props.getProperty("smtp-password");
        defaultEmailSubject = props.getProperty("default-email-subject", "Burst Reporting Message");
    }

    @Override
    public void run() {

        OffsetDateTime now = OffsetDateTime.now();

        // First we find all the subscriptions where the "next time" is less than now
        EntityManager em = entityManagerFactory.createEntityManager();
        TypedQuery<Subscription> subsQuery = em.createNamedQuery("subscription.allDue", Subscription.class);
        subsQuery.setParameter("dateNow", now);
        List<Subscription> dueSubscriptions = subsQuery.getResultList();
        em.close();

        if (dueSubscriptions.size() > 0) {
            logger.info("Generating reports for {} subscriptions", dueSubscriptions.size());

            for (Subscription s : dueSubscriptions) {
                logger.trace("Calculating number of topics");
                // Ensure the topics are all split, to handle comma delimited topics
                s.tidyUpTopics(entityManagerFactory);
                if (s.getTopics().size() == 0) {
                    logger.warn("Subscription {} for {} has no registered topics", s.getId(), s.getSubscriber().getEmailAddress());
                } else {
                    logger.trace("Handling subscription: {}", s.getId());
                    User user = s.getSubscriber();

                    // For each of those, we find all the matching messages
                    List<ox.softeng.burst.domain.report.Message> matchedMessages = findMessagesForSubscription(s, now, s.getSeverity());

                    StringBuilder emailContent = new StringBuilder();
                    int count = 0;
                    logger.trace("Generating email content for {} subscription topics", s.getTopics().size());
                    String msgSubject = defaultEmailSubject;
                    for (ox.softeng.burst.domain.report.Message msg : matchedMessages) {
                        logger.trace("Checking message with {} topics", msg.getTopics().size());

                        // Can't get this working within the query itself
                        // Every topic in the subscription must be contained in the message
                        if (s.getTopics().stream().allMatch(msg.getTopics()::contains)) {
                            // We send a message with the concatenation of all the messages
                            emailContent.append(msg.getMessage()).append("\n\n----\n\n");
                            msgSubject = msg.getTitle();
                            count++;
                        }
                    }
                    logger.debug("Email generated for {} messages", count);

                    if (count > 0) {

                        String fName = user.getFirstName() == null ? "Unknown" : user.getFirstName();
                        String sName = user.getLastName() == null ? "Person" : user.getLastName();
                        String grammar = count == 1 ? "message has" : "messages have";
                        String header = "To " + fName + " " + sName +
                                        "\n\nThe following " + grammar + " been logged in BuRST matching your subscription:\n\n";

                        emailContent.insert(0, header);
                        emailContent.append("kind regards\n\nThe BuRST Service");

                        logger.debug("Sending an email to: " + s.getSubscriber().getEmailAddress());
                        logger.trace("Content: \n{}", emailContent.toString());
                        String subj = count == 1 ? msgSubject : defaultEmailSubject;
                        sendMessage(user.getEmailAddress(), subj, emailContent.toString());
                    }

                    // Then we re-calculate the "last sent" and "next send" timestamps and update the record
                    updateSubscription(s, now);
                }
            }
            logger.debug("Reports generated");
        }

        // Finally we find any subscription where the "time of next run" is not set,
        // and put a time on it.
        Subscription.initialiseSubscriptions(entityManagerFactory);
    }

    private List<ox.softeng.burst.domain.report.Message> findMessagesForSubscription(Subscription subscription, OffsetDateTime runTime,
                                                                                     Severity severity) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        logger.trace("Creating message query with [Date '{}', Last run Date '{}', Severity '{}']",
                     runTime, subscription.getLastScheduledRun(), severity.getSeverity());
        TypedQuery<ox.softeng.burst.domain.report.Message> msgQuery = entityManager.createNamedQuery("message.MatchedMessages",
                                                                                                     ox.softeng.burst.domain.report.Message.class);
        msgQuery.setParameter("dateNow", runTime);
        msgQuery.setParameter("lastSentDate", subscription.getLastScheduledRun());
        msgQuery.setParameter("severity", severity.getSeverity().ordinal());
        //msgQuery.setParameter("topics", s.getTopics());
        //msgQuery.setParameter("topicsSize", s.getTopics().size());
        logger.trace("Getting matching messages");
        List<ox.softeng.burst.domain.report.Message> matchedMessages = msgQuery.getResultList();
        logger.debug("Subscription {} has {} potential matching messages", subscription.getId(), matchedMessages.size());
        entityManager.close();

        return matchedMessages;
    }

    private void sendMessage(String emailAddress, String subject, String contents) {
        // Get system properties
        Properties properties = System.getProperties();
        // Setup mail server
        properties.setProperty("mail.smtp.host", smtpHost);
        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);
        String protocol = "smtp";
        if (smtpUsername == null || "".equals(smtpUsername)) {
            logger.trace("Sending without authentication");
            properties.put("mail." + protocol + ".auth", "false");
        } else {
            logger.trace("Setting authentication method");
            properties.put("mail." + protocol + ".auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
        }
        Transport t = null;
        try {
            t = session.getTransport(protocol);
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            // Set From: header field of the header.
            message.setFrom(new InternetAddress(emailsFrom));
            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));
            // Set Subject: header field
            message.setSubject(subject);
            // Now set the actual message contents
            message.setText(contents);
            // Send message
            if (smtpUsername == null || "".equals(smtpUsername)) {
                t.connect();
            } else {
                t.connect(smtpUsername, smtpPassword);
            }
            t.sendMessage(message, message.getAllRecipients());

            logger.debug("Sent message successfully");
        } catch (MessagingException mex) {
            logger.error("Error whilst sending email: " + mex.getMessage(), mex);
        } finally {
            try {
                if (t != null) {
                    t.close();
                }
            } catch (MessagingException ignored) {}
        }
    }

    private void updateSubscription(Subscription subscription, OffsetDateTime lastRun) {
        logger.trace("Updating last run time and scheduling next run for {}", subscription.getId());
        subscription.setLastScheduledRun(lastRun);
        subscription.calculateNextScheduledRun();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.merge(subscription);
        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
