package ox.softeng.burst.services;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

public class Main {

	public static void main(String[] args)
	{
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
		Runnable rabbitReceiver = new RabbitService();
		executor.execute(rabbitReceiver);
		System.out.println("Bof");
		executor.scheduleAtFixedRate(new ReportScheduler(), 1, 2, TimeUnit.MINUTES);
	}
	
	
}
