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
package ox.softeng.burst.service.report;

import org.apache.commons.lang3.StringUtils;
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
    private final ExecutorService httpServiceExecutor;
    private final EntityManagerFactory entityManagerFactory;
    private final Long immediateFrequency;
    private final Properties properties;
    private final Subscription subscription;
    private String emailTransmission;
    private final boolean httpDisabled;
    private String httpTransmission;


    ReportService(EntityManagerFactory entityManagerFactory, ExecutorService emailServiceExecutor,
                  ExecutorService httpServiceExecutor, Properties properties, Subscription subscription,
                  OffsetDateTime currentTime, Long immediateFrequency) {
        this.subscription = subscription;
        this.currentTime = currentTime;
        this.entityManagerFactory = entityManagerFactory;
        this.immediateFrequency = immediateFrequency;
        this.emailServiceExecutor = emailServiceExecutor;
        this.httpServiceExecutor = httpServiceExecutor;
        this.properties = properties;
        this.emailDisabled = properties.containsKey("report.email.disabled");
        this.emailTransmission = properties.getProperty("report.email.transmission");
        this.httpDisabled = properties.containsKey("report.http.disabled");
        this.httpTransmission = properties.getProperty("report.http.transmission");
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

                if (!emailDisabled && StringUtils.isNotEmpty(user.getEmailAddress())) {
                    Map<SeverityEnum, List<Message>> emailContentsMessages = getEmailContentsMessages(matchedMessages);
                    logger.trace("{} - Generating email for {} messages", subscription.getId(), getNumberOfMessages(emailContentsMessages));
                    success = sendMessagesToUser(user, emailContentsMessages);
                }

                if (!httpDisabled && StringUtils.isNotEmpty(user.getEndpointUrl())) {
                    Map<SeverityEnum, List<Message>> contentsMessages = getEmailContentsMessages(matchedMessages);
                    logger.info("{} - Sending HTTP request for {} messages", subscription.getId(), getNumberOfMessages(contentsMessages));
                    success = sendHttpRequest(user, contentsMessages);
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

    protected String generateMessageContents(Map<SeverityEnum, List<Message>> emailContents, User user) {
        // message content is the same for http and email for now
        return generateEmailContents(emailContents, user);
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

    private boolean sendHttpRequest(User user, Map<SeverityEnum, List<Message>> contentsMessages) {
        if (contentsMessages.isEmpty() && httpDisabled) return true;
        String content = generateMessageContents(contentsMessages, user);
        HttpService httpService = new HttpService(user.getEndpointUrl(), content);
        logger.debug("{} - Sending an {} HTTP request to {}", subscription.getId(), httpTransmission, user.getEndpointUrl());
        switch (httpTransmission) {
            case "synchronous":
                httpService.run();
                return httpService.isRequestSent();
            default:
                httpServiceExecutor.submit(httpService);
                return true;
        }
    }

    private void updateSubscription(Subscription subscription, OffsetDateTime lastRun) {
        logger.trace("{} - Updating last run time and scheduling next run", subscription.getId());
        subscription.setLastScheduledRun(lastRun);
        subscription.calculateNextScheduledRun(immediateFrequency);
        logger.trace("{} - Saving subscription", subscription.getId());
        subscription.save(entityManagerFactory);
    }
}
