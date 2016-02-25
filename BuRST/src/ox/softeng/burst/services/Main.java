package ox.softeng.burst.services;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
		Runnable rabbitReceiver = new RabbitService(RabbitMQHost, RabbitMQExchange, RabbitMQQueue, props);
		executor.execute(rabbitReceiver);
		System.out.println("Bof");
		executor.scheduleAtFixedRate(new ReportScheduler(), 1, 2, TimeUnit.MINUTES);
	}
	
	
}
