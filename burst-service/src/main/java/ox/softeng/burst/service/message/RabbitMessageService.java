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
package ox.softeng.burst.service.message;

import ox.softeng.burst.service.thread.NamedThreadFactory;
import ox.softeng.burst.util.Utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class RabbitMessageService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMessageService.class);
    private final Connection connection;
    private final Integer consumerCount;
    private final EntityManagerFactory entityManagerFactory;
    private final String exchange;
    private final String queue;

    public RabbitMessageService(EntityManagerFactory emf, Properties props) throws IOException, TimeoutException {

        // Get system properties
        Properties properties = System.getProperties();
        properties.putAll(props);

        exchange = properties.getProperty("rabbitmq.exchange");
        queue = properties.getProperty("rabbitmq.queue");
        entityManagerFactory = emf;
        consumerCount = Utils.convertToInteger("message.service.thread.size",
                                               properties.getProperty("message.service.consumer.size"), 1);

        String host = properties.getProperty("rabbitmq.host");
        String username = properties.getProperty("rabbitmq.user", ConnectionFactory.DEFAULT_USER);
        String password = properties.getProperty("rabbitmq.password", ConnectionFactory.DEFAULT_PASS);
        Integer port = Utils.convertToInteger("rabbitmq.port", properties.getProperty("rabbitmq.port"),
                                              ConnectionFactory.DEFAULT_AMQP_PORT);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setPort(port);
        factory.setHost(host);
        factory.setAutomaticRecoveryEnabled(true);
        factory.setThreadFactory(new NamedThreadFactory("consumer"));

        connection = factory.newConnection();

        logger.info("Creating new RabbitMQ Service using: \n" +
                    "  host: {}:{}\n" +
                    "  user: {}\n" +
                    "  exchange: {}\n" +
                    "  queue: {}", host, port, username, exchange, queue);
    }

    public Connection getConnection() {
        return connection;
    }

    public String getExchange() {
        return exchange;
    }

    public String getQueue() {
        return queue;
    }

    @Override
    public void run() {
        for (int c = 0; c < consumerCount; c++) {
            try {
                Channel channel = connection.createChannel();
                channel.basicQos(40);
                channel.exchangeDeclare(exchange, "topic", true);
                Consumer consumer = new MessageConsumer(channel, entityManagerFactory);
                channel.basicConsume(queue, true, consumer);
            } catch (IOException e) {
                logger.error("Error running consumer for queue '" + queue + "'", e);
            } catch (Exception e) {
                logger.error("Unhandled exception: " + e.getMessage(), e);
            }
        }
    }
}