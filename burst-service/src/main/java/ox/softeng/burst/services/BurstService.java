package ox.softeng.burst.services;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BurstService {

    public static final Integer SCHEDULE_FREQUENCY = 1;
    public static final TimeUnit SCHEDULE_FREQUENCY_UNITS = TimeUnit.MINUTES;
    public static final Integer THREAD_POOL_SIZE = 2;
    private static final Logger logger = LoggerFactory.getLogger(BurstService.class);
    private static final CommandLineParser parser = new DefaultParser();
    ScheduledExecutorService executor;
    Runnable rabbitReceiver;
    ReportScheduler reportScheduler;

    public BurstService(Properties properties) {

        logger.info("Starting application version {}", System.getProperty("applicationVersion"));

        // Output env version from potential dockerfile environment
        if (System.getenv("BURST_VERSION") != null)
            logger.info("Docker container build version {}", System.getenv("BURST_VERSION"));

        logger.info("Connecting to database using: \n{}",
                    properties.stringPropertyNames().stream()
                            .filter(key -> key.startsWith("hibernate"))
                            .map(key -> "  " +
                                        key.replaceFirst("hibernate\\.", "").replaceAll("\\.", " ") +
                                        ": " + properties.getProperty(key) + "\n"
                                ).collect(Collectors.toList()));
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("ox.softeng.burst", properties);

        String RabbitMQHost = properties.getProperty("RabbitMQHost");
        String RabbitMQExchange = properties.getProperty("RabbitMQExchange");
        String RabbitMQQueue = properties.getProperty("RabbitMQQueue");

        logger.info("Creating new RabbitMQ Service using: \n" +
                    "  host: {}" +
                    "  exchange: {}" +
                    "  queue: {}", RabbitMQHost, RabbitMQExchange, RabbitMQQueue);
        rabbitReceiver = new RabbitService(RabbitMQHost, RabbitMQExchange, RabbitMQQueue, entityManagerFactory);

        logger.info("Creating new report scheduler");
        reportScheduler = new ReportScheduler(entityManagerFactory, properties);

        logger.info("Creating new executor with thread pool size {}", THREAD_POOL_SIZE);
        executor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
    }

    public void startService() {
        logger.info("Starting service with report schedule frequency of {} {}", SCHEDULE_FREQUENCY, SCHEDULE_FREQUENCY_UNITS.name().toLowerCase());
        executor.execute(rabbitReceiver);
        executor.scheduleAtFixedRate(reportScheduler, 0, SCHEDULE_FREQUENCY, SCHEDULE_FREQUENCY_UNITS);
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

    private static void help() {
        HelpFormatter formatter = new HelpFormatter();

        String header = "Monitor RabbitMQ queue for messages. \n" +
                        "Save them to the database and generate reporting emails based on subscriptions.\n\n";
        String footer = "\n" + version() + "\n\nPlease report issues at https://github.com/oxbrcinformatics/BuRST/issues\n";

        formatter.printHelp(120,
                            "burst-service -c <FILE>",
                            header, defineOptions(), footer, false);
    }

    private static String version() {
        return "burst-service version: \"" + BurstService.class.getPackage().getSpecificationVersion() + "\"\n" +
               "Java Version: \"" + System.getProperty("java.version") + "\"";

    }

    public static void main(String[] args) throws IOException {
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(defineOptions(), args);
            if (line.hasOption("h")) help();
            else if (line.hasOption("v")) System.out.println(version());
            else if (line.hasOption("c")) {
                long start = System.currentTimeMillis();
                try {

                    Properties properties = new Properties();
                    properties.load(new FileInputStream(line.getOptionValue('c')));

                    BurstService service = new BurstService(properties);
                    service.startService();

                    logger.info("Burst service started in {}ms", System.currentTimeMillis() - start);
                } catch (IOException ex) {
                    logger.error("Could not create burst service because: " + ex.getMessage(), ex);
                }
            } else {
                help();
            }
        } catch (ParseException exp) {
            logger.error("Could not start burst-service because of ParseException: " + exp.getMessage());
            help();
        }
    }
}
