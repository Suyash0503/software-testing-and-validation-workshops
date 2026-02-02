package coen448.week2.multithreading.test;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ThreadSynchroizationTest {

		final static Logger logger = LoggerFactory.getLogger(ThreadSynchroizationTest.class);
	  
		
		@BeforeAll
		public static void init() {
			LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		    try {
		      JoranConfigurator configurator = new JoranConfigurator();
		      configurator.setContext(context);
		      // Call context.reset() to clear any previous configuration, e.g. default
		      // configuration. For multi-step configuration, omit calling context.reset().
		      context.reset();
		      configurator.doConfigure("C:\\Users\\umroot\\Documents\\coen448\\workspace\\coen448.week2.multithreading.test\\src\\test\\resources\\logback-test.xml");
		    } catch (JoranException je) {
		      // StatusPrinter will handle this
		    }
		    StatusPrinter.printInCaseOfErrorsOrWarnings(context);
		}
		
		
	

	    // funtion based blackbox testing; 
	    @Test
	    public void testCountdownLatch() throws InterruptedException {
	        

	        // Create three worker threads
	        Thread worker1 = new TaskThread();
	        Thread worker2 = new TaskThread();
	        Thread worker3 = new TaskThread();

	        // Start the worker threads
	        
	        logger.info("worker starts.");
	        worker1.start();
	        worker2.start();
	        worker3.start();

	        logger.info("worker joins.");
	        worker1.join();
	        worker2.join();
	        worker3.join();
	       
	        // Perform assertions
	        assertTrue(((TaskThread) worker1).isFinished());
	        
	        assertTrue(((TaskThread) worker2).isFinished());
	        
	        assertTrue(((TaskThread) worker3).isFinished());
	        	        
	      
	    }

	  
	}
