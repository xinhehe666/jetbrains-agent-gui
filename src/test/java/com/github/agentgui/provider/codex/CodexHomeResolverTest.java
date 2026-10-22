package com.github.agentgui.provider.codex;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CodexHomeResolverTest {

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void resolvesConfiguredCodexHomeAndSessionsDirectory() throws IOException {
        Path customHome = tmp.newFolder("custom-codex-home").toPath().toAbsolutePath();

        Path resolved = CodexHomeResolver.resolveHome(Map.of("CODEX_HOME", customHome.toString()), null);

        assertEquals(customHome.normalize(), resolved);
        assertEquals(customHome.resolve("sessions").normalize(), resolved.resolve("sessions"));
    }

    @Test
    public void fallsBackToDefaultHomeWhenCodexHomeIsBlank() throws IOException {
        Path userHome = tmp.newFolder("user-home").toPath().toAbsolutePath();

        Path resolved = CodexHomeResolver.resolveHome(Map.of("CODEX_HOME", "  "), userHome.toString());

        assertEquals(userHome.resolve(".codex").normalize(), resolved);
    }

    @Test
    public void fallsBackToDefaultHomeWhenCodexHomeIsInvalid() throws IOException {
        Path userHome = tmp.newFolder("invalid-user-home").toPath().toAbsolutePath();

        Path resolved = CodexHomeResolver.resolveHome(Map.of("CODEX_HOME", "relative/path"), userHome.toString());

        assertEquals(userHome.resolve(".codex").normalize(), resolved);
    }
}
