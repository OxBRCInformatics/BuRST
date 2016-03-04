package ox.softeng.burst.services;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import ox.softeng.burst.domain.Frequency;
import ox.softeng.burst.domain.Severity;
import ox.softeng.burst.domain.Subscription;
import ox.softeng.burst.domain.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;

public class Main {

	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		// Arguments:
		// rabbitmq host name
		// rabbitmq exchange name
		// rabbitmq queue name
		// database server
		// database db name
		// database username
		// database password
		
		if(args.length < 1)
		{
			System.err.println("Usage: Main config.properties");
			System.exit(0);
		}
		
		Properties props = new Properties();
		props.load(new FileInputStream(new File(args[0])));
		
		String RabbitMQHost = props.getProperty("RabbitMQHost");
		String RabbitMQExchange = props.getProperty("RabbitMQExchange");
		String RabbitMQQueue = props.getProperty("RabbitMQQueue");
		
		System.out.println("RabbitMQ Host Name: " + RabbitMQHost);
		System.out.println("RabbitMQ Exchange Name: " + RabbitMQExchange);
		System.out.println("RabbitMQ Queue Name: " + RabbitMQQueue);
		
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory( "ox.softeng.burst", props);
		
		
		User u = new User("James", "Welch", "jamesrwelch@gmail.com");
		
		Subscription s = new Subscription(u, Frequency.IMMEDIATE, Severity.DEBUG);
		s.addTopic("File Receipt");
		EntityManager em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		em.persist(u);
		em.persist(s);
		em.getTransaction().commit();
		em.close();
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
		Runnable rabbitReceiver = new RabbitService(RabbitMQHost, RabbitMQExchange, RabbitMQQueue, entityManagerFactory);
		executor.execute(rabbitReceiver);
		ReportScheduler repSched = new ReportScheduler(entityManagerFactory, props);
		
		executor.scheduleAtFixedRate(repSched, 0, 1, TimeUnit.MINUTES);
	}
	
	
}
