package net.noahf.firegen.discord.utilities;

import static net.noahf.firegen.discord.Main.LOGGER;

public class Log {

    public static void user(String message) {
        info("User Action: " + message);
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void warn(String message, Throwable throwable) {
        LOGGER.warn(message, throwable);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void error(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }

    public static void debug(Class<?> origin, String message) {
        LOGGER.debug("[" + origin.getCanonicalName() + "] " + message);
    }

    public static void debug(Class<?> origin, String message, Throwable throwable) { LOGGER.debug("[" + origin.getCanonicalName() + "] " + message, throwable); }

}