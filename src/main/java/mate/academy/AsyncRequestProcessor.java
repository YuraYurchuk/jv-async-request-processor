package mate.academy;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class AsyncRequestProcessor {
    private final Map<String, CompletableFuture<UserData>> cache = new ConcurrentHashMap<>();
    private final Executor executor;

    public AsyncRequestProcessor(Executor executor) {
        this.executor = executor;
    }

    public CompletableFuture<UserData> processRequest(String userId) {
        CompletableFuture<UserData> request = cache.computeIfAbsent(userId, id ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }

                    return new UserData(id, "Details for " + id);
                }, executor)
        );

        // If processing fails, remove the failed future so a later call can retry.
        request.whenComplete((userData, error) -> {
            if (error != null) {
                cache.remove(userId, request);
            }
        });

        return request;
    }
}
