package net.noahf.firegen.backend.utils;

import net.noahf.firegen.backend.Main;

public class Log {

    public static int SHOWN_OF_CLASS_CHARACTER_LIMIT = 25;

    public static void line(Object object) {
        line(String.valueOf(object), ShowSource.NO);
    }

    public static void line(String message, ShowSource showSource) {
        line0(message, showSource);
    }

    @SuppressWarnings("ConstantValue")
    private static void line0(String message, ShowSource showSource) {
        String displayed = null;
        boolean useOfPlaceholder = false;
        if(showSource.enabled) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
            displayed = caller.getClassName();
            if (showSource == ShowSource.TRUNCATED) {
                displayed = "..." + displayed.substring(displayed.length()-SHOWN_OF_CLASS_CHARACTER_LIMIT);
                if(caller.getClassName().equalsIgnoreCase(Main.class.getCanonicalName()))
                    displayed = " ".repeat(SHOWN_OF_CLASS_CHARACTER_LIMIT + 5);
            }
            if (message.contains("%%class%%")) {
                message = message.replace("%%class%%", displayed);
                useOfPlaceholder = true;
            }
        }
        System.out.println((displayed != null && showSource.enabled && !useOfPlaceholder ? displayed + ": " : "") + message);
    }

    public static void debug(String message) {
        Log.line0("[DEBUG//%%class%%] " + message, ShowSource.YES);
    }

    public enum ShowSource {
        YES(true),
        NO(false),
        TRUNCATED(true);

        final boolean enabled;
        ShowSource(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
}
