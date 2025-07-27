package net.noahf.firewatch.common.units;

import java.util.function.Function;
import java.util.regex.Pattern;

public enum UnitStatus {

    OUT_OF_SERVICE ("out-of-service"),

    IN_SERVICE ("clear"),

    RESPONDING ("responding"),

    ON_SCENE ("on-scene"),

    TRANSPORTING_SECONDARY ((str) -> "transporting " + str),

    ARRIVED_SECONDARY ((str) -> "arrived " + str);

    private final Function<String, String> narrativeSuffix;
    private final boolean useInput;

    UnitStatus(String narrativeSuffix) {
        this.narrativeSuffix = (str) -> narrativeSuffix;
        this.useInput = false;
    }

    UnitStatus(Function<String, String> narrativeSuffix) {
        this.narrativeSuffix = narrativeSuffix;
        this.useInput = true;
    }

    public String narrativeSuffix(String input) {
        if (input != null && !input.isBlank() && !this.useInput) {
            return this.narrativeSuffix.apply(input) + (" " + input);
        }
        return this.narrativeSuffix.apply(input);
    }

    @Override
    public String toString() {
        return Pattern.compile("\\b[a-z]")
                .matcher(this.name().toLowerCase().replaceAll("_", " "))
                .replaceAll(mr -> mr.group().toUpperCase());
    }
}
