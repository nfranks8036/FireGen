package net.noahf.firegen.discord.utilities;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TimeOutput {

    public static final long STOPWATCH_NOT_STARTED = Long.MIN_VALUE;

    private final Map<LocalDateTime, String> log;
    private long time;

    public TimeOutput() {
        this.log = new LinkedHashMap<>();
        this.time = STOPWATCH_NOT_STARTED;
    }

    public TimeOutput start() {
        this.time = System.currentTimeMillis();
        text("*** STOPWATCH STARTED ***");
        return this;
    }

    public TimeOutput end() {
        String diff = Time.getTimeDifference(System.currentTimeMillis(), time, Time.TimeDiffStyle.CLOCK);
        diff = diff.toUpperCase();

        text("*** STOPWATCH ENDED ***");
        text("*** TOOK " + diff + " ***");
        this.time = STOPWATCH_NOT_STARTED;
        return this;
    }

    public void print(Logger logger, Level level) {
        LoggingEventBuilder log = logger.atLevel(level);
        for (Map.Entry<LocalDateTime, String> entry : this.log.entrySet()) {
            log.log("{" + entry.getKey().format(DateTimeFormatter.ofPattern("MMM d, yyyy @ HH:mm:ss")) +
                    "} " + entry.getValue()
                    );
        }
    }

    public void text(String message) {
        if (this.time == STOPWATCH_NOT_STARTED) {
            throw new IllegalStateException("TimeOutput timer not started. Use TimeOutput.startTimer()");
        }

        String diff = Time.getTimeDifference(System.currentTimeMillis(), time, Time.TimeDiffStyle.CLOCK);
        log.put(LocalDateTime.now(), "[+" + diff + "] " + message);
    }

}
