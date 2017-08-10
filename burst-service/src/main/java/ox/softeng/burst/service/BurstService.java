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

        if (!properties.containsKey("burst.database.migration.disabled")) {
            String user = properties.getProperty("hibernate.connection.user");
            String url = properties.getProperty("hibernate.connection.url");
            String password = properties.getProperty("hibernate.connection.password");

            logger.info("Migrating database using: url: {}  user: {} password: ****", url, user);
            migrateDatabase(url, user, password);
        }

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
                    properties.putAll(System.getProperties());

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
