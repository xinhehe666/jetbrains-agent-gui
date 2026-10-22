package com.github.agentgui.provider.codex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Merges duplicate Codex session records without mutating reader-owned instances.
 */
final class CodexHistoryDeduplicator {

    private CodexHistoryDeduplicator() {
    }

    static List<CodexHistoryReader.SessionInfo> deduplicate(
            List<CodexHistoryReader.SessionInfo> sessions
    ) {
        Map<String, CodexHistoryReader.SessionInfo> deduplicated = new LinkedHashMap<>();
        for (CodexHistoryReader.SessionInfo session : sessions) {
            if (session == null || session.sessionId == null || session.sessionId.isEmpty()) {
                continue;
            }
            deduplicated.merge(
                    session.sessionId,
                    copy(session),
                    CodexHistoryDeduplicator::merge
            );
        }

        List<CodexHistoryReader.SessionInfo> result = new ArrayList<>(deduplicated.values());
        result.sort((first, second) -> Long.compare(second.lastTimestamp, first.lastTimestamp));
        return result;
    }

    private static CodexHistoryReader.SessionInfo merge(
            CodexHistoryReader.SessionInfo existing,
            CodexHistoryReader.SessionInfo incoming
    ) {
        CodexHistoryReader.SessionInfo preferred =
                incoming.lastTimestamp >= existing.lastTimestamp ? incoming : existing;
        CodexHistoryReader.SessionInfo fallback = preferred == incoming ? existing : incoming;
        CodexHistoryReader.SessionInfo merged = copy(preferred);

        merged.lastTimestamp = Math.max(existing.lastTimestamp, incoming.lastTimestamp);
        if (merged.firstTimestamp == 0
                || (fallback.firstTimestamp > 0 && fallback.firstTimestamp < merged.firstTimestamp)) {
            merged.firstTimestamp = fallback.firstTimestamp;
        }
        merged.messageCount = Math.max(existing.messageCount, incoming.messageCount);
        if (isBlank(merged.title) && !isBlank(fallback.title)) {
            merged.title = fallback.title;
        }
        if (isBlank(merged.cwd) && !isBlank(fallback.cwd)) {
            merged.cwd = fallback.cwd;
        }
        merged.fileSize = Math.max(existing.fileSize, incoming.fileSize);
        return merged;
    }

    private static CodexHistoryReader.SessionInfo copy(CodexHistoryReader.SessionInfo session) {
        CodexHistoryReader.SessionInfo copy = new CodexHistoryReader.SessionInfo();
        copy.sessionId = session.sessionId;
        copy.title = session.title;
        copy.messageCount = session.messageCount;
        copy.lastTimestamp = session.lastTimestamp;
        copy.firstTimestamp = session.firstTimestamp;
        copy.cwd = session.cwd;
        copy.fileSize = session.fileSize;
        return copy;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isEmpty();
    }
}
