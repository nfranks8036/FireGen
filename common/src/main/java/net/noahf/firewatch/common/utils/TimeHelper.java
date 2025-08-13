package net.noahf.firewatch.common.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class TimeHelper {

    public static int getCurrentYear() {
        try {
            return Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date()));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to get current year: " + exception, exception);
        }
    }

    public static ZonedDateTime getDate(Instant instant) {
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

}
