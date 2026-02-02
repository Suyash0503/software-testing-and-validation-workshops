package coen448.week2.multithreading.test;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
public class CountDownLatchExample {

    private CountDownLatch latch;
    
    final static Logger logger = LoggerFactory.getLogger(CountDownLatchExample.class);

	public CountDownLatchExample(int numberOfTasks) {
		this.latch = new CountDownLatch(numberOfTasks);
		
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

	    try {
	      JoranConfigurator configurator = new JoranConfigurator();
	      configurator.setContext(context);
	      // Call context.reset() to clear any previous configuration, e.g. default
	      // configuration. For multi-step configuration, omit calling context.reset().
	      context.reset();
	      configurator.doConfigure("src\\test\\resources\\logback-test.xml");
	    } catch (JoranException je) {
	      // StatusPrinter will handle this
	    }
	    StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	}


	public void performTask() {
        // Perform some task here
		//....
		latch.countDown(); // Signal that the task is completed
		// Question: logging the number of latch after the task is performed. 
    }

    public void awaitCompletion() throws InterruptedException {
        latch.await(); // Wait until all tasks are completed
    }
    
    public long getLatchCount() {
		return latch.getCount();
	}

    public static void main(String[] args) throws InterruptedException {
        int numberOfTasks = 5;
        CountDownLatchExample example = new CountDownLatchExample(numberOfTasks);

        // Start multiple threads to simulate tasks
        for (int i = 0; i < numberOfTasks; i++) {
            Thread thread = new Thread(() -> {
                example.performTask();
            });
            thread.start();
        }
        // Wait for all tasks to complete
        example.awaitCompletion();
        System.out.println("All tasks completed!");
    }	
}