package coen448.week2.multithreading.test;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountDownLatchExampleTest {

    @Test
    public void testCountDownLatchExample() throws InterruptedException {
        int numberOfTasks = 3;
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

        // Assert that all tasks are completed
        assertEquals(0, example.getLatchCount(), "All tasks should be completed");
    }
}
