package ox.softeng.burst;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import ox.softeng.burst.domain.Message;
import ox.softeng.burst.domain.Severity;
import ox.softeng.burst.domain.Topic;
import ox.softeng.burst.domain.User;

public class TestInstall {

	public static void main(String[] args) throws NoSuchAlgorithmException, SQLException, IOException {

		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory( "ox.softeng.burst" );
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		List<String> topicStrings = Arrays.asList("topic 1","topic 2", "topic 3");
		
		
		User u = new User("James", "Welch", "jamesrwelch@gmail.com");
		entityManager.getTransaction().begin();
		entityManager.persist(u);
		for(String t : topicStrings)
		{
			entityManager.persist(new Topic(t));
		}
		Message m = new Message("Source System","Message Details...", Severity.ERROR, LocalDateTime.now());
		m.addTopic(new Topic("topic 3"));
		entityManager.merge(m);
		entityManager.getTransaction().commit();
		entityManager.close();
	}	
}
