package coen448.week2.multithreading.test;

import java.util.concurrent.CountDownLatch;

public class TaskThread extends Thread{
	
	        private boolean finished;

	        public boolean isFinished() {
	            return finished;
	        }

	        @Override
	        public void run() {
	            // Simulate some work
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }

	            // Signal that the work is finished
	            
	          
	            	finished = true;   
	        }
}
