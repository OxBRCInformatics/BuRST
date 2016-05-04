package ox.softeng.burst.services;

import ox.softeng.burst.domain.Message;
import ox.softeng.burst.xml.MessageDTO;

import com.rabbitmq.client.*;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RabbitService.class);
    private final EntityManagerFactory entityManagerFactory;
    private Channel channel;
    private String rabbitMQQueue;
    private Unmarshaller unmarshaller;

    public RabbitService(String rabbitMQHost, String rabbitMQExchange, String rabbitMQQueue, EntityManagerFactory emf) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMQHost);
        entityManagerFactory = emf;
        this.rabbitMQQueue = rabbitMQQueue;

        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(rabbitMQExchange, "topic", true);
        } catch (IOException | TimeoutException e) {
            logger.error("Cannot create RabbitMQ  service: " + e.getMessage(), e);
        }

        try {
            unmarshaller = JAXBContext.newInstance(MessageDTO.class).createUnmarshaller();
        } catch (JAXBException e) {
            logger.error("Cannot create JAXB unmarshaller for messages: " + e.getMessage(), e);
        }
    }

    public void run() {

        Consumer consumer = createConsumer();

        try {
            logger.warn("Waiting for messages. To exit press CTRL+C");
            channel.basicConsume(rabbitMQQueue, true, consumer);
        } catch (IOException e) {
            logger.error("Error running consumer for queue '" + rabbitMQQueue + "'", e);
        } catch (Exception e) {
            logger.error("Unhandled exception: " + e.getMessage(), e);
        }
    }

    private Consumer createConsumer() {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String messageString = new String(body, "UTF-8");
                String tag = properties.getMessageId() != null ? properties.getMessageId() : consumerTag;
                try {
                    MessageDTO messageDto = (MessageDTO) unmarshaller.unmarshal(new StreamSource(messageString));
                    Message m = messageDto.generateMessage();

                    logger.debug("{} - Received message DTO: \n{}", tag, messageDto.toString());

                    logger.debug("{} - Saving message to database", tag);
                    EntityManager entityManager = entityManagerFactory.createEntityManager();
                    entityManager.getTransaction().begin();
                    entityManager.merge(m);
                    entityManager.getTransaction().commit();
                    entityManager.close();
                    logger.debug("{} - Message saved", tag);
                    logger.info("{} - Received {} message from '{}'", tag, m.getSeverity(), m.getSource());
                } catch (JAXBException e) {
                    logger.error(tag + " - Could not unmarshall message\n" + messageString, e);
                } catch (HibernateException he) {
                    logger.error(tag + " - Could not save message to database: " + he.getMessage(), he);
                } catch (Exception e) {
                    logger.error(tag + " - Unhandled exception trying to process message", e);
                }
            }
        };
    }


}