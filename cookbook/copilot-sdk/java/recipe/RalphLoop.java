///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.github:copilot-sdk-java:0.2.1-java.1

import com.github.copilot.sdk.*;
import com.github.copilot.sdk.events.*;
import com.github.copilot.sdk.json.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Optimized Ralph Loop — reads PROMPT.md and runs it in a fresh session each iteration.
 * Enhanced with robust type-safety, clean resource management, and comprehensive error handling.
 *
 * Usage:
 *   jbang RalphLoop.java                  # defaults: PROMPT.md, 50 iterations
 *   jbang RalphLoop.java PROMPT.md 20     # custom prompt file, 20 iterations
 */
public final class RalphLoop {

    private static final String DEFAULT_PROMPT_FILE = "PROMPT.md";
    private static final int DEFAULT_MAX_ITERATIONS = 50;
    private static final String DEFAULT_MODEL = "gpt-5.1-codex-mini";

    private RalphLoop() {
        // Prevent instantiation
    }

    public static void main(String[] args) {
        final String promptFile = args.length > 0 && args[0] != null && !args[0].isBlank() 
                ? args[0] 
                : DEFAULT_PROMPT_FILE;
                
        int maxIterations = DEFAULT_MAX_ITERATIONS;
        if (args.length > 1 && args[1] != null) {
            try {
                maxIterations = Integer.parseInt(args[1]);
                if (maxIterations <= 0) {
                    System.err.println("Warning: max iterations must be positive. Defaulting to 50.");
                    maxIterations = DEFAULT_MAX_ITERATIONS;
                }
            } catch (NumberFormatException e) {
                System.err.printf("Invalid iteration count provided: '%s'. Defaulting to 50.%n", args[1]);
            }
        }

        System.out.printf("Ralph Loop — prompt: %s, max iterations: %d%n", promptFile, maxIterations);

        final Path promptPath = Path.of(promptFile);
        if (!Files.exists(promptPath) || !Files.isReadable(promptPath)) {
            System.err.printf("Error: Prompt file '%s' does not exist or is not readable.%n", promptFile);
            System.exit(1);
        }

        final String prompt;
        try {
            prompt = Files.readString(promptPath);
        } catch (IOException e) {
            System.err.printf("Error reading prompt file '%s': %s%n", promptFile, e.getMessage());
            System.exit(1);
            return;
        }

        try (var client = new CopilotClient()) {
            client.start().get();

            for (int i = 1; i <= maxIterations; i++) {
                System.out.printf("%n=== Iteration %d/%d ===%n", i, maxIterations);

                var sessionConfig = new SessionConfig()
                        .setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
                        .setModel(DEFAULT_MODEL)
                        .setWorkingDirectory(System.getProperty("user.dir"));

                try (var session = client.createSession(sessionConfig).get()) {
                    // Log tool usage for visibility with safe null-checking
                    session.on(ToolExecutionStartEvent.class, ev -> {
                        if (ev != null && ev.getData() != null) {
                            System.out.printf("  ⚙ %s%n", ev.getData().toolName());
                        }
                    });

                    session.sendAndWait(new MessageOptions().setPrompt(prompt)).get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Ralph Loop execution interrupted.");
                    break;
                } catch (ExecutionException e) {
                    System.err.printf("Error during session execution on iteration %d: %s%n", i, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                }

                System.out.printf("Iteration %d complete.%n", i);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("CopilotClient execution interrupted.");
        } catch (ExecutionException e) {
            System.err.printf("Fatal CopilotClient error: %s%n", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.printf("Unexpected error: %s%n", e.getMessage());
            System.exit(1);
        }

        System.out.println("\nAll iterations complete.");
    }
}