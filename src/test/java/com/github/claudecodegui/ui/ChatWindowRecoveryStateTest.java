package com.github.claudecodegui.ui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChatWindowRecoveryStateTest {

    @Test
    public void shouldSerializeCompleteRecoveryState() {
        String json = ChatWindowRecoveryState.buildTabStateJson(
                "codex",
                "gpt-5.6-sol",
                "bypassPermissions",
                "high",
                "fast"
        );

        JsonObject state = JsonParser.parseString(json).getAsJsonObject();
        assertEquals("codex", state.get("provider").getAsString());
        assertEquals("gpt-5.6-sol", state.get("model").getAsString());
        assertEquals("bypassPermissions", state.get("permissionMode").getAsString());
        assertEquals("high", state.get("reasoningEffort").getAsString());
        assertEquals("fast", state.get("codexFastMode").getAsString());
    }

    @Test
    public void shouldTreatEveryNonFastTierAsNormal() {
        String json = ChatWindowRecoveryState.buildTabStateJson(
                "claude",
                "fable",
                "default",
                "medium",
                null
        );

        JsonObject state = JsonParser.parseString(json).getAsJsonObject();
        assertEquals("normal", state.get("codexFastMode").getAsString());
    }
}
