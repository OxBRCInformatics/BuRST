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

import ox.softeng.burst.domain.report.Message;
import ox.softeng.burst.xml.MessageDTO;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;

/**
 * @since 05/05/2017
 */
public class MessageConsumer extends DefaultConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    private final EntityManagerFactory entityManagerFactory;
    private final Unmarshaller unmarshaller;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    MessageConsumer(Channel channel, EntityManagerFactory entityManagerFactory) throws JAXBException {
        super(channel);
        this.entityManagerFactory = entityManagerFactory;
        unmarshaller = JAXBContext.newInstance(MessageDTO.class).createUnmarshaller();
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String messageString = new String(body, "UTF-8");
        String tag = properties.getMessageId() != null ? properties.getMessageId() : consumerTag;
        try {
            MessageDTO messageDto = (MessageDTO) unmarshaller.unmarshal(new StringReader(messageString));
            Message message = Message.generateMessage(messageDto);

            logger.debug("{} - Received message", tag);
            logger.trace("{}", messageDto.toString());
            message.save(entityManagerFactory);
            logger.info("{} - Received {} message from '{}'", tag, message.getSeverity(), message.getSource());
        } catch (JAXBException e) {
            logger.error(tag + " - Could not unmarshall message\n" + messageString, e);
        } catch (HibernateException he) {
            logger.error(tag + " - Could not save message to database: " + he.getMessage(), he);
        } catch (Exception e) {
            logger.error(tag + " - Unhandled exception trying to process message", e);
        }
    }
}
