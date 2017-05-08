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
package ox.softeng.burst.service.report;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportAdapter;
import javax.mail.event.TransportEvent;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @since 05/05/2017
 */
public class EmailService extends TransportAdapter implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final String content;
    private final InternetAddress fromEmailAddress;
    private final String password;
    private final Session session;
    private final String subject;
    private final InternetAddress toEmailAddress;
    private final Transport transport;
    private final String username;
    private boolean emailSent;

    EmailService(Properties properties, String toEmailAddress, String subject, String content) throws NoSuchProviderException, AddressException {
        this.session = Session.getDefaultInstance(properties);
        this.toEmailAddress = new InternetAddress(toEmailAddress);
        this.subject = subject;
        this.content = content;

        fromEmailAddress = new InternetAddress(properties.getProperty("report.email.from"));
        username = properties.getProperty("report.email.username");
        password = properties.getProperty("report.email.password");

        transport = session.getTransport(properties.getProperty("report.email.protocol"));
        transport.addTransportListener(this);

        emailSent = false;
    }

    @Override
    public void messageDelivered(TransportEvent e) {
        emailSent = true;
    }

    @Override
    public void messageNotDelivered(TransportEvent e) {
        emailSent = false;
    }

    @Override
    public void run() {
        try {
            MimeMessage message = createMessage();
            // Send message
            if (Strings.isNullOrEmpty(username)) transport.connect();
            else transport.connect(username, password);
            transport.sendMessage(message, message.getAllRecipients());

            logger.debug("Sent message successfully");
        } catch (MessagingException mex) {
            logger.error("Error whilst attempting to send email: " + mex.getMessage());
            emailSent = false;
        } finally {
            try {
                transport.close();
            } catch (MessagingException ignored) {}
        }
    }

    boolean isEmailSent() {
        return emailSent;
    }

    private MimeMessage createMessage() throws MessagingException {
        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);
        // Set From: header field of the header.
        message.setFrom(fromEmailAddress);
        // Set To: header field of the header.
        message.addRecipient(javax.mail.Message.RecipientType.TO, toEmailAddress);
        // Set Subject: header field
        message.setSubject(subject);
        // Now set the actual message contents
        message.setText(content);
        message.saveChanges();
        return message;
    }
}
