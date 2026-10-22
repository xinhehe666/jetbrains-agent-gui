package com.github.agentgui.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for child-process output handling.
 */
public class ShellExecutorTest {

    private static final int LARGE_OUTPUT_LINES = 5000;

    @Test
    public void executeDrainsLargeOutputWithoutPipeDeadlock() {
        ShellExecutor.ExecutionResult result = ShellExecutor.execute(
                largeOutputCommand("large-output-first", "large-output-last"),
                line -> line.startsWith("large-output-"),
                "[ShellExecutorTest]",
                15,
                false
        );

        assertTrue("large child output should complete instead of timing out", result.isSuccess());
        assertEquals("large-output-first", result.getOutput());
        assertTrue("all child output should be drained", result.getAllLines().size() > LARGE_OUTPUT_LINES);
    }

    @Test
    public void executeAndGetLastDrainsLargeOutputAndReturnsLastMatch() {
        ShellExecutor.ExecutionResult result = ShellExecutor.executeAndGetLast(
                largeOutputCommand("large-output-first", "large-output-last"),
                line -> line.startsWith("large-output-"),
                "[ShellExecutorTest]",
                15
        );

        assertTrue("large child output should complete instead of timing out", result.isSuccess());
        assertEquals("large-output-last", result.getOutput());
        assertTrue("all child output should be drained", result.getAllLines().size() > LARGE_OUTPUT_LINES);
    }

    private static List<String> largeOutputCommand(String firstLine, String lastLine) {
        List<String> command = new ArrayList<>();
        if (isWindows()) {
            command.add("cmd.exe");
            command.add("/d");
            command.add("/c");
            command.add("(echo " + firstLine
                    + "& for /L %i in (1,1," + LARGE_OUTPUT_LINES + ") do @echo filler-%i"
                    + "& echo " + lastLine + ")");
        } else {
            command.add("/bin/sh");
            command.add("-c");
            command.add("printf '%s\\n' " + firstLine
                    + "; i=1; while [ $i -le " + LARGE_OUTPUT_LINES
                    + " ]; do echo filler-$i; i=$((i + 1)); done"
                    + "; printf '%s\\n' " + lastLine);
        }
        return command;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
    }
}
