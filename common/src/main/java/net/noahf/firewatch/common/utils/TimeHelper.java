package net.noahf.firewatch.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeHelper {

    public static int getCurrentYear() {
        try {
            return Integer.parseInt(new SimpleDateFormat("YYYY").format(new Date()));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to get current year: " + exception, exception);
        }
    }

}
