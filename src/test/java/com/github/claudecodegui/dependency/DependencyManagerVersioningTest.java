package com.github.claudecodegui.dependency;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DependencyManagerVersioningTest {
    @Test
    public void shouldUseRequestedVersionForMainPackage() {
        List<String> packages = DependencyVersionSupport.buildPackageSpecs(
                SdkDefinition.CLAUDE_SDK,
                "0.3.182"
        );

        assertEquals("@anthropic-ai/claude-agent-sdk@0.3.182", packages.get(0));
        assertEquals("@anthropic-ai/sdk", packages.get(1));
        assertEquals("@anthropic-ai/bedrock-sdk", packages.get(2));
    }

    @Test
    public void shouldFallbackToSdkDefaultVersionWhenRequestedVersionIsBlank() {
        List<String> packages = DependencyVersionSupport.buildPackageSpecs(
                SdkDefinition.CLAUDE_SDK,
                " "
        );

        assertEquals("@anthropic-ai/claude-agent-sdk@^0.3.182", packages.get(0));
    }

    @Test
    public void shouldNormalizeLeadingVInRequestedVersion() {
        assertEquals("0.3.182", DependencyVersionSupport.normalizeRequestedVersion(" v0.3.182 "));
    }

    @Test
    public void shouldAcceptValidSemverVersions() {
        assertEquals("1.0.0", DependencyVersionSupport.normalizeRequestedVersion("1.0.0"));
        assertEquals("0.3.182", DependencyVersionSupport.normalizeRequestedVersion("V0.3.182"));
        assertEquals(
                "1.2.3-beta.1",
                DependencyVersionSupport.normalizeRequestedVersion("1.2.3-beta.1")
        );
        assertEquals(
                "2.0.0-rc.1",
                DependencyVersionSupport.normalizeRequestedVersion("v2.0.0-rc.1")
        );
    }

    @Test
    public void shouldRejectInvalidVersionFormats() {
        assertNull(DependencyVersionSupport.normalizeRequestedVersion("not-a-version"));
        assertNull(DependencyVersionSupport.normalizeRequestedVersion("1.0"));
        assertNull(DependencyVersionSupport.normalizeRequestedVersion("latest"));
        assertNull(DependencyVersionSupport.normalizeRequestedVersion(">=1.0.0"));
        assertNull(DependencyVersionSupport.normalizeRequestedVersion("1.0.0 && rm -rf /"));
    }

    @Test
    public void shouldRejectNullAndEmpty() {
        assertNull(DependencyVersionSupport.normalizeRequestedVersion(null));
        assertNull(DependencyVersionSupport.normalizeRequestedVersion(""));
        assertNull(DependencyVersionSupport.normalizeRequestedVersion("   "));
    }

    @Test
    public void shouldUnwrapNestedVersionListsAndSortNewestFirst() {
        List<String> versions = DependencyVersionSupport.parseVersionList(
                "[[\"0.3.9\",\"0.3.182\",\"0.3.10\",\"0.4.0-beta.1\"]]"
        );

        assertEquals(List.of("0.3.182", "0.3.10", "0.3.9"), versions);
    }

    @Test
    public void shouldCompareMissingVersionSegmentsAsZero() {
        assertEquals(0, DependencyVersionSupport.compareVersions("1.2", "1.2.0"));
        assertTrue(DependencyVersionSupport.compareVersions("v1.10.0", "1.9.9") > 0);
        assertTrue(DependencyVersionSupport.compareVersions("1.2.3", "1.2.4") < 0);
    }

    @Test
    public void claudeSdkShouldRequireFableCapableMinimumVersion() {
        // The Fable tier (ANTHROPIC_DEFAULT_FABLE_MODEL + 'fable' alias) needs SDK 0.3.182+.
        // Pinning this floor here prevents accidental downgrade that would resurrect the
        // "model fable" 401 on third-party relays.
        assertEquals("0.3.182", SdkDefinition.CLAUDE_SDK.getMinRequiredVersion());
    }

    @Test
    public void codexRuntimeNoLongerGatesOnASdkVersion() {
        // Native auto capability is decided by the CLI runtime itself, not by a
        // TypeScript SDK version floor (see design D5 of the app-server migration).
        assertNull(SdkDefinition.fromId("codex-sdk"));
        assertNull(SdkDefinition.fromProvider("codex"));
    }
}
