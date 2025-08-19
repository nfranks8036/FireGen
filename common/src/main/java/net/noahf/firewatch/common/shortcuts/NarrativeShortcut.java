package net.noahf.firewatch.common.shortcuts;

import net.noahf.firewatch.common.incidents.Incident;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class NarrativeShortcut {

    final ShortcutParser parser;
    final String shortcutName;
    final String raw;

    String placeholders;

    String value;
    boolean valid;

    Map<Character, Function<ShortcutParser, String>> mapper;

    NarrativeShortcut(ShortcutParser parser, String shortcutName, String raw) {
        this.parser = parser;
        this.shortcutName = shortcutName;
        this.raw = raw;
        this.valid = false;
        this.mapper = null;
    }

    public void invalidate() {
        this.valid = false;
    }

    public boolean isValid() {
        return this.valid;
    }

    public String parse() {
        if (!this.valid) {
            this.value = this.parser.parse(this);
        }
        return this.value;
    }



}
