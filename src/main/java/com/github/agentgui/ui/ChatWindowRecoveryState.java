package com.github.agentgui.ui;

import com.github.agentgui.handler.SettingsHandler;
import com.github.agentgui.handler.provider.ModelProviderHandler;
import com.github.agentgui.settings.CodemossSettingsService;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;

/**
 * Builds the provider-aware state used when a WebView is recreated after a runtime failure.
 */
final class ChatWindowRecoveryState {

    private static final Logger LOG = Logger.getInstance(ChatWindowRecoveryState.class);

    private ChatWindowRecoveryState() {
    }

    static String buildTabStateJson(
            String provider,
            String model,
            String permissionMode,
            String reasoningEffort,
            String codexServiceTier
    ) {
        JsonObject state = new JsonObject();
        state.addProperty("provider", provider);
        state.addProperty("model", model);
        state.addProperty("permissionMode", permissionMode);
        state.addProperty("reasoningEffort", reasoningEffort);
        state.addProperty("codexFastMode", "fast".equals(codexServiceTier) ? "fast" : "normal");
        return state.toString();
    }

    static int resolveModelContextLimit(
            String provider,
            String model,
            CodemossSettingsService settingsService
    ) {
        if ("codex".equalsIgnoreCase(provider)) {
            return SettingsHandler.getModelContextLimit(provider, model);
        }

        String resolvedModel = model;
        if (settingsService != null) {
            try {
                JsonObject settings = settingsService.readClaudeSettings();
                if (settings != null && settings.has("env") && settings.get("env").isJsonObject()) {
                    resolvedModel = ModelProviderHandler.resolveConfiguredClaudeModel(
                            model,
                            settings.getAsJsonObject("env")
                    );
                }
            } catch (Exception exception) {
                LOG.warn(
                        "Failed to resolve configured Claude model during WebView recovery: "
                                + exception.getMessage()
                );
            }
        }
        return SettingsHandler.getModelContextLimit(resolvedModel);
    }
}
