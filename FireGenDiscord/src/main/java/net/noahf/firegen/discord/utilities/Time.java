package net.noahf.firegen.discord.utilities;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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

    public enum TimeDiffStyle {
        COMPACT, // 4d, 3h, 2m, 1s
        SHORTEST, // 4d
        CLOCK // 04:03:0201
    }

}
