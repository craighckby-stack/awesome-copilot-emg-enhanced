///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.github:copilot-sdk-java:0.2.1-java.1

import com.github.copilot.sdk.*;
import com.github.copilot.sdk.events.*;
import com.github.copilot.sdk.json.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized and robust recipe for persisting and resuming Copilot sessions.
 * Implements strict type-safety, efficient resource management, and comprehensive error handling.
 */
public final class PersistingSessions {

    private static final Logger LOGGER = Logger.getLogger(PersistingSessions.java.getName());
    private static final String DEFAULT_SESSION_ID = "user-123-conversation";
    private static final String DEFAULT_MODEL = "gpt-5";
    private static final String DEFAULT_PROMPT = "Let's discuss TypeScript generics";

    private PersistingSessions() {
        // Prevent instantiation of utility/recipe runner class
    }

    public static void main(String[] args) {
        try (var client = new CopilotClient()) {
            client.start().get();

            // Create a session with a custom ID so we can resume it later, using fluent configuration
            var sessionConfig = new SessionConfig()
                    .setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
                    .setSessionId(DEFAULT_SESSION_ID)
                    .setModel(DEFAULT_MODEL);

            var session = client.createSession(sessionConfig).get();
            Objects.requireNonNull(session, "Failed to initialize Copilot session.");

            session.on(AssistantMessageEvent.class, msg -> {
                if (msg != null && msg.getData() != null) {
                    System.out.println(msg.getData().content());
                }
            });

            var messageOptions = new MessageOptions().setPrompt(DEFAULT_PROMPT);
            session.sendAndWait(messageOptions).get();

            System.out.printf("\nSession ID: %s%n", session.getSessionId());

            // Close session safely ensuring data is persisted for later resumption
            session.close();
            System.out.println("Session closed — data persisted to disk.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Session execution was interrupted.", e);
            System.err.println("Error: The operation was interrupted.");
            System.exit(1);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred during session persistence execution.", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}