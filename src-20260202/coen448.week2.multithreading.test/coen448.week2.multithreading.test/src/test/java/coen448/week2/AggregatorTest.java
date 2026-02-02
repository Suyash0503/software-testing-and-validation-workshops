package coen448.week2;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static org.junit.jupiter.api.Assertions.*;

public class AggregatorTest {

    @Test
    public void testJoinBarrier() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("Result 1");
        CompletableFuture<String> future2 = CompletableFuture.completedFuture("Result 2");
        CompletableFuture<String> future3 = CompletableFuture.completedFuture("Result 3");

        CompletableFuture<Void> allOf = CompletableFuture.allOf(future1, future2, future3);
        allOf.join(); // Join barrier

        assertTrue(allOf.isDone());
    }

    @Test
    public void testPreservedInputOrder() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            sleep(100);
            return "Result 1";
        });
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            sleep(50);
            return "Result 2";
        });
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            sleep(150);
            return "Result 3";
        });

        CompletableFuture<List<String>> allOf = CompletableFuture.allOf(future1, future2, future3)
                .thenApply(v -> List.of(future1.join(), future2.join(), future3.join()));

        List<String> results = allOf.get();
        assertEquals("Result 1", results.get(0));
        assertEquals("Result 2", results.get(1));
        assertEquals("Result 3", results.get(2));
    }

    @Test
    public void testExceptionalCompletion() {
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("Result 1");
        CompletableFuture<String> future2 = CompletableFuture.failedFuture(new RuntimeException("Failure in future 2"));
        CompletableFuture<String> future3 = CompletableFuture.completedFuture("Result 3");

        CompletableFuture<Void> allOf = CompletableFuture.allOf(future1, future2, future3);

        ExecutionException exception = assertThrows(ExecutionException.class, allOf::join);
        assertEquals("java.lang.RuntimeException: Failure in future 2", exception.getMessage());
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}