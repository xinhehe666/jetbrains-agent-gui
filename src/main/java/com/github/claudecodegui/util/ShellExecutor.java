package com.github.claudecodegui.util;

import com.intellij.openapi.diagnostic.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * Shell command execution utility.
 * Encapsulates common logic for process execution, timeout handling, and output filtering.
 */
public final class ShellExecutor {

    private static final Logger LOG = Logger.getInstance(ShellExecutor.class);

    /**
     * Default process timeout in seconds.
     */
    public static final int DEFAULT_TIMEOUT_SECONDS = 5;
    private static final int OUTPUT_DRAIN_TIMEOUT_SECONDS = 2;

    private enum OutputSelection {
        FIRST,
        LAST
    }

    private ShellExecutor() {
        // Utility class, do not instantiate
    }

    /**
     * Execution result.
     */
    public static class ExecutionResult {
        private final boolean success;
        private final String output;
        private final List<String> allLines;
        private final List<String> filteredLines;

        private ExecutionResult(boolean success, String output, List<String> allLines, List<String> filteredLines) {
            this.success = success;
            this.output = output;
            this.allLines = allLines;
            this.filteredLines = filteredLines;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getOutput() {
            return output;
        }

        public List<String> getAllLines() {
            return allLines;
        }

        public List<String> getFilteredLines() {
            return filteredLines;
        }

        public static ExecutionResult success(String output, List<String> allLines, List<String> filteredLines) {
            return new ExecutionResult(true, output, allLines, filteredLines);
        }

        public static ExecutionResult failure() {
            return new ExecutionResult(false, null, List.of(), List.of());
        }

        public static ExecutionResult timeout() {
            return new ExecutionResult(false, null, List.of(), List.of());
        }
    }

    /**
     * Execute a shell command and return the first valid output line.
     *
     * @param command    the command as a list of arguments
     * @param lineFilter line filter; returns true if the line is valid
     * @param logPrefix  prefix for log messages
     * @return the execution result
     */
    public static ExecutionResult execute(List<String> command, Predicate<String> lineFilter, String logPrefix) {
        return execute(command, lineFilter, logPrefix, DEFAULT_TIMEOUT_SECONDS, true);
    }

    /**
     * Execute a shell command and return the result.
     *
     * @param command          the command as a list of arguments
     * @param lineFilter       line filter; returns true if the line is valid
     * @param logPrefix        prefix for log messages
     * @param timeoutSeconds   timeout in seconds
     * @param useInteractive   whether to use interactive shell configuration (sets TERM=dumb)
     * @return the execution result
     */
    public static ExecutionResult execute(
            List<String> command,
            Predicate<String> lineFilter,
            String logPrefix,
            int timeoutSeconds,
            boolean useInteractive
    ) {
        return executeInternal(
                command, lineFilter, logPrefix, timeoutSeconds, useInteractive, OutputSelection.FIRST);
    }

    /**
     * Execute a shell command and return the last valid output line (useful for retrieving environment variables).
     *
     * @param command          the command as a list of arguments
     * @param lineFilter       line filter; returns true if the line is valid
     * @param logPrefix        prefix for log messages
     * @param timeoutSeconds   timeout in seconds
     * @return the execution result
     */
    public static ExecutionResult executeAndGetLast(
            List<String> command,
            Predicate<String> lineFilter,
            String logPrefix,
            int timeoutSeconds
    ) {
        return executeInternal(
                command, lineFilter, logPrefix, timeoutSeconds, true, OutputSelection.LAST);
    }

    private static ExecutionResult executeInternal(
            List<String> command,
            Predicate<String> lineFilter,
            String logPrefix,
            int timeoutSeconds,
            boolean useInteractive,
            OutputSelection selection
    ) {
        Process process = null;
        ProcessOutputDrainer drainer = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            if (useInteractive) {
                // Set TERM=dumb to suppress extra output from interactive shells (color codes, prompts, etc.)
                pb.environment().put("TERM", "dumb");
            }
            pb.redirectErrorStream(true);

            process = pb.start();
            List<String> allLines = new ArrayList<>();
            List<String> filteredLines = new ArrayList<>();
            AtomicReference<String> firstValid = new AtomicReference<>();
            AtomicReference<String> lastValid = new AtomicReference<>();

            drainer = ProcessOutputDrainer.start(process, line -> {
                String trimmed = line.trim();
                allLines.add(trimmed);
                if (lineFilter.test(trimmed)) {
                    firstValid.compareAndSet(null, trimmed);
                    lastValid.set(trimmed);
                } else if (!trimmed.isEmpty()) {
                    filteredLines.add(trimmed);
                }
            });

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                LOG.debug(logPrefix + " command timed out");
                process.destroyForcibly();
                awaitDrainer(drainer, logPrefix);
                return ExecutionResult.timeout();
            }

            awaitDrainer(drainer, logPrefix);

            // Log filtered lines at DEBUG level
            if (!filteredLines.isEmpty() && LOG.isDebugEnabled()) {
                LOG.debug(logPrefix + " filtered lines: " + filteredLines);
            }

            String validOutput = selection == OutputSelection.FIRST
                    ? firstValid.get()
                    : lastValid.get();
            if (validOutput != null) {
                return ExecutionResult.success(validOutput, allLines, filteredLines);
            }

            return ExecutionResult.failure();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ExecutionResult.failure();
        } catch (Exception e) {
            LOG.debug(logPrefix + " execution failed: " + e.getMessage());
            return ExecutionResult.failure();
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            if (drainer != null) {
                drainer.cancel();
            }
        }
    }

    private static void awaitDrainer(ProcessOutputDrainer drainer, String logPrefix) {
        try {
            if (!drainer.await(OUTPUT_DRAIN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                LOG.debug(logPrefix + " output drain timed out");
                drainer.cancel();
                drainer.await(1, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            drainer.cancel();
        }
    }

    /**
     * Create a default filter for interactive shell output.
     * Filters out common shell prompts, login messages, etc.
     *
     * @return the line filter
     */
    public static Predicate<String> createShellOutputFilter() {
        return line -> {
            if (line == null || line.isEmpty()) {
                return false;
            }
            // Skip common shell output noise
            return !line.startsWith("[") &&         // Skip MOTD brackets
                   !line.startsWith("%") &&         // Skip zsh prompts
                   !line.startsWith(">") &&         // Skip continuation prompts
                   !line.contains("Last login");    // Skip login messages
        };
    }

    /**
     * Create a filter for Node.js paths.
     *
     * @return the line filter
     */
    public static Predicate<String> createNodePathFilter() {
        return line -> {
            if (line == null || line.isEmpty()) {
                return false;
            }
            // A valid node path should start with /, end with /node, and not contain error messages
            return line.startsWith("/") &&
                   !line.contains("not found") &&
                   line.endsWith("/node");
        };
    }
}
