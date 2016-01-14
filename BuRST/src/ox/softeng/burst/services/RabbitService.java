package ox.softeng.burst.services;

import com.rabbitmq.client.*;

import ox.softeng.burst.domain.Message;
import ox.softeng.burst.domain.Severity;

import java.io.IOException;
import java.io.StringReader;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.bind.JAXB;

public class RabbitService {

  private final static String QUEUE_NAME = "BuRST";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    
	EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory( "ox.softeng.burst" );
	EntityManager entityManager = entityManagerFactory.createEntityManager();

	Severity errorSeverity = new Severity("Error");
	Severity warningSeverity = new Severity("Warning");
	Severity infoSeverity = new Severity("Info");
	Severity debugSeverity = new Severity("Debug");

	entityManager.getTransaction().begin();
	entityManager.persist(errorSeverity);
	entityManager.persist(warningSeverity);
	entityManager.persist(infoSeverity);
	entityManager.persist(debugSeverity);
	entityManager.getTransaction().commit();
    
    
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        String messageString = new String(body, "UTF-8");
        MessageMsg message = JAXB.unmarshal(new StringReader(messageString), MessageMsg.class);
        Message m = message.generateMessage();
		
        entityManager.getTransaction().begin();
		entityManager.merge(m);
		entityManager.getTransaction().commit();

        System.out.println(" [x] Received '" + message.toString() + "'");
        
        
      }
    };
    channel.basicConsume(QUEUE_NAME, true, consumer);
    //entityManager.close();
  }
}