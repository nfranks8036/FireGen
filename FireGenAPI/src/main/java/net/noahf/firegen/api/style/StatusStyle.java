package net.noahf.firegen.api.style;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public record StatusStyle(StatusColor color, StatusBackground background) {

    public static StatusStyle ofDefault() {
        return of(null, null);
    }

    public static StatusStyle ofColor(StatusColor color) {
        return of(color, null);
    }

    public static StatusStyle ofBackground(StatusBackground background) {
        return of(null, background);
    }

    public static StatusStyle of(@Nullable StatusColor color, @Nullable StatusBackground background) {
        return new StatusStyle(color, background);
    }

    /**
     * If input starts with "BACKGROUND_", will interpret as background only and use default color.
     * If input does not start with anything special, will interpret as color only.
     */
    public static StatusStyle fromString(String input) {
        if (input.startsWith("BACKGROUND_")) {
            return StatusStyle.ofBackground(StatusBackground.valueOf(input.substring("BACKGROUND_".length())));
        }
        return StatusStyle.ofColor(StatusColor.valueOf(input));
    }

    public StatusStyle(@Nullable StatusColor color, @Nullable StatusBackground background) {
        this.color = color != null ? color : StatusColor.DEFAULT;
        this.background = background != null ? background : StatusBackground.DEFAULT;
    }

}
