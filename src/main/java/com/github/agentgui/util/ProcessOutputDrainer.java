package com.github.agentgui.util;

import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Continuously drains a child process's output streams.
 *
 * <p>Reading must start before waiting for the process. Otherwise a child that
 * writes more than the OS pipe buffer can block forever while the parent waits
 * for it to exit.
 */
public final class ProcessOutputDrainer {

    private static final Logger LOG = Logger.getInstance(ProcessOutputDrainer.class);

    private final InputStream stdout;
    private final InputStream stderr;
    private final Process process;
    private final Thread stdoutThread;
    private final Thread stderrThread;

    private ProcessOutputDrainer(
            Process process,
            InputStream stdout,
            InputStream stderr,
            Thread stdoutThread,
            Thread stderrThread
    ) {
        this.process = process;
        this.stdout = stdout;
        this.stderr = stderr;
        this.stdoutThread = stdoutThread;
        this.stderrThread = stderrThread;
    }

    /**
     * Starts draining both output streams. When stderr is merged into stdout by
     * the caller, the stderr consumer may be {@code null}; it is still drained.
     */
    public static ProcessOutputDrainer start(
            Process process,
            Consumer<String> stdoutConsumer,
            Consumer<String> stderrConsumer
    ) {
        InputStream stdout = process.getInputStream();
        InputStream stderr = process.getErrorStream();
        Thread stdoutThread = startThread("process-stdout-" + process.pid(), stdout, stdoutConsumer);
        Thread stderrThread = startThread("process-stderr-" + process.pid(), stderr, stderrConsumer);
        return new ProcessOutputDrainer(process, stdout, stderr, stdoutThread, stderrThread);
    }

    public static ProcessOutputDrainer start(Process process, Consumer<String> stdoutConsumer) {
        return start(process, stdoutConsumer, null);
    }

    /**
     * Waits for both streams to reach EOF.
     *
     * @return true when both drain threads completed before the timeout
     */
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        if (!joinUntil(stdoutThread, deadline)) {
            return false;
        }
        return joinUntil(stderrThread, deadline);
    }

    /**
     * Closes the streams and interrupts the drain threads. Used after a child
     * process is force-killed or when a descendant keeps an inherited pipe open.
     */
    public void cancel() {
        closeQuietly(stdout);
        closeQuietly(stderr);
        if (process.isAlive()) {
            process.destroyForcibly();
        }
        stdoutThread.interrupt();
        stderrThread.interrupt();
    }

    private static Thread startThread(String name, InputStream stream, Consumer<String> consumer) {
        Thread thread = new Thread(() -> drain(name, stream, consumer), name);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static void drain(String name, InputStream stream, Consumer<String> consumer) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (consumer == null) {
                    continue;
                }
                try {
                    consumer.accept(line);
                } catch (RuntimeException e) {
                    // A consumer failure must not stop draining and deadlock the child.
                    LOG.warn("[" + name + "] output consumer failed: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            LOG.debug("[" + name + "] stream closed: " + e.getMessage());
        }
    }

    private static boolean joinUntil(Thread thread, long deadlineNanos) throws InterruptedException {
        long remainingNanos = deadlineNanos - System.nanoTime();
        if (remainingNanos <= 0L) {
            return !thread.isAlive();
        }
        TimeUnit.NANOSECONDS.timedJoin(thread, remainingNanos);
        return !thread.isAlive();
    }

    private static void closeQuietly(InputStream stream) {
        try {
            stream.close();
        } catch (IOException ignored) {
            // Best effort: the owning drain thread also closes the stream.
        }
    }
}
