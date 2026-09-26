package net.osslabz.evmclient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

/** Takes a logger's output off the console and keeps it for assertions until closed. */
public final class CapturedLog implements AutoCloseable {

    private final Logger logger;

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    private CapturedLog(Class<?> type) {
        this.logger = (Logger) LoggerFactory.getLogger(type);
        appender.start();
        logger.addAppender(appender);
        logger.setAdditive(false);
    }

    public static CapturedLog of(Class<?> type) {
        return new CapturedLog(type);
    }

    public List<String> messages(Level level) {
        // Logback appends under the appender's monitor, from whichever thread logs.
        synchronized (appender) {
            return appender.list.stream()
                    .filter(event -> event.getLevel().equals(level))
                    .map(ILoggingEvent::getFormattedMessage)
                    .toList();
        }
    }

    /** Waits until {@code count} messages at {@code level} start with {@code prefix}, and returns them. */
    public List<String> await(Level level, String prefix, int count) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        List<String> matching = matching(level, prefix);
        while (matching.size() < count) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("expected %d %s messages starting with '%s', got %s"
                        .formatted(count, level, prefix, messages(level)));
            }
            Thread.sleep(10);
            matching = matching(level, prefix);
        }
        return matching;
    }

    private List<String> matching(Level level, String prefix) {
        return messages(level).stream()
                .filter(message -> message.startsWith(prefix))
                .toList();
    }

    @Override
    public void close() {
        logger.setAdditive(true);
        logger.detachAppender(appender);
        appender.stop();
    }
}
