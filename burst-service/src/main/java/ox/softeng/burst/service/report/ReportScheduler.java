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
