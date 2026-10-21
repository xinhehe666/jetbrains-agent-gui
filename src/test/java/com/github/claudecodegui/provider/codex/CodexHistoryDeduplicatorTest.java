package com.github.claudecodegui.provider.codex;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class CodexHistoryDeduplicatorTest {

    @Test
    public void shouldMergeDuplicateSessionsWithoutMutatingInputs() {
        CodexHistoryReader.SessionInfo existing = session("thread-1", "old", 2, 20L, 10L);
        existing.fileSize = 50L;
        CodexHistoryReader.SessionInfo incoming = session("thread-1", "", 3, 30L, 5L);
        incoming.fileSize = 70L;

        List<CodexHistoryReader.SessionInfo> result =
                CodexHistoryDeduplicator.deduplicate(List.of(existing, incoming));

        assertEquals(1, result.size());
        CodexHistoryReader.SessionInfo merged = result.get(0);
        assertNotSame(existing, merged);
        assertNotSame(incoming, merged);
        assertEquals("old", merged.title);
        assertEquals(3, merged.messageCount);
        assertEquals(30L, merged.lastTimestamp);
        assertEquals(5L, merged.firstTimestamp);
        assertEquals(70L, merged.fileSize);
        assertEquals("old", existing.title);
        assertEquals("", incoming.title);
        assertEquals(2, existing.messageCount);
        assertEquals(3, incoming.messageCount);
    }

    @Test
    public void shouldSkipNullAndBlankSessionIds() {
        CodexHistoryReader.SessionInfo valid = session("thread-1", "valid", 1, 10L, 10L);

        List<CodexHistoryReader.SessionInfo> result =
                CodexHistoryDeduplicator.deduplicate(
                        Arrays.asList(null, session("", "blank", 1, 1L, 1L), valid)
                );

        assertEquals(1, result.size());
        assertEquals("thread-1", result.get(0).sessionId);
    }

    @Test
    public void shouldCopySingleSessionWithoutMutatingInput() {
        CodexHistoryReader.SessionInfo source = session("thread-1", "title", 1, 10L, 5L);

        List<CodexHistoryReader.SessionInfo> result =
                CodexHistoryDeduplicator.deduplicate(List.of(source));

        assertEquals(1, result.size());
        assertNotSame(source, result.get(0));
        assertEquals(source.sessionId, result.get(0).sessionId);
        assertEquals(source.title, result.get(0).title);
    }

    private static CodexHistoryReader.SessionInfo session(
            String id,
            String title,
            int messageCount,
            long lastTimestamp,
            long firstTimestamp
    ) {
        CodexHistoryReader.SessionInfo session = new CodexHistoryReader.SessionInfo();
        session.sessionId = id;
        session.title = title;
        session.messageCount = messageCount;
        session.lastTimestamp = lastTimestamp;
        session.firstTimestamp = firstTimestamp;
        return session;
    }
}
