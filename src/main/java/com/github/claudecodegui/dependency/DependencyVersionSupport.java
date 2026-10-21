package com.github.claudecodegui.dependency;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stateless version parsing and package selection rules for managed SDK dependencies.
 */
final class DependencyVersionSupport {

    private static final Logger LOG = Logger.getInstance(DependencyVersionSupport.class);

    /**
     * Semver-like pattern: major.minor.patch with optional pre-release suffix.
     */
    private static final Pattern SEMVER_PATTERN =
            Pattern.compile("^\\d+\\.\\d+\\.\\d+([-.][a-zA-Z0-9.]+)*$");
    private static final Pattern VERSION_PREFIX_PATTERN = Pattern.compile("^(\\d+)");

    private DependencyVersionSupport() {
    }

    /**
     * Compares two version strings.
     *
     * @return negative if v1 is lower, zero if equal, positive if v1 is higher
     */
    static int compareVersions(String firstVersion, String secondVersion) {
        if (firstVersion == null || secondVersion == null) {
            return 0;
        }

        String[] firstParts = stripLowercaseVPrefix(firstVersion).split("\\.");
        String[] secondParts = stripLowercaseVPrefix(secondVersion).split("\\.");

        int maxLength = Math.max(firstParts.length, secondParts.length);
        for (int index = 0; index < maxLength; index++) {
            int firstPart = index < firstParts.length ? parseVersionPart(firstParts[index]) : 0;
            int secondPart = index < secondParts.length ? parseVersionPart(secondParts[index]) : 0;
            if (firstPart != secondPart) {
                return firstPart - secondPart;
            }
        }
        return 0;
    }

    /**
     * Validates an explicit npm version and rejects values that could alter the install command.
     */
    static String normalizeRequestedVersion(String version) {
        if (version == null) {
            return null;
        }

        String trimmed = version.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("v") || trimmed.startsWith("V")) {
            trimmed = trimmed.substring(1);
        }
        if (!SEMVER_PATTERN.matcher(trimmed).matches()) {
            LOG.warn("[DependencyManager] Rejected invalid version format: " + version);
            return null;
        }
        return trimmed;
    }

    static List<String> buildPackageSpecs(SdkDefinition sdk, String requestedVersion) {
        if (sdk == null) {
            return Collections.emptyList();
        }

        String normalizedVersion = normalizeRequestedVersion(requestedVersion);
        String targetVersion = normalizedVersion != null ? normalizedVersion : sdk.getVersion();
        List<String> packages = new ArrayList<>(sdk.getDependencies().size() + 1);
        packages.add(sdk.getNpmPackage() + "@" + targetVersion);
        packages.addAll(sdk.getDependencies());
        return packages;
    }

    static List<String> parseVersionList(String rawJson) {
        List<String> versions = new ArrayList<>();
        try {
            JsonElement element = JsonParser.parseString(rawJson);
            if (!element.isJsonArray()) {
                return versions;
            }

            // npm 9 may wrap the flat list in one or more single-element arrays.
            JsonArray array = element.getAsJsonArray();
            while (!array.isEmpty() && array.get(0).isJsonArray() && array.size() == 1) {
                array = array.get(0).getAsJsonArray();
            }

            for (JsonElement item : array) {
                if (!item.isJsonPrimitive()) {
                    continue;
                }
                String version = normalizeRequestedVersion(item.getAsString());
                if (version != null && !version.contains("-")) {
                    versions.add(version);
                }
            }
        } catch (Exception exception) {
            LOG.warn("[DependencyManager] Failed to parse version list: " + exception.getMessage());
        }

        versions.sort((left, right) -> compareVersions(right, left));
        return versions;
    }

    private static String stripLowercaseVPrefix(String version) {
        return version.startsWith("v") ? version.substring(1) : version;
    }

    private static int parseVersionPart(String part) {
        Matcher matcher = VERSION_PREFIX_PATTERN.matcher(part);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }
}
