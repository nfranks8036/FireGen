package net.noahf.firegen.discord.utilities.ansi;

public enum AnsiColor {
    RESET("\u001B[0m"),

    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    MAGENTA("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m"),

    BRIGHT_RED("\u001B[91m"),
    BRIGHT_GREEN("\u001B[92m"),
    BRIGHT_YELLOW("\u001B[93m"),
    BRIGHT_BLUE("\u001B[94m"),

    BACKGROUND_BLACK("\u001B[40m"),
    BACKGROUND_RED("\u001B[41m"),
    BACKGROUND_GREEN("\u001B[42m"),
    BACKGROUND_YELLOW("\u001B[43m"),
    BACKGROUND_BLUE("\u001B[44m"),
    BACKGROUND_PURPLE("\u001B[45m"),
    BACKGROUND_CYAN("\u001B[46m"),
    BACKGROUND_WHITE("\u001B[47m");

    private final String code;

    AnsiColor(String code) {
        this.code = code;
    }

    public String wrap(String text) {
        return code + text + RESET.code;
    }
}
