/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 James Welch
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
import ox.softeng.burst.util.SeverityEnum;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.cli.*;
import org.flywaydb.core.Flyway;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Properties;
import java.util.concurrent.*;

public class BurstService {

    public static final Integer SCHEDULE_FREQUENCY = 1;
    public static final TimeUnit SCHEDULE_FREQUENCY_UNITS = TimeUnit.MINUTES;
    public static final Integer THREAD_POOL_SIZE = 2;
    private static final Logger logger = LoggerFactory.getLogger(BurstService.class);
    private static final CommandLineParser parser = new DefaultParser();
    private final EntityManagerFactory entityManagerFactory;
    private final ScheduledExecutorService executor;
    private final ReportScheduler reportScheduler;
    private final Integer scheduleFrequency;
    private RabbitService rabbitReceiver;

    public BurstService(Properties properties) {

        logger.info("Starting application version {}", System.getProperty("applicationVersion"));

        // Output env version from potential dockerfile environment
        if (System.getenv("BURST_VERSION") != null)
            logger.info("Docker container build version {}", System.getenv("BURST_VERSION"));

        String user = (String) properties.get("hibernate.connection.user");
        String url = (String) properties.get("hibernate.connection.url");
        String password = (String) properties.get("hibernate.connection.password");

        logger.info("Migrating database using: \n" +
                    "  url: {}" +
                    "  user: {}" +
                    "  password: ****", url, user);
        migrateDatabase(url, user, password);
        entityManagerFactory = Persistence.createEntityManagerFactory("ox.softeng.burst", properties);

        String rabbitMQHost = properties.getProperty("rabbitmq.host");
        String rabbitMQExchange = properties.getProperty("rabbitmq.exchange");
        String rabbitMQQueue = properties.getProperty("rabbitmq.queue");
        String rabbitPortStr = properties.getProperty("rabbitmq.port");
        String rabbitUser = properties.getProperty("rabbitmq.user", ConnectionFactory.DEFAULT_USER);
        String rabbitPassword = properties.getProperty("rabbitmq.password", ConnectionFactory.DEFAULT_PASS);

        Integer rabbitPort = ConnectionFactory.DEFAULT_AMQP_PORT;

        try {
            rabbitPort = Integer.parseInt(rabbitPortStr);
        } catch (NumberFormatException ignored) {
            logger.warn("Configuration supplied rabbit port '{}' is not numerical, using default value {}", rabbitPortStr, rabbitPort);
        }

        logger.info("Creating new RabbitMQ Service using: \n" +
                    "  host: {}:{}\n" +
                    "  user: {}:{}\n" +
                    "  exchange: {}\n" +
                    "  queue: {}", rabbitMQHost, rabbitPort, rabbitUser, rabbitPassword, rabbitMQExchange, rabbitMQQueue);
        try {
            rabbitReceiver = new RabbitService(rabbitMQHost, rabbitPort, rabbitUser, rabbitPassword, rabbitMQExchange, rabbitMQQueue,
                                               entityManagerFactory);
        } catch (IOException | TimeoutException e) {
            logger.error("Cannot create RabbitMQ service: " + e.getMessage(), e);
            System.exit(1);
        } catch (JAXBException e) {
            logger.error("Cannot create JAXB unmarshaller for messages: " + e.getMessage(), e);
            System.exit(1);
        }

        logger.info("Creating new report scheduler");
        reportScheduler = new ReportScheduler(entityManagerFactory, properties);
        scheduleFrequency = Integer.parseInt(properties.getProperty("report.schedule.frequency", SCHEDULE_FREQUENCY.toString()));

        logger.info("Creating new executor with thread pool size {}", THREAD_POOL_SIZE);
        executor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
    }

    public void generateStartupMessage() {
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
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.merge(message);
            entityManager.getTransaction().commit();
            entityManager.close();
        } catch (HibernateException he) {
            logger.error("Could not save startup message to database: " + he.getMessage(), he);
        } catch (Exception e) {
            logger.error("Unhandled exception trying to process startup message", e);
        }
    }

    public void startService() {
        logger.info("Starting service with report schedule frequency of {} {}", scheduleFrequency,
                    SCHEDULE_FREQUENCY_UNITS.name().toLowerCase());
        generateStartupMessage();
        executor.execute(rabbitReceiver);
        try {
            executor.scheduleWithFixedDelay(reportScheduler, 0, scheduleFrequency, SCHEDULE_FREQUENCY_UNITS);
        } catch (RejectedExecutionException ex) {
            logger.error("Failed to execute report scheduler", ex);
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
}
