package net.noahf.firegen.discord.utilities;

public enum MessageStatus {

    NONE,

    EMPTY,

    CONTENT;

    public MessageStatus compare(MessageStatus other) {
        if (this == CONTENT || other == CONTENT) return CONTENT;
        if (this == EMPTY || other == EMPTY) return EMPTY;
        return NONE;
    }

}
