///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.github:copilot-sdk-java:0.2.1-java.1

import com.github.copilot.sdk.*;
import com.github.copilot.sdk.json.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MultipleSessions demonstrates concurrent session management and execution
 * using the Copilot Java SDK with type-safety and robust error handling.
 */
public class MultipleSessions {
    private static final Logger LOGGER = Logger.getLogger(MultipleSessions.class.getName());

    public static void main(String[] args) {
        try (var client = new CopilotClient()) {
            client.start().get();

            var defaultConfig = new SessionConfig()
                .setModel("gpt-5")
                .setOnPermissionRequest(PermissionHandler.APPROVE_ALL);

            var alternativeConfig = new SessionConfig()
                .setModel("claude-sonnet-4.5")
                .setOnPermissionRequest(PermissionHandler.APPROVE_ALL);

            // Create 3 sessions in parallel with type-safe CF handles
            CompletableFuture<Session> f1 = client.createSession(defaultConfig);
            CompletableFuture<Session> f2 = client.createSession(defaultConfig);
            CompletableFuture<Session> f3 = client.createSession(alternativeConfig);

            CompletableFuture.allOf(f1, f2, f3).get();

            try (Session s1 = f1.get(); Session s2 = f2.get(); Session s3 = f3.get()) {
                // Send messages concurrently or sequentially with structured error handling
                var r1 = s1.sendAndWait(new MessageOptions().setPrompt("Explain Java records"));
                var r2 = s2.sendAndWait(new MessageOptions().setPrompt("Explain sealed classes"));
                var r3 = s3.sendAndWait(new MessageOptions().setPrompt("Explain pattern matching"));

                System.out.println("S1: " + r1.get().getData().content());
                System.out.println("S2: " + r2.get().getData().content());
                System.out.println("S3: " + r3.get().getData().content());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Execution failed in MultipleSessions workflow", e);
            Thread.currentThread().interrupt();
        }
    }
}
@