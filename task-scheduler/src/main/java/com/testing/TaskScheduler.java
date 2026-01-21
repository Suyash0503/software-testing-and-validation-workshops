package com.testing;

import java.time.Instant;
import java.util.concurrent.*;

public class TaskScheduler {
    private final ScheduledExecutorService executor;
    private final ConcurrentHashMap<Runnable, ScheduledFuture<?>> scheduledTasks;

    public TaskScheduler() {
        this.executor = Executors.newScheduledThreadPool(10);
        this.scheduledTasks = new ConcurrentHashMap<>();
    }

    public void schedule(Runnable task, long delayMillis) {
        if (task == null) {
            throw new NullPointerException("Task cannot be null");
        }
        if (delayMillis < 0) {
            throw new IllegalArgumentException("Delay cannot be negative: " + delayMillis);
        }

        // Wrap the task to remove it from the map when executed
        Runnable wrappedTask = () -> {
            try {
                task.run();
            } finally {
                scheduledTasks.remove(task);
            }
        };

        ScheduledFuture<?> future = executor.schedule(wrappedTask, delayMillis, TimeUnit.MILLISECONDS);
        scheduledTasks.put(task, future);
    }

    public void cancel(Runnable task) {
        ScheduledFuture<?> future = scheduledTasks.remove(task);
        if (future != null) {
            future.cancel(false);
        }
    }

    public Instant now() {
        return Instant.now();
    }

    // Shutdown method for cleanup (useful in test teardown if needed)
    public void shutdown() {
        executor.shutdownNow();
    }
}
