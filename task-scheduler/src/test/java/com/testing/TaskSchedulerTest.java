package com.testing;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.InOrder;


@DisplayName("TaskScheduler Test Suite")
public class TaskSchedulerTest {

    private TaskScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new TaskScheduler();
    }

    // ==================== Delayed Execution Tests ====================

    @Test
    @DisplayName("Task should execute after specified delay")
    @Timeout(5)
    void testDelayedExecution() throws InterruptedException {
        // Arrange
        Runnable mockTask = mock(Runnable.class);
        long delayMillis = 100;
        long startTime = System.currentTimeMillis();

        // Act
        scheduler.schedule(mockTask, delayMillis);
        Thread.sleep(delayMillis + 50); // Wait for task execution + buffer

        // Assert
        verify(mockTask, times(1)).run();
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertTrue(elapsedTime >= delayMillis, "Task executed before delay");
    }

    @Test
    @DisplayName("Task should not execute before delay completes")
    @Timeout(5)
    void testTaskNotExecutedBeforeDelay() throws InterruptedException {
        // Arrange
        Runnable mockTask = mock(Runnable.class);
        long delayMillis = 200;

        // Act
        scheduler.schedule(mockTask, delayMillis);
        Thread.sleep(delayMillis / 2); // Sleep less than delay

        // Assert - Task should NOT have executed yet
        verify(mockTask, never()).run();
    }

    @Test
    @DisplayName("Multiple tasks should execute at correct intervals")
    @Timeout(5)
    void testMultipleDelayedExecutions() throws InterruptedException {
        // Arrange
        Runnable task1 = mock(Runnable.class);
        Runnable task2 = mock(Runnable.class);
        Runnable task3 = mock(Runnable.class);

        // Act
        long time1 = System.currentTimeMillis();
        scheduler.schedule(task1, 50);
        scheduler.schedule(task2, 100);
        scheduler.schedule(task3, 150);
        
        Thread.sleep(200);

        // Assert
        verify(task1, times(1)).run();
        verify(task2, times(1)).run();
        verify(task3, times(1)).run();
    }

    // ==================== Invalid Scheduling Tests ====================

    @Test
    @DisplayName("Negative delay should raise IllegalArgumentException")
    void testNegativeDelayThrowsException() {
        // Arrange
        Runnable mockTask = mock(Runnable.class);
        long negativeDelay = -100;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            scheduler.schedule(mockTask, negativeDelay);
        }, "Negative delay should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Zero delay should be accepted (immediate execution)")
    @Timeout(2)
    void testZeroDelayIsValid() throws InterruptedException {
        // Arrange
        Runnable mockTask = mock(Runnable.class);

        // Act
        scheduler.schedule(mockTask, 0);
        Thread.sleep(50); // Brief wait for immediate execution

        // Assert - Task should execute immediately
        verify(mockTask, times(1)).run();
    }

    @Test
    @DisplayName("Null task should raise NullPointerException")
    void testNullTaskThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            scheduler.schedule(null, 100);
        }, "Null task should throw NullPointerException");
    }

    // ==================== Concurrent Task Dispatch Tests ====================

    @Test
    @DisplayName("Multiple concurrent task dispatches should not interfere")
    @Timeout(10)
    void testConcurrentTaskDispatches() throws InterruptedException {
        // Arrange
        int numTasks = 50;
        Runnable[] tasks = new Runnable[numTasks];
        long[] executionOrder = new long[numTasks];
        
        for (int i = 0; i < numTasks; i++) {
            final int index = i;
            tasks[i] = mock(Runnable.class);
            doAnswer(invocation -> {
                executionOrder[index] = System.currentTimeMillis();
                return null;
            }).when(tasks[i]).run();
        }

        // Act - Schedule tasks concurrently from multiple threads
        ExecutorService dispatcher = Executors.newFixedThreadPool(10);
        for (int i = 0; i < numTasks; i++) {
            final int index = i;
            dispatcher.submit(() -> scheduler.schedule(tasks[index], 50 + (index % 20)));
        }
        dispatcher.shutdown();
        dispatcher.awaitTermination(5, TimeUnit.SECONDS);

        // Wait for all tasks to execute
        Thread.sleep(300);

        // Assert - All tasks should execute exactly once
        for (int i = 0; i < numTasks; i++) {
            verify(tasks[i], times(1)).run();
        }
    }

    @Test
    @DisplayName("Race condition: concurrent schedule and cancel")
    @Timeout(5)
    void testRaceConditionScheduleAndCancel() throws InterruptedException {
        // Arrange
        Runnable mockTask = mock(Runnable.class);
        AtomicInteger executionCount = new AtomicInteger(0);
        doAnswer(invocation -> {
            executionCount.incrementAndGet();
            return null;
        }).when(mockTask).run();

        // Act - Race between schedule and cancel
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> scheduler.schedule(mockTask, 50));
        Thread.sleep(5);
        executor.submit(() -> scheduler.cancel(mockTask));
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        Thread.sleep(200); // Wait to see if task executes

        // Assert - Either task executed once or not at all (depends on race winner)
        assertTrue(executionCount.get() <= 1, "Task should execute at most once");
    }

    @Test
    @DisplayName("Thread safety: multiple threads scheduling same task")
    @Timeout(5)
    void testThreadSafetyMultipleSchedules() throws InterruptedException {
        // Arrange
        Runnable mockTask = mock(Runnable.class);
        int numThreads = 10;

        // Act - Schedule same task from multiple threads
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    scheduler.schedule(mockTask, 50);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        Thread.sleep(150);

        // Assert - All schedules should complete without exception
        // Invocation count depends on implementation details
        verify(mockTask, atLeastOnce()).run();
    }

    // ==================== Cancellation Tests ====================

    @Test
    @DisplayName("Cancelled task should not run")
    @Timeout(5)
    void testCancelledTaskNotExecuted() throws InterruptedException {
        // Arrange
        Runnable mockTask = mock(Runnable.class);

        // Act
        scheduler.schedule(mockTask, 200);
        Thread.sleep(50);
        scheduler.cancel(mockTask);
        Thread.sleep(250); // Wait past original execution time

        // Assert
        verify(mockTask, never()).run();
    }

    @Test
    @DisplayName("Cancellation before execution should prevent task run")
    @Timeout(3)
    void testEarlyCancel() throws InterruptedException {
        // Arrange
        Runnable mockTask = mock(Runnable.class);

        // Act
        scheduler.schedule(mockTask, 500); // Long delay
        scheduler.cancel(mockTask); // Cancel immediately
        Thread.sleep(600);

        // Assert
        verify(mockTask, never()).run();
    }

    @Test
    @DisplayName("Cancellation after execution should have no effect")
    @Timeout(3)
    void testCancelAfterExecution() throws InterruptedException {
        // Arrange
        Runnable mockTask = mock(Runnable.class);

        // Act
        scheduler.schedule(mockTask, 50);
        Thread.sleep(150); // Wait for execution
        scheduler.cancel(mockTask); // Cancel after execution
        Thread.sleep(50);

        // Assert - Task should have executed once
        verify(mockTask, times(1)).run();
    }

    @Test
    @DisplayName("Cancelling non-existent task should not throw exception")
    void testCancelNonExistentTask() {
        // Arrange
        Runnable mockTask = mock(Runnable.class);

        // Act & Assert - Should not throw
        assertDoesNotThrow(() -> {
            scheduler.cancel(mockTask);
        });
    }

    @Test
    @DisplayName("Multiple cancellations of same task should be idempotent")
    @Timeout(3)
    void testMultipleCancellations() throws InterruptedException {
        // Arrange
        Runnable mockTask = mock(Runnable.class);

        // Act
        scheduler.schedule(mockTask, 100);
        scheduler.cancel(mockTask);
        scheduler.cancel(mockTask); // Second cancellation
        scheduler.cancel(mockTask); // Third cancellation
        Thread.sleep(200);

        // Assert
        verify(mockTask, never()).run();
    }

    // ==================== Boundary Condition Tests ====================

    @Test
    @DisplayName("Very small delay (1ms) should execute")
    @Timeout(3)
    void testMinimalDelay() throws InterruptedException {
        // Arrange
        Runnable mockTask = mock(Runnable.class);

        // Act
        scheduler.schedule(mockTask, 1);
        Thread.sleep(50);

        // Assert
        verify(mockTask, times(1)).run();
    }

    @Test
    @DisplayName("Very large delay should not cause overflow")
    void testLargeDelay() {
        // Arrange
        Runnable mockTask = mock(Runnable.class);
        long largeDelay = Long.MAX_VALUE / 2;

        // Act & Assert - Should not throw
        assertDoesNotThrow(() -> {
            scheduler.schedule(mockTask, largeDelay);
        });
    }

    @Test
    @DisplayName("Rapid successive scheduling should maintain order")
    @Timeout(3)
    void testRapidSuccessiveScheduling() throws InterruptedException {
        // Arrange
        Runnable task1 = mock(Runnable.class);
        Runnable task2 = mock(Runnable.class);
        Runnable task3 = mock(Runnable.class);

        InOrder inOrder = inOrder(task1, task2, task3);

        // Act
        scheduler.schedule(task1, 50);
        scheduler.schedule(task2, 50);
        scheduler.schedule(task3, 50);
        Thread.sleep(150);

        // Assert - All should execute
        inOrder.verify(task1).run();
        inOrder.verify(task2).run();
        inOrder.verify(task3).run();
    }

    // ==================== Time-related Tests ====================

    @Test
    @DisplayName("Now() method should return current time")
    void testNowMethodReturnsCurrent() {
        // Act
        Instant before = Instant.now();
        Instant result = scheduler.now();
        Instant after = Instant.now();

        // Assert
        assertNotNull(result);
        assertTrue(!result.isBefore(before) && !result.isAfter(after),
                "now() should return time between before and after");
    }

}
