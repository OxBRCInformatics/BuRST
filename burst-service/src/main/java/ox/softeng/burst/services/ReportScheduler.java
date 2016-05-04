package ox.softeng.burst.services;

import ox.softeng.burst.domain.subscription.Severity;
import ox.softeng.burst.domain.subscription.Subscription;

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
import java.util.Arrays;
import java.util.Collection;
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

        try {
            logger.info("Generating reports");

            OffsetDateTime now = OffsetDateTime.now();

            // First we find all the subscriptions where the "next time" is less than now
            EntityManager em = entityManagerFactory.createEntityManager();
            TypedQuery<Subscription> subsQuery = em.createNamedQuery("subscription.allDue", Subscription.class);
            subsQuery.setParameter("dateNow", now);
            List<Subscription> dueSubscriptions = subsQuery.getResultList();
            em.close();

            logger.info("Handling {} active subscriptions", dueSubscriptions.size());
            for (Subscription s : dueSubscriptions) {
                logger.debug("Calculating number of topics");
                Collection<String> sTopics = Arrays.asList(s.getTopics().split(","));
                if (sTopics.size() == 0) {
                    logger.warn("Subscription {} has no registered topics", s.getId());
                } else {
                    logger.debug("Handling subscription: {}", s.getId());

                    // For each of those, we find all the matching messages
                    List<ox.softeng.burst.domain.report.Message> matchedMessages = findMessagesForSubscription(s, now, s.getSeverity());

                    String emailContent = "";
                    int count = 0;
                    logger.debug("Generating email content for {} subscription topics", sTopics.size());
                    String msgSubject = defaultEmailSubject;
                    for (ox.softeng.burst.domain.report.Message msg : matchedMessages) {
                        logger.debug("Checking message with {} topics", msg.getTopics().size());
                        // Now we ensure the subscribed topics are a subset of the message topics
                        // Can't get this working within the query itself
                        if (msg.getTopics().containsAll(sTopics)) {
                            // We send a message with the concatenation of all the messages
                            emailContent += msg.getMessage();
                            emailContent += "\n\n";
                            msgSubject = msg.getTitle();
                            count++;
                        }
                    }
                    logger.debug("Email generated for {} messages", count);

                    if (count > 0) {
                        logger.info("Sending an email to: " + s.getSubscriber().getEmailAddress());
                        logger.debug("Content: \n{}", emailContent);
                        String subj = count == 1 ? msgSubject : defaultEmailSubject;
                        sendMessage(s.getSubscriber().getEmailAddress(), subj, emailContent);
                    }

                    // Then we re-calculate the "last sent" and "next send" timestamps and update the record
                    updateSubscription(s, now);
                }
            }


            // Finally we find any subscription where the "time of next run" is not set,
            // and put a time on it.
            initialiseSubscriptions();

            logger.info("Reports generated");
        } catch (Exception ex) {
            logger.error("Unhandled exception: " + ex.getMessage(), ex);
        }
    }

    private List<ox.softeng.burst.domain.report.Message> findMessagesForSubscription(Subscription subscription, OffsetDateTime runTime,
                                                                                     Severity severity) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        logger.debug("Creating message query with [Date '{}', Last run Date '{}', Severity '{}']",
                     runTime, subscription.getLastScheduledRun(), severity.getSeverity());
        TypedQuery<ox.softeng.burst.domain.report.Message> msgQuery = entityManager.createNamedQuery("message.MatchedMessages",
                                                                                                     ox.softeng.burst.domain.report.Message.class);
        msgQuery.setParameter("dateNow", runTime);
        msgQuery.setParameter("lastSentDate", subscription.getLastScheduledRun());
        msgQuery.setParameter("severity", severity.getSeverity().ordinal());
        //msgQuery.setParameter("topics", s.getTopics());
        //msgQuery.setParameter("topicsSize", s.getTopics().size());
        logger.debug("Getting matching messages");
        List<ox.softeng.burst.domain.report.Message> matchedMessages = msgQuery.getResultList();
        logger.info("Subscription has {} currently matching messages", matchedMessages.size());
        entityManager.close();

        return matchedMessages;
    }

    private void initialiseSubscriptions() {
        EntityManager badSubsEm = entityManagerFactory.createEntityManager();
        TypedQuery<Subscription> badSubsQuery = badSubsEm.createNamedQuery("subscription.unInitialised", Subscription.class);
        List<Subscription> uninitialisedSubscriptions = badSubsQuery.getResultList();
        logger.debug("Initialising {} subscriptions", uninitialisedSubscriptions.size());
        badSubsEm.close();
        for (Subscription s : uninitialisedSubscriptions) {
            logger.debug("Scheduling new run for {}", s.getId());
            EntityManager schedEm = entityManagerFactory.createEntityManager();
            schedEm.getTransaction().begin();
            s.calculateNextScheduledRun();
            schedEm.merge(s);
            schedEm.getTransaction().commit();
            schedEm.close();
        }
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
            logger.debug("Sending without authentication");
            properties.put("mail." + protocol + ".auth", "false");
        } else {
            logger.debug("Setting authentication method");
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

            logger.info("Sent message successfully");
        } catch (MessagingException mex) {
            logger.error("Error whilst sending email: " + mex.getMessage(), mex);
        } finally {
            try {
                t.close();
            } catch (NullPointerException | MessagingException ignored) {}
        }
    }

    private void updateSubscription(Subscription subscription, OffsetDateTime lastRun) {
        logger.debug("Updating last run time and scheduling next run for {}", subscription.getId());
        subscription.setLastScheduledRun(lastRun);
        subscription.calculateNextScheduledRun();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.merge(subscription);
        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
