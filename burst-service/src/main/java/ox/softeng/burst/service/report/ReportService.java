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
package ox.softeng.burst.service.report;

import ox.softeng.burst.domain.report.Message;
import ox.softeng.burst.domain.subscription.Severity;
import ox.softeng.burst.domain.subscription.Subscription;
import ox.softeng.burst.domain.subscription.User;
import ox.softeng.burst.util.SeverityEnum;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.NoSuchProviderException;
import javax.mail.internet.AddressException;
import javax.persistence.EntityManagerFactory;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @since 05/05/2017
 */
public class ReportService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private final OffsetDateTime currentTime;
    private final boolean emailDisabled;
    private final ExecutorService emailServiceExecutor;
    private final EntityManagerFactory entityManagerFactory;
    private final Long immediateFrequency;
    private final Properties properties;
    private final Subscription subscription;
    private String emailTransmission;

    ReportService(EntityManagerFactory entityManagerFactory, ExecutorService emailServiceExecutor, Properties properties, Subscription subscription,
                  OffsetDateTime currentTime, Long immediateFrequency) {
        this.subscription = subscription;
        this.currentTime = currentTime;
        this.entityManagerFactory = entityManagerFactory;
        this.immediateFrequency = immediateFrequency;
        this.emailServiceExecutor = emailServiceExecutor;
        this.properties = properties;
        this.emailDisabled = properties.containsKey("report.email.disabled");
        this.emailTransmission = properties.getProperty("report.email.transmission");
    }

    @Override
    public void run() {
        try {
            User user = subscription.getSubscriber();
            logger.trace("{} - Handling subscription for {}", subscription.getId(), user.getEmailAddress());

            // For each of those, we find all the matching messages
            List<Message> matchedMessages = findMessagesForSubscription(subscription, currentTime, subscription.getSeverity());
            boolean success = matchedMessages.isEmpty();

            if (!matchedMessages.isEmpty()) {
                logger.debug("{} - Subscription has {} matching messages", subscription.getId(), matchedMessages.size());
                success = emailDisabled;

                if (!emailDisabled) {
                    Map<SeverityEnum, List<Message>> emailContentsMessages = getEmailContentsMessages(matchedMessages);
                    logger.trace("{} - Generating email for {} messages", subscription.getId(), getNumberOfMessages(emailContentsMessages));
                    success = sendMessagesToUser(user, emailContentsMessages);
                }
            }

            // Then we re-calculate the "last sent" and "next send" timestamps and update the record
            if (success) updateSubscription(subscription, currentTime);

            switch (emailTransmission) {
                case "synchronous":
                    logger.debug("{} - Subscription completed with email sent: {}", subscription.getId(), success);
                    break;
                case "disabled":
                    logger.debug("{} - Subscription completed with email not sent", subscription.getId());
                    break;
                default:
                    logger.debug("{} - Subscription completed with email status unknown", subscription.getId());
            }

        } catch (Exception ex) {
            logger.error(subscription.getId() + " - Unhandled exception occurred", ex);
        }
    }

    protected String generateEmailContents(Map<SeverityEnum, List<Message>> emailContents, User user) {
        StringBuilder emailContent = new StringBuilder();

        int count = getNumberOfMessages(emailContents);
        String fName = Strings.isNullOrEmpty(user.getFirstName()) ? "Unknown" : user.getFirstName();
        String sName = Strings.isNullOrEmpty(user.getLastName()) ? "Person" : user.getLastName();
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
        return emailContent.toString();
    }

    protected String generateEmailSubject(Map<SeverityEnum, List<Message>> emailContents) {
        // put the number of each msg severity in the subject title
        StringBuilder emailSubject = new StringBuilder(properties.getProperty("report.email.default.subject"));

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

    private List<Message> findMessagesForSubscription(Subscription subscription, OffsetDateTime runTime, Severity severity) {
        List<Message> matchedMessages = Message.findAllMessagesBySeverityBetweenTime(entityManagerFactory, severity,
                                                                                     subscription.getLastScheduledRun(), runTime);

        // Every topic in the subscription must be contained in the message
        List<Message> clean = matchedMessages.stream()
                .filter(message -> subscription.getTopics().stream().allMatch(message.getTopics()::contains))
                .collect(Collectors.toList());
        logger.trace("{} - Obtained {} messages for subscription", subscription.getId(), clean.size());
        return clean;
    }

    private Map<SeverityEnum, List<Message>> getEmailContentsMessages(List<Message> messages) {
        Map<SeverityEnum, List<Message>> emailContents = new HashMap<>();
        messages.forEach(msg -> {
            // We send a message with the concatenation of all the messages
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

    private boolean sendMessagesToUser(User user, Map<SeverityEnum, List<Message>> emailContentsMessages) {
        if (emailContentsMessages.isEmpty() && emailDisabled) return true;

        String emailContent = generateEmailContents(emailContentsMessages, user);
        String emailSubject = generateEmailSubject(emailContentsMessages);
        try {
            EmailService emailService = new EmailService(properties, user.getEmailAddress(), emailSubject, emailContent);

            logger.debug("{} - Sending an {} email to {}", subscription.getId(), emailTransmission, user.getEmailAddress());
            switch (emailTransmission) {
                case "synchronous":
                    emailService.run();
                    return emailService.isEmailSent();
                default:
                    emailServiceExecutor.submit(emailService);
                    return true;
            }

        } catch (NoSuchProviderException e) {
            logger.error("{} - Could not create email transport connection, email will not be sent", subscription.getId());
        } catch (AddressException e) {
            logger.error("{} - Could not send email as email addresses could not be resolved", subscription.getId());
        }
        return false;
    }

    private void updateSubscription(Subscription subscription, OffsetDateTime lastRun) {
        logger.trace("{} - Updating last run time and scheduling next run", subscription.getId());
        subscription.setLastScheduledRun(lastRun);
        subscription.calculateNextScheduledRun(immediateFrequency);
        logger.trace("{} - Saving subscription", subscription.getId());
        subscription.save(entityManagerFactory);
    }
}
