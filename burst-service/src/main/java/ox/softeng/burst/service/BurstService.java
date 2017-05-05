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
package ox.softeng.burst.service;

import ox.softeng.burst.domain.report.Message;
import ox.softeng.burst.service.message.RabbitMessageService;
import ox.softeng.burst.service.report.ReportScheduler;
import ox.softeng.burst.service.thread.NamedThreadFactory;
import ox.softeng.burst.util.SeverityEnum;
import ox.softeng.burst.util.Utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.common.base.Strings;
import org.apache.commons.cli.*;
import org.flywaydb.core.Flyway;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Properties;
import java.util.concurrent.*;

public class BurstService {

    private static final Logger logger = LoggerFactory.getLogger(BurstService.class);
    private static final CommandLineParser parser = new DefaultParser();
    private final EntityManagerFactory entityManagerFactory;
    private final String serviceChoice;
    private RabbitMessageService rabbitMessageService;
    private ScheduledExecutorService reportExecutor;
    private ReportScheduler reportScheduler;
    private Integer scheduleFrequency;

    public BurstService(Properties properties) throws IOException, TimeoutException, IllegalStateException {

        logger.info("Starting application version {}", BurstService.class.getPackage().getSpecificationVersion());

        // Output env version from potential dockerfile environment
        if (!Strings.isNullOrEmpty(System.getenv("BURST_VERSION")))
            logger.info("Docker container build version {}", System.getenv("BURST_VERSION"));

        String user = properties.getProperty("hibernate.connection.user");
        String url = properties.getProperty("hibernate.connection.url");
        String password = properties.getProperty("hibernate.connection.password");

        logger.info("Migrating database using: url: {}  user: {} password: ****", url, user);
        migrateDatabase(url, user, password);

        entityManagerFactory = Persistence.createEntityManagerFactory("ox.softeng.burst", properties);

        serviceChoice = properties.getProperty("burst.service", "all");

        switch (serviceChoice) {
            case "message":
                createRabbitMessageService(properties);
                break;
            case "report":
                createReportSchedulerService(properties);
                break;
            default:
                createRabbitMessageService(properties);
                createReportSchedulerService(properties);
        }
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public RabbitMessageService getRabbitMessageService() {
        return rabbitMessageService;
    }

    public void startService() {

        switch (serviceChoice) {
            case "message":
                startRabbitMessageService();
                break;
            case "report":
                startReportService();
                break;
            default:
                startRabbitMessageService();
                startReportService();
        }
        generateStartupMessage();
    }

    public void stopService() {
        logger.warn("Stopping all services");
        reportExecutor.shutdown();
    }

    private void createRabbitMessageService(Properties properties) throws IOException, TimeoutException {
        logger.debug("Creating new rabbit message service");
        rabbitMessageService = new RabbitMessageService(entityManagerFactory, properties);
    }

    private void createReportSchedulerService(Properties properties) {
        logger.debug("Creating new report scheduler");
        reportScheduler = new ReportScheduler(entityManagerFactory, properties);
        scheduleFrequency = Utils.convertToInteger("report.schedule.frequency", properties.getProperty("report.schedule.frequency"), 1);
        reportExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("scheduler"));
    }

    private void generateStartupMessage() {
        Message message = new Message(
                "burst-service",
                "Burst Service starting\n" + version(),
                SeverityEnum.INFORMATIONAL,
                OffsetDateTime.now(ZoneId.of("UTC")),
                "Burst Service Startup"
        );
        message.addTopic("service");
        message.addTopic("startup");
        message.addTopic("burst");
        message.addMetadata("gmc", "gel");
        message.addMetadata("burst_service_version", version());

        try {
            message.save(entityManagerFactory);
        } catch (HibernateException he) {
            logger.error("Could not save startup message to database: " + he.getMessage(), he);
        } catch (Exception e) {
            logger.error("Unhandled exception trying to process startup message", e);
        }
    }

    private void startRabbitMessageService() {
        logger.info("Starting message consumers");
        rabbitMessageService.run();
    }

    private void startReportService() {
        logger.info("Starting report schedule with frequency of {} {}", scheduleFrequency, TimeUnit.MINUTES.name().toLowerCase());
        try {
            reportExecutor.scheduleWithFixedDelay(reportScheduler, 0, scheduleFrequency, TimeUnit.MINUTES);
        } catch (RejectedExecutionException ex) {
            logger.error("Failed to execute report scheduler", ex);
        }
    }

    public static void main(String[] args) {
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(defineOptions(), args);
            if (line.hasOption("h")) help();
            else if (line.hasOption("v")) System.out.println(fullVersion());
            else if (line.hasOption("c")) {
                long start = System.currentTimeMillis();
                try {

                    // Give some information about the logging set-up
                    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
                    StatusPrinter.print(lc);

                    Properties properties = new Properties();
                    properties.load(new FileInputStream(line.getOptionValue('c')));

                    logger.info("Starting burst service\n{}", version());

                    BurstService service = new BurstService(properties);
                    service.startService();

                    logger.info("Burst service started in {}ms", System.currentTimeMillis() - start);
                } catch (IOException ex) {
                    logger.error("Burst service failed due to: " + ex.getMessage(), ex);
                } catch (TimeoutException e) {
                    logger.error("Cannot connect to the rabbit server", e);
                }
            } else {
                help();
            }
        } catch (ParseException exp) {
            logger.error("Could not start burst-service because of ParseException: " + exp.getMessage());
            help();
        } catch (RuntimeException ex) {
            logger.error("Unhandled runtime exception", ex);
        }
    }

    private static Options defineOptions() {

        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(
                Option.builder("c").longOpt("config")
                        .argName("FILE")
                        .hasArg().required()
                        .desc("The config file defining the monitor config")
                        .build());
        group.addOption(Option.builder("h").longOpt("help").build());
        group.addOption(Option.builder("v").longOpt("version").build());
        options.addOptionGroup(group);

        return options;
    }

    private static String fullVersion() {
        return "burst-service " + version();
    }

    private static void help() {
        HelpFormatter formatter = new HelpFormatter();

        String header = "Monitor RabbitMQ queue for messages. \n" +
                        "Save them to the database and generate reporting emails based on subscriptions.\n\n";
        String footer = "\n" + version() + "\n\nPlease report issues at https://github.com/oxbrcinformatics/BuRST/issues\n";

        formatter.printHelp(120,
                            "burst-service -c <FILE>",
                            header, defineOptions(), footer, false);
    }

    private static void migrateDatabase(String url, String user, String password) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(url, user, password);
        flyway.migrate();
    }

    private static String version() {
        return "Version: \"" + BurstService.class.getPackage().getSpecificationVersion() + "\"\n" +
               "Java Version: \"" + System.getProperty("java.version") + "\"";
    }
}
