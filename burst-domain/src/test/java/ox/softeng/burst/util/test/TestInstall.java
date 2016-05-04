package ox.softeng.burst.util.test;

import ox.softeng.burst.domain.report.Message;
import ox.softeng.burst.domain.subscription.*;

import org.flywaydb.core.Flyway;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class TestInstall {

    private static void migrateDatabase(String url, String user, String password) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(url, user, password);
        flyway.migrate();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, SQLException, IOException {
        try {
            Path p = Paths.get("src/test/resources/config.properties");
            if (!Files.exists(p)) p = Paths.get("burst-domain/src/test/resources/config.properties");

            Properties properties = new Properties();
            properties.load(Files.newBufferedReader(p));


            String user = (String) properties.get("hibernate.connection.user");
            String url = (String) properties.get("hibernate.connection.url");
            String password = (String) properties.get("hibernate.connection.password");
            migrateDatabase(url, user, password);


            EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("ox.softeng.burst", properties);

            EntityManager entityManager = entityManagerFactory.createEntityManager();


            List<String> topicStrings = Arrays.asList("topic 1", "topic 2", "topic 3");

            User u = new User("Test", "User", UUID.randomUUID().toString() + "@test.com", "Oxford");


            TypedQuery<Severity> q = entityManager.createNamedQuery("severity.getSeverity", Severity.class);
            q.setParameter("severityEnum", SeverityEnum.ALERT);
            TypedQuery<Frequency> q2 = entityManager.createNamedQuery("frequency.getFrequency", Frequency.class);
            q2.setParameter("frequencyEnum", FrequencyEnum.DAILY);

            Subscription s = new Subscription(u, q2.getSingleResult(), q.getSingleResult(), topicStrings);

            Message m = new Message("Source System", "Message Details...", SeverityEnum.ERROR, OffsetDateTime.now(ZoneId.of("UTC")), "Test Message");
            m.addTopic("topic 3");


            entityManager.getTransaction().begin();
            entityManager.persist(u);
            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();
            entityManager.persist(s);
            entityManager.merge(m);
            entityManager.getTransaction().commit();
            entityManager.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }
}
