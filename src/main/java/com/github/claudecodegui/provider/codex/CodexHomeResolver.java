package com.github.claudecodegui.provider.codex;

import com.github.claudecodegui.bridge.NodeDetector;
import com.github.claudecodegui.util.PlatformUtils;
import com.github.claudecodegui.util.WslPathUtil;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/** Resolves the Codex home used by both history reads and privacy-index lookups. */
final class CodexHomeResolver {

    private static final String CODEX_HOME_ENV = "CODEX_HOME";

    private CodexHomeResolver() {
    }

    static Path resolveHome() {
        return resolveHome(System.getenv(), PlatformUtils.getHomeDirectory());
    }

    static Path resolveSessionsDir() {
        return resolveHome().resolve("sessions");
    }

    static String resolveHomeScope() {
        return resolveHome().toString();
    }

    static Path resolveHome(Map<String, String> environment, String userHome) {
        String configured = environment == null ? null : environment.get(CODEX_HOME_ENV);
        Path configuredPath = toUsableAbsolutePath(configured);
        if (configuredPath != null) {
            return configuredPath;
        }

        String home = userHome;
        if (home == null || home.isBlank()) {
            home = NodeDetector.resolveHomeForFileOps();
        }
        Path fallback = toUsableAbsolutePath(home);
        if (fallback == null) {
            fallback = Paths.get(PlatformUtils.getHomeDirectory());
        }
        return fallback.resolve(".codex").normalize().toAbsolutePath();
    }

    private static Path toUsableAbsolutePath(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        try {
            Path path = Paths.get(trimmed);
            if (!path.isAbsolute()) {
                return null;
            }
            // Keep WSL absolute paths in their native form; Windows Path parsing would
            // otherwise turn them into drive-relative paths.
            return PlatformUtils.isWindows() && WslPathUtil.isWslPath(trimmed)
                    ? path
                    : path.toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            return null;
        }
    }
}
