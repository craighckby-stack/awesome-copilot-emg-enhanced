///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.github:copilot-sdk-java:0.2.1-java.1

import com.github.copilot.sdk.*;
import com.github.copilot.sdk.events.*;
import com.github.copilot.sdk.json.*;
import java.util.concurrent.ExecutionException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized and resilient error-handling recipe for Copilot SDK Java.
 */
public final class ErrorHandling {
    private static final Logger LOGGER = Logger.getLogger(ErrorHandling.class.getName());

    private ErrorHandling() {
        // Prevent instantiation
    }

    public static void main(String[] args) {
        try (var client = new CopilotClient()) {
            client.start().get();

            try (var session = client.createSession(
                new SessionConfig()
                    .setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
                    .setModel("gpt-5")).get()) {

                Objects.requireNonNull(session, "Session initialization failed.");

                session.on(AssistantMessageEvent.class, msg -> {
                    if (msg != null && msg.getData() != null) {
                        System.out.println(msg.getData().content());
                    }
                });

                session.sendAndWait(
                    new MessageOptions().setPrompt("Hello!")).get();
            }
        } catch (ExecutionException ex) {
            Thread.currentThread().interrupt();
            Throwable cause = ex.getCause();
            Throwable error = cause != null ? cause : ex;
            LOGGER.log(Level.SEVERE, "Execution error: {0}", error.getMessage());
            error.printStackTrace();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Operation interrupted: {0}", ex.getMessage());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error: {0}", ex.getMessage());
            ex.printStackTrace();
        }
    }
}