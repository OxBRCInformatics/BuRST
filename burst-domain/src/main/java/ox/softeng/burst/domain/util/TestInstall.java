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
package ox.softeng.burst.domain.util;

import ox.softeng.burst.domain.report.Message;
import ox.softeng.burst.domain.subscription.Frequency;
import ox.softeng.burst.domain.subscription.Severity;
import ox.softeng.burst.domain.subscription.Subscription;
import ox.softeng.burst.domain.subscription.User;
import ox.softeng.burst.util.FrequencyEnum;
import ox.softeng.burst.util.SeverityEnum;

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
