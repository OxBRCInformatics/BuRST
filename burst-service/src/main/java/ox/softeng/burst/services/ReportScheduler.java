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
package ox.softeng.burst.services;

import ox.softeng.burst.domain.report.Message;
import ox.softeng.burst.domain.subscription.Severity;
import ox.softeng.burst.domain.subscription.Subscription;
import ox.softeng.burst.domain.subscription.User;
import ox.softeng.burst.util.SeverityEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ReportScheduler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReportScheduler.class);
    private final EntityManagerFactory entityManagerFactory;
    private String defaultEmailSubject;
    private String emailsFrom;
    private Long immediateFrequency;
    private Properties properties;
    private String protocol;
    private String smtpPassword;
    private String smtpUsername;


    public ReportScheduler(EntityManagerFactory emf, Properties props) {
        entityManagerFactory = emf;
        emailsFrom = props.getProperty("report.email.from");
        String smtpHost = props.getProperty("report.email.host");
        smtpUsername = props.getProperty("report.email.username");
        smtpPassword = props.getProperty("report.email.password");
        defaultEmailSubject = props.getProperty("report.email.default.subject", "BuRST Reporting Message");
        try {
            immediateFrequency = Long.parseLong(props.getProperty("report.immediate.frequency", "1"));
        } catch (NumberFormatException ignored) {
            logger.warn("Could not convert property [report.immediate.frequency] to integer to setting immediate frequency to 1 minute");
            immediateFrequency = 1L;
        }

        // Get system properties
        properties = System.getProperties();
        // Setup mail server
        properties.setProperty("mail.smtp.host", smtpHost);
        // Get the default Session object.
        protocol = "smtp";
        if (smtpUsername == null || "".equals(smtpUsername)) {
            logger.trace("Sending emails without authentication");
            properties.put("mail." + protocol + ".auth", "false");
        } else {
            logger.trace("Setting email authentication method");
            properties.put("mail." + protocol + ".auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
        }

    }

    @Override
    public void run() {

        try {
            OffsetDateTime now = OffsetDateTime.now();
            List<Subscription> dueSubscriptions = findDueSubscriptions(now);

            if (!dueSubscriptions.isEmpty()) {
                logger.info("Generating reports for {} subscriptions", dueSubscriptions.size());

                for (Subscription s : dueSubscriptions) {

                    User user = s.getSubscriber();
                    logger.trace("Handling subscription: {} for {}", s.getId(), user.getEmailAddress());

                    // For each of those, we find all the matching messages
                    List<Message> matchedMessages = findMessagesForSubscription(s, now, s.getSeverity());
                    logger.debug("Subscription {} has {} matching messages", s.getId(), matchedMessages.size());

                    Map<SeverityEnum, List<Message>> emailContentsMessages = getEmailContentsMessages(matchedMessages);
                    logger.debug("Generating email for {} messages", getNumberOfMessages(emailContentsMessages));

                    sendMessagesToUser(user, emailContentsMessages);

                    // Then we re-calculate the "last sent" and "next send" timestamps and update the record
                    updateSubscription(s, now);
                }
                logger.debug("Reports generated");
            }

            // Finally we find any subscription where the "time of next run" is not set,
            // and put a time on it.
            Subscription.initialiseSubscriptions(entityManagerFactory, immediateFrequency);
        } catch (Exception ex) {
            logger.error("Unhandled report scheduler exception", ex);
        }
    }

    protected String generateEmailContents(Map<SeverityEnum, List<Message>> emailContents, User user) {
        StringBuilder emailContent = new StringBuilder();

        int count = getNumberOfMessages(emailContents);
        String fName = user.getFirstName() == null ? "Unknown" : user.getFirstName();
        String sName = user.getLastName() == null ? "Person" : user.getLastName();
        String grammar = count == 1 ? "message has" : "messages have";
        String header = "To " + fName + " " + sName +
                        "\n\nThe following " + grammar + " been logged in BuRST matching your subscription:\n\n";
        emailContent.append(header);

        if (count == 1) {
            Message msg = emailContents.values().stream().findFirst().get().get(0);
            emailContent.append(msg.getMessage()).append("\n\n");
        } else {
            // Add the messages in severity reverse order.
            // So most important first
            emailContents.keySet().stream()
                    .sorted((o1, o2) -> o1.compareTo(o2) * -1)
                    .forEachOrdered(severityEnum -> {
                        List<Message> msgs = emailContents.get(severityEnum);
                        final int[] left = {msgs.size()};
                        emailContent.append(msgs.size())
                                .append(" ")
                                .append(severityEnum.toString());
                        if (msgs.size() == 1) emailContent.append(" message:\n\n");
                        else emailContent.append(" messages:\n\n");
                        msgs.forEach(msg -> {
                            emailContent.append(msg.getMessage())
                                    .append("\n\n");
                            if (left[0] > 1) {
                                emailContent.append("----\n\n");
                                left[0]--;
                            }
                        });
                        emailContent.append("------ End of ")
                                .append(severityEnum.toString())
                                .append(" messages ------\n\n");
                    });
        }

        emailContent.append("Kind Regards\n\nThe BuRST Service");
        logger.trace("Content: \n{}", emailContent.toString());
        return emailContent.toString();
    }

    protected String generateEmailSubject(Map<SeverityEnum, List<Message>> emailContents) {
        // put the number of each msg severity in the subject title
        StringBuilder emailSubject = new StringBuilder(defaultEmailSubject);
        if (emailContents == null || emailContents.size() == 0) return emailSubject.toString();

        emailSubject.append(": ");

        if (getNumberOfMessages(emailContents) == 1) {
            Message msg = emailContents.values().stream().findFirst().get().get(0);
            if (msg.hasTitle()) {
                emailSubject.append(msg.getTitle());
                return emailSubject.toString();
            }
        }

        final int[] left = {emailContents.size()};
        emailContents.keySet().stream()
                .sorted((o1, o2) -> o1.compareTo(o2) * -1)
                .forEachOrdered(severityEnum -> {
                    Collection<Message> messages = emailContents.get(severityEnum);
                    emailSubject.append(messages.size()).append(" ").append(severityEnum.toString());
                    if (messages.size() > 1) emailSubject.append("s");
                    if (left[0] > 1) {
                        emailSubject.append(", ");
                        left[0]--;
                    }
                });

        return emailSubject.toString();
    }

    protected void sendMessagesToUser(User user, Map<SeverityEnum, List<Message>> emailContentsMessages) {
        if (!emailContentsMessages.isEmpty()) {
            logger.debug("Sending an email to: " + user.getEmailAddress());

            String emailContent = generateEmailContents(emailContentsMessages, user);
            String emailSubject = generateEmailSubject(emailContentsMessages);
            Session session = Session.getDefaultInstance(properties);
            Transport t = null;
            try {
                t = session.getTransport(protocol);
                // Create a default MimeMessage object.
                MimeMessage message = new MimeMessage(session);
                // Set From: header field of the header.
                message.setFrom(new InternetAddress(emailsFrom));
                // Set To: header field of the header.
                message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(user.getEmailAddress()));
                // Set Subject: header field
                message.setSubject(emailSubject);
                // Now set the actual message contents
                message.setText(emailContent);
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
    }

    private List<Subscription> findDueSubscriptions(OffsetDateTime now) {
        // First we find all the subscriptions where the "next time" is less than now
        EntityManager em = entityManagerFactory.createEntityManager();
        TypedQuery<Subscription> subsQuery = em.createNamedQuery("subscription.allDue", Subscription.class);
        subsQuery.setParameter("dateNow", now);
        List<Subscription> dueSubscriptions = subsQuery.getResultList();
        em.close();

        return dueSubscriptions.stream()
                .filter(Subscription::hasTopics)
                .collect(Collectors.toList());
    }

    private List<Message> findMessagesForSubscription(Subscription subscription, OffsetDateTime runTime, Severity severity) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        logger.trace("Creating message query with [Date '{}', Last run Date '{}', Severity '{}']",
                     runTime, subscription.getLastScheduledRun(), severity.getSeverity());
        TypedQuery<Message> msgQuery = entityManager.createNamedQuery("message.MatchedMessages", Message.class);
        msgQuery.setParameter("dateNow", runTime);
        msgQuery.setParameter("lastSentDate", subscription.getLastScheduledRun());
        msgQuery.setParameter("severity", severity.getSeverity().ordinal());
        //msgQuery.setParameter("topics", s.getTopics());
        //msgQuery.setParameter("topicsSize", s.getTopics().size());
        logger.trace("Getting matching messages");
        List<Message> matchedMessages = msgQuery.getResultList();
        entityManager.close();
        // Every topic in the subscription must be contained in the message
        return matchedMessages.stream()
                .filter(message -> subscription.getTopics().stream().allMatch(message.getTopics()::contains))
                .collect(Collectors.toList());
    }

    private Map<SeverityEnum, List<Message>> getEmailContentsMessages(List<Message> messages) {
        Map<SeverityEnum, List<Message>> emailContents = new HashMap<>();
        messages.forEach(msg -> {
            // We send a message with the concatenation of all the messages
            logger.trace("Checking message with {} topics", msg.getTopics().size());
            List<Message> msgs = emailContents.getOrDefault(msg.getSeverity(), new ArrayList<>());
            msgs.add(msg);
            emailContents.put(msg.getSeverity(), msgs);
        });
        return emailContents;
    }

    private int getNumberOfMessages(Map<SeverityEnum, List<Message>> emailContents) {
        Collection<Message> messages = new ArrayList<>();
        emailContents.values().forEach(messages::addAll);
        return messages.size();
    }

    private void updateSubscription(Subscription subscription, OffsetDateTime lastRun) {
        logger.trace("Updating last run time and scheduling next run for {}", subscription.getId());
        subscription.setLastScheduledRun(lastRun);
        subscription.calculateNextScheduledRun(immediateFrequency);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.merge(subscription);
        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
