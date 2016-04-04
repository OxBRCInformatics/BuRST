package ox.softeng.burst.services;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import ox.softeng.burst.domain.Frequency;
import ox.softeng.burst.domain.Severity;
import ox.softeng.burst.domain.Subscription;
import ox.softeng.burst.domain.User;

public class ReportScheduler implements Runnable{

	EntityManagerFactory entityManagerFactory;
	String emailsFrom; 
	String smtpHost;
	String smtpUsername;
	String smtpPassword;

	
	
	public ReportScheduler(EntityManagerFactory emf, Properties props)
	{
		this.entityManagerFactory = emf;
		emailsFrom = props.getProperty("smtp-from");
		smtpHost = props.getProperty("smtp-host");
		smtpUsername = props.getProperty("smtp-username");
		smtpPassword = props.getProperty("smtp-password");
	}
	
	@Override
	public void run() {
		//System.out.println("Running version 1.1.2-SNAPSHOT...");
		System.out.println("Generating reports...");

		OffsetDateTime now = OffsetDateTime.now();
		
		
		// First we find all the subscriptions where the "next time" is less than now
		
		EntityManager em = entityManagerFactory.createEntityManager();
		TypedQuery<Subscription> subsQuery = em.createNamedQuery("Subscription.allDue", Subscription.class);
		subsQuery.setParameter("dateNow", now);
		List<Subscription> dueSubscriptions = subsQuery.getResultList();
		
		System.out.println("no. of active subscriptions: " + dueSubscriptions.size());
		for(Subscription s : dueSubscriptions)
		{
			System.out.println("Calculating number of topics...");
			if(s.getTopics().size() == 0)
			{
				System.out.println("Subscription " + s.getId() + " has no registered topics!");
			}
			else
			{
				// For each of those, we find all the matching messages
				Severity severity = s.getSeverity();
				//Frequency frequency = s.getFrequency();
				User u = s.getSubscriber();
				System.out.println("user email:" + u.getEmailAddress());
				
				EntityManager msgEm = entityManagerFactory.createEntityManager();
				System.out.println("Created entity manager");
				TypedQuery<ox.softeng.burst.domain.Message> msgQuery = msgEm.createNamedQuery("Message.MatchedMessages", ox.softeng.burst.domain.Message.class);
				
				System.out.println("Setting date now " + now.toString());
				msgQuery.setParameter("dateNow", now);
				System.out.println("Setting last run date " + s.getLastScheduledRun().toString());
				msgQuery.setParameter("lastSentDate", s.getLastScheduledRun());
				System.out.println("Setting severity: " + severity.getSeverity().ordinal());
				msgQuery.setParameter("severity", severity.getSeverity().ordinal());
				//msgQuery.setParameter("topics", s.getTopics());
				//msgQuery.setParameter("topicsSize", s.getTopics().size());
				List<ox.softeng.burst.domain.Message> matchedMessages = msgQuery.getResultList();
				System.out.println("Retrieved matched messages");
				System.out.println("Size: " + matchedMessages.size());
				System.out.println("matched messages: " + matchedMessages.size());
	
				
				String emailContent = "";
				int count = 0;
				for(ox.softeng.burst.domain.Message msg : matchedMessages)
				{
					System.out.println("message topics size: " + msg.getTopics().size());
					System.out.println("subscription topics size: " + s.getTopics().size());
					// Now we ensure the subscribed topics are a subset of the message topics
					// Can't get this working within the query itself
					if(msg.getTopics().containsAll(s.getTopics()))
					{
						// We send a message with the concatenation of all the messages
						emailContent += msg.getMessage();
						emailContent += "\n\n";
						count ++;
					}					
				}
				System.out.println("topic matched messages: " + matchedMessages.size());
	
				if(count > 0)
				{
					System.out.println("Sending an email to: " + s.getSubscriber().getEmailAddress());
					System.out.println("Content: ");
					System.out.println(emailContent);
					sendMessage(s.getSubscriber().getEmailAddress(),  "Genomics England reporting message", emailContent);
				}
				
				// Then we re-calculate the "last sent" and "next send" timestamps and update the record 
				s.setLastScheduledRun(now);
				s.calculateNextScheduledRun();
				EntityManager subEm = entityManagerFactory.createEntityManager();
				subEm.getTransaction().begin();
				subEm.merge(s);
				subEm.getTransaction().commit();
				
				subEm.close();
				msgEm.close();
			}			
		}
		em.close();

		
		
		// Finally we find any subscription where the "time of next run" is not set,
		// and put a time on it.
		
		EntityManager badSubsEm = entityManagerFactory.createEntityManager();
		TypedQuery<Subscription> badSubsQuery = badSubsEm.createNamedQuery("Subscription.unInitialised", Subscription.class);
		List<Subscription> uninitialisedSubscriptions = badSubsQuery.getResultList();
		System.out.println("Uninitialised subscriptions: " + uninitialisedSubscriptions.size()) ;
		badSubsEm.close();
		for(Subscription s : uninitialisedSubscriptions )
		{
			EntityManager schedEm = entityManagerFactory.createEntityManager();
			schedEm.getTransaction().begin();
			s.calculateNextScheduledRun();
			schedEm.merge(s);
			schedEm.getTransaction().commit();
			schedEm.close();
		}

	}


	private void sendMessage(String emailAddress, String subject, String contents)
	{
		// Get system properties
		Properties properties = System.getProperties();
		// Setup mail server
		properties.setProperty("mail.smtp.host", smtpHost);
		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);
		String protocol = "smtp";
		if(smtpUsername == null || "".equals(smtpUsername))
		{
			System.out.println("sending without authentication...");
			properties.put("mail." + protocol + ".auth", "false");
		}
		else{
			System.out.println("setting authentication method...");
			properties.put("mail." + protocol + ".auth", "true");
			properties.put("mail.smtp.starttls.enable", "true");
		}
		try{
			Transport t = session.getTransport(protocol);
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);
			// Set From: header field of the header.
			message.setFrom(new InternetAddress(emailsFrom));
			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));
			// Set Subject: header field
			message.setSubject(subject);
			// Now set the actual message contents
			message.setText(contents);
			// Send message
		    try {
				if(smtpUsername == null || "".equals(smtpUsername))
				{
					t.connect();
				}
				else{
					t.connect(smtpUsername, smtpPassword);
				}
		        t.sendMessage(message, message.getAllRecipients());
		    } finally {
		        t.close();
		    }
			System.out.println("Sent message successfully....");
		}catch (MessagingException mex) {
			mex.printStackTrace();
		}

	}


}
