package ox.softeng.burst.services;

import ox.softeng.burst.domain.report.Message;
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
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.TimeoutException;

public class RabbitService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RabbitService.class);
    private final Connection connection;
    private final EntityManagerFactory entityManagerFactory;
    private String rabbitMQExchange;
    private String rabbitMQQueue;
    private Unmarshaller unmarshaller;

    public RabbitService(String rabbitMQHost, Integer port, String rabbitMQExchange, String rabbitMQQueue, String username, String password,
                         EntityManagerFactory emf) throws IOException, TimeoutException, JAXBException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setPort(port);
        factory.setHost(rabbitMQHost);
        factory.setAutomaticRecoveryEnabled(true);

        entityManagerFactory = emf;
        this.rabbitMQQueue = rabbitMQQueue;
        this.rabbitMQExchange = rabbitMQExchange;

        connection = factory.newConnection();
        unmarshaller = JAXBContext.newInstance(MessageDTO.class).createUnmarshaller();
    }

    public void run() {
        try {
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(rabbitMQExchange, "topic", true);
            Consumer consumer = createConsumer(channel);
            logger.warn("Waiting for messages");
            channel.basicConsume(rabbitMQQueue, true, consumer);
        } catch (IOException e) {
            logger.error("Error running consumer for queue '" + rabbitMQQueue + "'", e);
        } catch (Exception e) {
            logger.error("Unhandled exception: " + e.getMessage(), e);
        }
    }

    private Consumer createConsumer(Channel channel) {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String messageString = new String(body, "UTF-8");
                String tag = properties.getMessageId() != null ? properties.getMessageId() : consumerTag;
                try {
                    MessageDTO messageDto = (MessageDTO) unmarshaller.unmarshal(new StringReader(messageString));
                    Message m = messageDto.generateMessage();

                    logger.debug("{} - Received message DTO: {}", tag, messageDto.toString());

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