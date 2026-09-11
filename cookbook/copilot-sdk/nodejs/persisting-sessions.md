# Session Persistence and Resumption

Save and restore conversation sessions across application restarts using the GitHub Copilot SDK.

## Example Scenario

You want users to be able to continue a conversation even after closing and reopening your application.

> **Runnable Example:** [recipe/persisting-sessions.ts](recipe/persisting-sessions.ts)
>
> ```bash
> cd recipe && npm install
> npx tsx persisting-sessions.ts
> # or: npm run persisting-sessions
> ```

### Creating a Session with a Custom ID

```typescript
import { CopilotClient, approveAll } from "@github/copilot-sdk";

// Initialize and start the Copilot client
const client = new CopilotClient();
await client.start();

// Create session with a memorable ID and model selection
const session = await client.createSession({
    onPermissionRequest: approveAll,
    sessionId: "user-123-conversation",
    model: "gpt-5",
});

await session.sendAndWait({ prompt: "Let's discuss TypeScript generics" });

// Session ID is preserved and logged
console.log(session.sessionId); // "user-123-conversation"

// Destroy session instance in memory but keep data persisted on disk
await session.destroy();
await client.stop();
```

### Resuming an Existing Session

```typescript
import { CopilotClient, approveAll } from "@github/copilot-sdk";

const client = new CopilotClient();
await client.start();

// Resume the previous session from disk
const session = await client.resumeSession("user-123-conversation", { 
    onPermissionRequest: approveAll 
});

// Previous conversation context is fully restored
await session.sendAndWait({ prompt: "What were we discussing?" });
// AI successfully remembers the TypeScript generics discussion

await session.destroy();
await client.stop();
```

### Listing Available Sessions

```typescript
import { CopilotClient } from "@github/copilot-sdk";

const client = new CopilotClient();
await client.start();

// Retrieve all stored sessions
const sessions = await client.listSessions();
console.log(sessions);
// [
//   { sessionId: "user-123-conversation", ... },
//   { sessionId: "user-456-conversation", ... },
// ]

await client.stop();
```

### Deleting a Session Permanently

```typescript
import { CopilotClient } from "@github/copilot-sdk";

const client = new CopilotClient();
await client.start();

// Remove session and all associated data from disk storage
await client.deleteSession("user-123-conversation");

await client.stop();
```

## Retrieving Session History

Inspect all prior messages recorded within an active session:

```typescript
const messages = await session.getMessages();
for (const msg of messages) {
    console.log(`[${msg.type}]`, msg.data);
}
```

## Best Practices

1. **Use Meaningful Session IDs**: Include explicit user identifiers or context namespaces in the session ID.
2. **Handle Missing Sessions**: Verify session existence or gracefully handle exceptions when attempting to resume non-existent sessions.
3. **Clean Up Old Sessions**: Periodically purge outdated or unreferenced sessions to manage disk space efficiently.