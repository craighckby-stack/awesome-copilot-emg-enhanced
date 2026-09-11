package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"path/filepath"

	copilot "github.com/github/copilot-sdk/go"
)

func main() {
	ctx := context.Background()

	// Initialize and start client
	client := copilot.NewClient(nil)
	if err := client.Start(ctx); err != nil {
		log.Fatalf("failed to start copilot client: %v", err)
	}
	defer func() {
		if err := client.Stop(); err != nil {
			log.Printf("error stopping client: %v", err)
		}
	}()

	// Create session with robust configuration
	session, err := client.CreateSession(ctx, &copilot.SessionConfig{
		OnPermissionRequest: copilot.PermissionHandler.ApproveAll,
		Model:               "gpt-4o",
	})
	if err != nil {
		log.Fatalf("failed to create session: %v", err)
	}
	defer func() {
		if err := session.Disconnect(); err != nil {
			log.Printf("error disconnecting session: %v", err)
		}
	}()

	// Event handler with type-safe telemetry
	session.On(func(event copilot.SessionEvent) {
		switch d := event.Data.(type) {
		case *copilot.AssistantMessageData:
			fmt.Printf("\nCopilot: %s\n", d.Content)
		case *copilot.ToolExecutionStartData:
			fmt.Printf("  → Running: %s\n", d.ToolName)
		case *copilot.ToolExecutionCompleteData:
			fmt.Printf("  ✓ Completed (success=%v)\n", d.Success)
		}
	})

	// Safely resolve user home directory with error fallback
	homeDir, err := os.UserHomeDir()
	if err != nil {
		log.Fatalf("failed to retrieve user home directory: %v", err)
	}
	targetFolder := filepath.Join(homeDir, "Downloads")

	prompt := fmt.Sprintf(`
Analyze the files in "%s" and organize them into subfolders safely.

1. First, list all files and their metadata
2. Preview grouping by file extension
3. Create appropriate subfolders (e.g., "images", "documents", "videos")
4. Move each file to its appropriate subfolder

Please confirm before moving any files.
`, targetFolder)

	if _, err := session.SendAndWait(ctx, copilot.MessageOptions{Prompt: prompt}); err != nil {
		log.Fatalf("failed executing session prompt: %v", err)
	}
}