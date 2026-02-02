package coen448.week2.multithreading.test;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AsyncOperationTest {

    @Test
    void testAsyncOperation() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                // Simulate async operation
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "Operation completed";
            });
            // Program two lines of code to assert the completion
            // of the asynchronous operation
            // Line 1
            // Line 2
            
        });
    }
    

}




//@Test
//void testAsyncOperation() {
//    assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            // Simulate async operation
//            try {
//                TimeUnit.SECONDS.sleep(2);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            return "Operation completed";
//        });
//
//        String result = future.get(4, TimeUnit.SECONDS);
//        assertEquals("Operation completed", result);
//    });
//}


//@Test
//void testAsyncOperationFailure() {
//  CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//      throw new RuntimeException("Async operation failed");
//  });
//
//  assertThrows(ExecutionException.class, () -> future.get());
//}
