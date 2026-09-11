import fs from "fs";
import path from "path";

const AWESOME_COPILOT_NAMESPACE = "com.github.awesome-copilot";

/**
 * Extracts and normalizes an extension ID from a reference path string.
 * @param {unknown} reference - The reference string to parse.
 * @returns {string | null} The normalized extension ID or null if invalid.
 */
function extensionIdFromReference(reference) {
  if (typeof reference !== "string" || !reference.startsWith("./extensions/")) {
    return null;
  }

  return reference.replace(/^\.\/extensions\//, "").replace(/\/$/, "");
}

/**
 * Builds a mapping of extension IDs to their corresponding plugin names.
 * @param {Array<{ directoryName: string, manifest: Record<string, unknown> }>} pluginEntries - List of plugin entries.
 * @returns {Map<string, string[]>} Mapping from extension ID to list of plugin names.
 */
export function buildExtensionPluginOwners(pluginEntries) {
  const owners = new Map();
  const sortedEntries = [...pluginEntries].sort((a, b) =>
    a.directoryName.localeCompare(b.directoryName)
  );

  for (const { directoryName, manifest } of sortedEntries) {
    const pluginName =
      typeof manifest?.name === "string" && manifest.name.trim()
        ? manifest.name.trim()
        : directoryName;
    const extensionIds = new Set([directoryName]);
    const references =
      manifest?.extensions &&
      typeof manifest.extensions === "object" &&
      manifest.extensions !== null &&
      AWESOME_COPILOT_NAMESPACE in manifest.extensions
        ? /** @type {Record<string, unknown>} */ (manifest.extensions)[AWESOME_COPILOT_NAMESPACE]
        : undefined;

    const extRefs =
      typeof references === "object" &&
      references !== null &&
      "extensions" in references &&
      Array.isArray(/** @type {any} */ (references).extensions)
        ? /** @type {unknown[]} */ (/** @type {any} */ (references).extensions)
        : null;

    if (extRefs) {
      for (const reference of extRefs) {
        const extensionId = extensionIdFromReference(reference);
        if (extensionId) {
          extensionIds.add(extensionId);
        }
      }
    }

    for (const extensionId of extensionIds) {
      const pluginNames = owners.get(extensionId) ?? [];
      if (!pluginNames.includes(pluginName)) {
        pluginNames.push(pluginName);
      }
      owners.set(extensionId, pluginNames);
    }
  }

  return owners;
}

/**
 * Reads extension plugin owners from the specified plugins directory.
 * @param {string} pluginsDir - Path to the plugins directory.
 * @returns {Map<string, string[]>} Mapping from extension ID to list of plugin names.
 */
export function readExtensionPluginOwners(pluginsDir) {
  if (!fs.existsSync(pluginsDir)) {
    return new Map();
  }

  const pluginEntries = fs
    .readdirSync(pluginsDir, { withFileTypes: true })
    .filter((entry) => entry.isDirectory())
    .map((entry) => {
      const manifestPath = path.join(pluginsDir, entry.name, "plugin.json");
      if (!fs.existsSync(manifestPath)) {
        return null;
      }

      try {
        const fileContent = fs.readFileSync(manifestPath, "utf-8");
        return {
          directoryName: entry.name,
          manifest: JSON.parse(fileContent),
        };
      } catch {
        return null;
      }
    })
    .filter((entry) => entry !== null);

  return buildExtensionPluginOwners(pluginEntries);
}

/**
 * Resolves the primary plugin name for a given extension ID.
 * @param {string} extensionId - The extension ID to resolve.
 * @param {Map<string, string[]>} owners - The owners mapping.
 * @returns {string} The resolved plugin name or extension ID.
 */
export function resolveExtensionPluginName(extensionId, owners) {
  const pluginNames = owners.get(extensionId) ?? [];
  return (
    pluginNames.find((pluginName) => pluginName === extensionId) ??
    pluginNames[0] ??
    extensionId
  );
}