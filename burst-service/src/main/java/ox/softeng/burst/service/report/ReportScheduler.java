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

import ox.softeng.burst.domain.subscription.Subscription;
import ox.softeng.burst.service.thread.NamedThreadFactory;
import ox.softeng.burst.util.Utils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReportScheduler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ReportScheduler.class);
    private final EntityManagerFactory entityManagerFactory;
    private final Long immediateFrequency;
    private final Properties properties;
    private final ExecutorService reportServiceExecutor;
    private ExecutorService emailServiceExecutor;
    private ExecutorService httpServiceExecutor;

    public ReportScheduler(EntityManagerFactory emf, Properties properties) throws IllegalStateException {
        entityManagerFactory = emf;


        this.properties = properties;

        immediateFrequency = Utils.convertToLong("report.immediate.frequency",
                                                 properties.getProperty("report.immediate.frequency"), 1L);

        boolean emailDisabled = properties.containsKey("report.email.disabled");

        // Only setup all the email properties if email is enabled
        if (!emailDisabled) {
            // Get the default Session object.
            String smtpHost = properties.getProperty("report.email.host");
            if (Strings.isNullOrEmpty(smtpHost)) throw new IllegalStateException("An SMTP host must be supplied for emails to be sent");
            properties.setProperty("mail.smtp.host", smtpHost);

            String from = properties.getProperty("report.email.from");
            if (Strings.isNullOrEmpty(from)) throw new IllegalStateException("An email address which the emails will come from must be supplied");

            String defaultEmailSubject = properties.getProperty("report.email.default.subject", "BuRST Reporting Message");
            properties.setProperty("report.email.default.subject", defaultEmailSubject);

            String protocol = properties.getProperty("report.email.protocol", "smtp");
            properties.setProperty("report.email.protocol", protocol);

            String emailTransmission = properties.getProperty("report.email.transmission", "asynchronous");
            properties.setProperty("report.email.transmission", emailTransmission);

            switch (emailTransmission) {
                case "synchronous":
                    logger.info("Emails will be sent synchronously, so the reporting services will wait and know of the result of emails being sent");
                    // If emails are synchronous then the report threads run the emails and not use the email executor
                    break;
                default:
                    logger.warn("Emails will be sent asnychronously, so the reporting services will not know of the result of emails being sent");
                    emailServiceExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("email"));
            }

            String username = properties.getProperty("report.email.username");
            if (Strings.isNullOrEmpty(username)) {
                logger.trace("Sending emails without authentication");
                properties.setProperty("mail." + protocol + ".auth", "false");
            } else {
                logger.trace("Setting email authentication method");
                properties.setProperty("mail." + protocol + ".auth", "true");
                properties.setProperty("mail.smtp.starttls.enable", "true");
            }

            if (properties.containsKey("report.email.disabled")) {
                logger.warn("Email service is disabled, emails will not be sent");
            }
        } else {
            properties.setProperty("report.email.transmission", "disabled");
        }

        boolean httpDisabled = properties.containsKey("report.http.disabled");

        // Only setup http properties if http is enabled
        if (!httpDisabled) {
            String httpTransmission = properties.getProperty("report.http.transmission", "asynchronous");
            properties.setProperty("report.http.transmission", httpTransmission);

            switch (httpTransmission) {
                case "synchronous":
                    logger.info("HTTP requests will be sent synchronously, so the reporting services will wait and know of the result of requests being sent");
                    break;
                default:
                    logger.warn("HTTP requests will be sent asnychronously, so the reporting services will not know of the result of requests being sent");
                    httpServiceExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("http"));
            }
        }

        Integer reportServiceThreadCount = Utils.convertToInteger("report.service.thread.size",
                                                                  properties.getProperty("report.service.thread.size"), 50);

        // Setup one executor and thread pool for the emails and one for the reporting
        reportServiceExecutor = Executors.newFixedThreadPool(reportServiceThreadCount, new NamedThreadFactory("report"));
    }

    @Override
    public void run() {

        try {
            logger.trace("Starting schedule run");
            OffsetDateTime now = OffsetDateTime.now();
            List<Subscription> dueSubscriptions = Subscription.findDueSubscriptions(entityManagerFactory, now);
            List<Future> futures = new ArrayList<>();

            if (!dueSubscriptions.isEmpty()) {
                logger.info("Generating reports for {} subscriptions", dueSubscriptions.size());

                for (Subscription subscription : dueSubscriptions) {
                    ReportService reportService = new ReportService(entityManagerFactory, emailServiceExecutor,
                            httpServiceExecutor, properties, subscription, now, immediateFrequency);
                    futures.add(reportServiceExecutor.submit(reportService));
                }
                logger.debug("Reporting services generated");
            } else {
                logger.trace("No subscriptions due");
            }

            // Wait for all threads/subscriptions to be reported
            boolean complete = false;
            while (!complete) {
                complete = futures.stream().allMatch(future -> future.isCancelled() || future.isDone());
            }

            logger.debug("All reporting services completed");

            // Finally we find any subscription where the "time of next run" is not set,
            // and put a time on it.
            Subscription.initialiseSubscriptions(entityManagerFactory, immediateFrequency);

            logger.info("Report schedule completed");

        } catch (Exception ex) {
            logger.error("Unhandled report scheduler exception", ex);
        }
    }
}
