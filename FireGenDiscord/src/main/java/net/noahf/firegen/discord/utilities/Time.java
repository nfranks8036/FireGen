package net.noahf.firegen.discord.utilities;

import net.noahf.firegen.discord.services.ServiceResponse;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.users.Permission;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Time {

    public static long getUnix(LocalDateTime time) {
        return time.toEpochSecond(OffsetDateTime.now().getOffset());
    }

    public static long getUnix() {
        return getUnix(LocalDateTime.now());
    }

    public static long getUnixOffset(int offset, TimeUnit unit) {
        LocalDateTime time = LocalDateTime.now();
        if (offset > 0) {
            time = time.plus(offset, unit.toChronoUnit());
        } else if (offset < 0) {
            time = time.minus(offset, unit.toChronoUnit());
        }
        return getUnix(time);
    }

    public static LocalDateTime getLocalDateTime(long unix, String tz) {
        return LocalDateTime.ofEpochSecond(unix, 0,
                ZoneOffset.ofTotalSeconds(TimeZone.getTimeZone(tz).getRawOffset() / 1000)
        );
    }

    public static String getTimeDifference(long futureTime, long pastTime, TimeDiffStyle style) {
        long diffMillis = Math.max(0, futureTime - pastTime);

        long totalSeconds = diffMillis / 1000;

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        switch (style) {
            case COMPACT:
                StringBuilder sb = new StringBuilder();

                if (days > 0) sb.append(days).append("d, ");
                if (hours > 0) sb.append(hours).append("h, ");
                if (minutes > 0) sb.append(minutes).append("m, ");
                if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append("s, ");

                return sb.substring(0, sb.length() - 2);

            case SHORTEST:
                if (days > 0) return days + "d";
                if (hours > 0) return hours + "h";
                if (minutes > 0) return minutes + "m";
                return seconds + "s";

            case CLOCK:
                return String.format("%02d:%02d:%02d:%02d",
                        days, hours, minutes, seconds);

            default:
                throw new IllegalArgumentException("Unknown format");
        }
    }

    private static final String DATE_CREATE_FORMAT = "MM/dd/yyyy";
    private static final String TIME_CREATE_FORMAT = "HH:mm";
    public static ServiceResponse<?> fromInputs(FireGenUser<?> user, String dateInput, String timeInput) {
        if (dateInput == null && timeInput == null) {
            return ServiceResponse.successNone();
        }

        if (!user.hasPermission(Permission.CHANGE_DATE_TIME)) {
            return ServiceResponse.fail(user, "You don't have permission to change the incident date & time.");
        }

        if (timeInput == null) {
            return ServiceResponse.fail(user, "Expected at minimum a time to be provided, found none.");
        }

        LocalDate date = LocalDate.now(); // default to now if no date/time provided
        LocalTime time;

        try {
            time = LocalTime.parse(timeInput, DateTimeFormatter.ofPattern(TIME_CREATE_FORMAT));
        } catch (DateTimeParseException e) {
            return ServiceResponse.fail(user, "Failed to parse your time, expected format '" +
                    TIME_CREATE_FORMAT + "', got '" + timeInput + "'"
            );
        }

        if (dateInput != null) {
            try {
                date = LocalDate.parse(dateInput, DateTimeFormatter.ofPattern(DATE_CREATE_FORMAT));
            } catch (DateTimeParseException e) {
                return ServiceResponse.fail(user, "Failed to parse your date, expected format '" +
                        DATE_CREATE_FORMAT + "', got '" + dateInput + "'");
            }
        }

        return ServiceResponse.success(date.atTime(time));
    }

    public enum TimeDiffStyle {
        COMPACT, // 4d, 3h, 2m, 1s
        SHORTEST, // 4d
        CLOCK // 04:03:0201
    }

}
