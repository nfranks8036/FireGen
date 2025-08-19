package net.noahf.firewatch.common.shortcuts;

import net.noahf.firewatch.common.incidents.Incident;

public class ShortcutParser {

    private static final char[] STATEMENT_CONTAINER = {'<', '>'};
    private static final char STATEMENT_METHOD_SEPARATOR = '.';
    private static final char METHOD_ALL_PARAMETER_SEPARATOR = ':';
    private static final char METHOD_SINGLE_PARAMETER_SEPARATOR = ',';

    private final Incident incident;

    public ShortcutParser(Incident incident) {
        this.incident = incident;
    }

    public Incident incident() { return this.incident; }

    String parse(NarrativeShortcut shortcut) {
        Thread thread = new Thread(() -> {

            String text = shortcut.raw;

            if (shortcut.mapper != null) {
                shortcut.value = shortcut.placeholders;
                for (Character character : shortcut.mapper.keySet()) {
                    shortcut.value = shortcut.value.replace(String.valueOf(character),shortcut.mapper.get(character).apply(this));
                }
                return;
            }

            char[] newText = new char[]{};
            char[] oldText = text.toCharArray();
            boolean inStatement = false, inParams = false;
            for (int i = 0; i < oldText.length; i++) {
                char character = oldText[i];

            }
        });
        thread.start();
    }

}
