package ox.softeng.burst.util.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TestSendMessage {
	  private final static String QUEUE_NAME = "burst";

	  public static void main(String[] argv) throws Exception {
	    ConnectionFactory factory = new ConnectionFactory();
		  factory.setHost("localhost");
		  Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();

	    channel.exchangeDeclare("Carfax", "topic", true);
	    

	    for(int i=0;i<argv.length;i++)
	    {
			String message = new String(Files.readAllBytes(Paths.get(argv[i])));
		    channel.basicPublish("Carfax", "noaudit.burst", null, message.getBytes("UTF-8"));
		    System.out.println(" [x] Sent '" + message + "'");
	    }
	    
	    channel.close();
	    connection.close();
	  }
}
