package ox.softeng.burst.services;

import com.rabbitmq.client.*;

import ox.softeng.burst.domain.Message;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.TimeoutException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.bind.JAXB;

public class RabbitService implements Runnable {

	private final static String QUEUE_NAME = "burst";

	Connection connection;
	Channel channel;
	EntityManagerFactory entityManagerFactory;

	public RabbitService()
	{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("192.168.99.100");
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare("Carfax", "topic", true);
			
			entityManagerFactory = Persistence.createEntityManagerFactory( "ox.softeng.burst" );

			//entityManager.getTransaction().begin();
			//entityManager.getTransaction().commit();
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}


	}

	public void run() {

		EntityManager entityManager = entityManagerFactory.createEntityManager();

		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
					throws IOException {
				String messageString = new String(body, "UTF-8");
				MessageDTO messageDto = JAXB.unmarshal(new StringReader(messageString), MessageDTO.class);
				Message m = messageDto.generateMessage();


				System.out.println("Received message metadata size: " + messageDto.getMetadata().size());
				System.out.println("Stored message metadata size: " + m.getMetadata().size());

				entityManager.getTransaction().begin();
				entityManager.merge(m);
				//for(Metadata md : m.getMetadata())
				//{
				//	entityManager.merge(md);
				//}
				entityManager.getTransaction().commit();

				System.out.println(" [x] Received '" + messageDto.toString() + "'");


			}
		};
		try {
			channel.basicConsume(QUEUE_NAME, true, consumer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//entityManagerFactory.close();
		//entityManager.close();
	}


}