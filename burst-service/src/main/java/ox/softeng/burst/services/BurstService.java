package ox.softeng.burst.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        Runnable rabbitReceiver = new RabbitService(RabbitMQHost, RabbitMQExchange, RabbitMQQueue, entityManagerFactory);

        logger.info("Creating new Report Scheduler");
        ReportScheduler reportScheduler = new ReportScheduler(entityManagerFactory, properties);

        logger.info("Creating new executor with report schedule frequency of {} {}",
                    SCHEDULE_FREQUENCY, SCHEDULE_FREQUENCY_UNITS.name().toLowerCase());
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
        executor.execute(rabbitReceiver);
        executor.scheduleAtFixedRate(reportScheduler, 0, SCHEDULE_FREQUENCY, SCHEDULE_FREQUENCY_UNITS);

    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        // Arguments:
        // rabbitmq host name
        // rabbitmq exchange name
        // rabbitmq queue name
        // database server
        // database db name
        // database username
        // database password

        if (args.length < 1) {
            System.err.println("Usage: BurstService config.properties");
            System.exit(0);
        }
        long start = System.currentTimeMillis();

        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(args[0])));

        new BurstService(properties);

        logger.info("Burst Service started in {}ms", System.currentTimeMillis() - start);
    }


}
